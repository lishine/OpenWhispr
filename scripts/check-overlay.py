"""Read-only native regression check: run with a focused field, or on Home."""
import argparse
import re
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument('expected', choices=['visible', 'hidden'])
args = parser.parse_args()
windows = subprocess.check_output(['adb', 'shell', 'dumpsys', 'window', 'windows'], text=True)
blocks = re.split(r'(?=  Window #\d+)', windows)
mic = [b for b in blocks if 'package=com.edib.openwhispr.cohere' in b
       and 'ty=ACCESSIBILITY_OVERLAY' in b and re.search(r'\(\d+x\d+\)', b)]
assert len(mic) == 1, 'Expected one mic window; check service setup'
visible = 'mViewVisibility=0x0 ' in mic[0] and 'Surface: shown=true' in mic[0]
assert visible == (args.expected == 'visible'), f'Expected {args.expected}; visible={visible}'
print(f'PASS: mic {args.expected}')
