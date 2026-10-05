"""Physical Android check using a disposable debug field and developer speech fixture.
Requires connected/unlocked phone, enabled service, configured speech endpoint,
and Mac speaker audio near the phone. Overwrites clipboard with a test sentinel.
Prints booleans/counts only, never the transcript or existing clipboard contents.
"""
import argparse
import json
from pathlib import Path
import re
import shlex
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--fixture', required=True)
parser.add_argument('--case', choices=['empty', 'middle', 'selection', 'limit'], required=True)
args = parser.parse_args()
package = 'com.edib.openwhispr.cohere'
cases = {'empty': ('', 0, 0), 'middle': ('Before  after', 7, 7),
         'selection': ('Before replace after', 7, 14), 'limit': ('ABCDE', 2, 2)}
seed, start, end = cases[args.case]


def adb(*parts):
    return subprocess.check_output(['adb', *parts], text=True, timeout=30)


def nodes():
    adb('shell', 'uiautomator', 'dump', '/data/local/tmp/dictation-check.xml')
    xml = adb('shell', 'cat', '/data/local/tmp/dictation-check.xml')
    return [n for n in ET.fromstring(xml).iter('node') if n.get('package') == package]


def tap_bounds(bounds):
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', bounds))
    adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))


parts = ['shell', 'am', 'start', '--activity-clear-top', '-n',
         package+'/com.edib.openwhispr.DictationCheckActivity', '--ez', 'clipboard_check',
         'true', '--es', 'seed', shlex.quote(seed), '--ei', 'selection_start', str(start),
         '--ei', 'selection_end', str(end)]
if args.case == 'limit':
    parts += ['--ei', 'max_length', '5']
adb(*parts)
initial = next(n for n in nodes() if n.get('class') == 'android.widget.EditText')
if seed:
    assert initial.get('text') == seed, 'Test seed did not reach the disposable field'
tap_bounds(initial.get('bounds'))
time.sleep(.4)
windows = adb('shell', 'dumpsys', 'window', 'windows')
blocks = re.split(r'(?=  Window #\d+)', windows)
block = next(b for b in blocks if 'package='+package in b and
             'ty=ACCESSIBILITY_OVERLAY' in b and 'Surface: shown=true' in b)
frame = re.search(r' frame=(\[\d+,\d+\]\[\d+,\d+\])', block).group(1)
with tempfile.TemporaryDirectory(prefix='dictation-clipboard-') as directory:
    log_path = Path(directory) / 'actions.log'
    with log_path.open('w') as out:
        log = subprocess.Popen(['adb', 'logcat', '-T', '1', '-v', 'brief', 'OpenWhispr:I', '*:S'],
                               stdout=out, stderr=subprocess.DEVNULL)
        try:
            tap_bounds(frame)
            time.sleep(.6)
            subprocess.run(['afplay', args.fixture], check=True, timeout=30)
            time.sleep(.2)
            tap_bounds(frame)
            deadline = time.monotonic()+25
            while time.monotonic() < deadline:
                time.sleep(.5)
                if 'Text injection action reported success' in log_path.read_text():
                    break
        finally:
            log.terminate()
            log.wait(timeout=3)
    actions = log_path.read_text()
    assert 'Text injection action reported success' in actions, 'No successful speech insertion within 25 seconds'
result = nodes()
field = next(n for n in result if n.get('class') == 'android.widget.EditText').get('text')
status = None
for _ in range(3):
    tap_bounds(next(n for n in result if n.get('class') == 'android.widget.Button').get('bounds'))
    result = nodes()
    status = next((n.get('text') for n in result if n.get('text', '').startswith('Clipboard unchanged:')), None)
    if status is not None:
        break
assert status is not None, 'Could not activate the debug check button after layout settled'
if args.case == 'limit':
    assert field == seed, 'Length-limited field lost existing characters'
    assert 'Clipboard unchanged: false;' in status, 'Expected clipboard fallback'
    assert 'ACTION_PASTE => true' in actions, 'Fallback paste did not succeed'
    proof = {'existing_text_preserved': True, 'clipboard_fallback': True}
else:
    prefix, suffix = seed[:start], seed[end:]
    inserted = len(field) - len(prefix) - len(suffix)
    assert inserted > 0 and field.startswith(prefix) and field.endswith(suffix), 'Surrounding text changed'
    assert 'Tap here, then tap the mic to dictate' not in field, 'Hint became field content'
    caret = len(prefix)+inserted
    assert status == f'Clipboard unchanged: true; cursor: {caret},{caret}', 'Clipboard or cursor changed'
    assert 'ACTION_SET_TEXT => true' in actions and 'ACTION_PASTE' not in actions, 'Expected direct insertion only'
    proof = {'surrounding_text_preserved': True, 'clipboard_unchanged': True,
             'cursor_after_insertion': True, 'direct_set_text': True, 'inserted_characters': inserted}
print(json.dumps({'case': args.case, 'passed': True, **proof}))
