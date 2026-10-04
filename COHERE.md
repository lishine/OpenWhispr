# Mac Cohere setup

This fork accepts editable HTTPS endpoint, model, language and bearer key in **Settings → Server and model**. See the [README](README.md) for the full setup and build instructions. No personal server is built into the app.

For your own Mac-hosted Cohere bridge, enter the complete `/v1/audio/transcriptions` endpoint, model `cohere-transcribe-03-2026`, supported language `en`, and your bridge's connection key. Keep the Mac awake and its HTTPS route running. The bridge must accept WAV multipart uploads and return JSON `{"text":"..."}`. Recordings stop automatically at 89 seconds to fit the original Mac bridge's 90-second cap.

The original maintainer's installed route remains configured privately on their phone. This repository includes no credentials or free public endpoint. Other compatible speech servers can be configured without rebuilding; supported languages, available models and data handling belong to the chosen server.
