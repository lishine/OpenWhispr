# OpenWispr: configurable speech server edition

An open-source Android floating microphone, derived from [EdiBianco/OpenWhispr](https://github.com/EdiBianco/OpenWhispr) and Phone Whisper. Use your own OpenAI-compatible transcription server and model, including a Mac-hosted Cohere bridge. Keep Gboard or your preferred keyboard visible while dictating.

## Install and configure

Download the APK from [Releases](https://github.com/lishine/OpenWhispr/releases). Android 11 or later is required. Grant microphone permission, enable OpenWispr Cohere in Android Accessibility, and allow background activity. On Android 13+, App info → ⋮ → Allow restricted settings may be needed before enabling Accessibility.

Open **Settings → Server and model** and enter:

| Setting | What to enter |
|---|---|
| Transcription URL | Complete HTTPS endpoint, including `/audio/transcriptions`. It is not a base URL; the app does not append a path. |
| Model | Your server's exact model ID. |
| Language | Supported code such as `en`. Leave blank to omit this field when the server supports automatic detection. Cohere requires an explicit supported language. |
| API key | Your server's bearer token. Optional for servers that do not require authentication. |

For example, a compatible self-hosted server could use `https://speech.example.com/v1/audio/transcriptions` and its configured model ID. The app sends multipart WAV audio plus `model` and optional `language`, and expects JSON with a `text` property. Provider-specific authentication headers and noncompatible APIs are not supported. No endpoint, model or key is preconfigured in a fresh installation. All four settings can be edited without rebuilding.

For local Cohere on a Mac, expose your own authenticated bridge through HTTPS, enter its full endpoint, `cohere-transcribe-03-2026`, `en`, and its connection key. That Mac must be awake and online. This repository does not provide a public transcription service.

## Use

Open a text field. The mic appears when an editable field is focused **or** the keyboard is visible. Some apps focus their field before opening the keyboard, so you can dictate at that point. Tap once to record, then again to stop. Recording starts only on a tap; it stops automatically at 89 seconds. The mic stays visible during recording and transcription. The result is inserted directly into the focused field, keeping surrounding text and inserting at the cursor or replacing the selection, without touching the clipboard or displaying a success tooltip. The app never sends your message. Fields that reject direct insertion use clipboard paste as a fallback; Android can still show its clipboard notice in that case. If automatic insertion fails, the app shows a manual-paste hint and leaves the transcript on the clipboard.

The keyboard visibility fallback reads Android's interactive input-method window. No app-specific allowlist is required. Hide the idle mic by leaving text entry; pause it using Status → Background service. Disable any older dictation overlay to avoid duplicate buttons.

## Privacy and scope

Recordings go to the server you select; processing location and retention depend on that server. This app records in memory, stores connection settings privately with Android backup disabled, and does not log field contents or include an API key in the APK. See [privacy](PRIVACY.md).

This edition provides transcription and insertion. It removes the upstream Groq cleanup/commands, on-phone models and automatic updater. The installed name/package remains OpenWispr Cohere / `com.edib.openwhispr.cohere`, allowing coexistence with upstream apps.

## Build and updates

Use JDK 17 or 21 and Android SDK platform/build-tools 35; set `ANDROID_HOME` or an ignored `local.properties` with `sdk.dir`.

```sh
./gradlew assembleDebug testDebugUnitTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Keep your private `~/.android/debug.keystore` to sign future updates. A build signed by a different Mac or CI runner cannot update that installation. CI artifacts are build checks; the published APKs use the maintainer's retained signer. Build your own edition consistently with your own signer.

A debug build includes a disposable field for testing without sending anything:

```sh
adb shell am start -n com.edib.openwhispr.cohere/com.edib.openwhispr.DictationCheckActivity
```

For a read-only visibility check with the phone attached, run `python3 scripts/check-overlay.py visible` while its text field/keyboard is open, then `python3 scripts/check-overlay.py hidden` on Home. It checks the actual mic window without collecting field text.

See [Cohere notes](COHERE.md), [upstream README preserved for history](UPSTREAM_README.md), and [license](LICENSE). Original authors retain their attribution and license.
