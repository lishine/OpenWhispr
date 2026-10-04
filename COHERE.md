# OpenWispr Cohere edition

A focused Android push-to-talk overlay derived from EdiBianco/OpenWhispr v3.10.0. The mic appears during text entry and stays visible until recording/transcription finishes. It inserts text while your existing keyboard remains open.

This edition uses an existing personal Mac bridge at `https://cohere-mac.devpark.dev/v1/audio/transcriptions`, model `cohere-transcribe-03-2026`, English. This is an authenticated personal endpoint, not a public speech service. Enter its connection key in Settings → Mac connection key. No key is included in this repository or APK. To use your own compatible bridge, change the endpoint in `TranscriberClient.kt` and rebuild. The Mac and its Cloudflare Tunnel must be running and online.

Groq cleanup, voice commands, on-phone models and the upstream APK updater are removed. The package is `com.edib.openwhispr.cohere`, so it can coexist with the original apps. Its signing key is generated locally by Gradle, never the upstream checked-in key. Keep `~/.android/debug.keystore` to install future updates. Build from the same Mac, or transfer that private keystore securely to the other Mac before building; a GitHub Actions artifact signed on another runner cannot update that installation.

## Build

Use JDK 17 or 21 and an Android SDK with platform/build-tools 35. Set `ANDROID_HOME` or an ignored `local.properties` with `sdk.dir`.

```sh
./gradlew assembleDebug testDebugUnitTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Grant microphone access, enable OpenWispr Cohere in Android Accessibility, and allow unrestricted background activity. Disable the older dictation overlay to avoid two mic buttons. Keep Gboard as the default keyboard. Tap the mic once to record and again to stop. Recording does not start automatically. The app stops and transcribes automatically at 89 seconds, within the bridge’s 90-second limit.

For a disposable test field in a debug build:

```sh
adb shell am start -n com.edib.openwhispr.cohere/com.edib.openwhispr.DictationCheckActivity
```

It never saves or sends field contents; the normal microphone still uses the configured Mac bridge.

The original upstream license and attribution remain in this fork. The upstream README below describes the original app, not this edition.
