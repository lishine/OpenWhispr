# Configurable speech server edition privacy

Microphone WAV recordings are sent over HTTPS to the transcription URL you select in Settings. There is no default public endpoint. Your chosen provider or self-hosted server controls processing and retention. With a personal Mac Cohere bridge, transcription runs on that Mac; an HTTPS tunnel provider also carries the audio. The original Mac bridge deletes temporary files after processing, but this app cannot impose that policy on other servers.

The app records in memory. It copies the transcript to Android's clipboard and attempts insertion into the active field. Android Accessibility detects editable focus and the visible input-method window, and inserts the result. Focused-field content and API keys are not printed to app logs. Connection settings are stored privately on-device and Android backup is disabled. No key is included in source or APK. There is no cleanup/chat-model call or on-phone speech engine.

The upstream policy below is historical and does not describe this edition's configurable destination.

---

# Privacy Policy for OpenWhispr

OpenWhispr is an Android dictation app that records speech, transcribes it, and inserts the result into text fields across apps.

## Data handling

OpenWhispr supports two transcription modes.

### Local mode

In local mode, audio is processed on-device using local speech recognition models. Audio does not leave the device.

### Cloud mode

In cloud mode, recorded audio is sent directly from the device to Groq's transcription API to generate text.

If optional cleanup is enabled, the transcribed text is also sent directly from the device to Groq's chat API to improve punctuation, capitalization, and clarity.

## API keys

If you use cloud features, your Groq API key is stored locally on your device in app storage and used to authenticate requests sent directly to Groq.

I do not operate a relay server for these requests.

## Accessibility Service

OpenWhispr uses Android Accessibility Service only to identify the currently focused text field and insert dictated text after you explicitly interact with the floating overlay button.

OpenWhispr is not designed to monitor browsing, collect screen content for analytics, or perform background automation.

## Data collection

I do not run a backend for OpenWhispr and do not collect user accounts, analytics, or uploaded recordings myself.

Third-party services you choose to use, such as Groq, may process data according to their own terms and privacy policies.

## Contact

For questions about privacy, open an issue at: https://github.com/EdiBianco/OpenWhispr
