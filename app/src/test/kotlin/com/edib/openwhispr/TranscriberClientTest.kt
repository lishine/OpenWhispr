package com.edib.openwhispr

import org.junit.Assert.*
import org.junit.Test

class TranscriberClientTest {
    @Test fun `routes WAV to Mac Cohere with its connection key`() {
        val req = TranscriberClient.request(byteArrayOf(1, 2), "https://speech.example.com/v1/audio/transcriptions", "cohere-transcribe-03-2026", "en", "test-connection-key")
        assertEquals("https://speech.example.com/v1/audio/transcriptions", req.url.toString())
        assertEquals("Bearer test-connection-key", req.header("Authorization"))
        val buffer = okio.Buffer()
        req.body!!.writeTo(buffer)
        val body = buffer.readUtf8()
        assertTrue(body.contains("cohere-transcribe-03-2026"))
        val language = (req.body as okhttp3.MultipartBody).parts.single {
            it.headers?.get("Content-Disposition") == "form-data; name=\"language\""
        }
        val languageBody = okio.Buffer()
        language.body.writeTo(languageBody)
        assertEquals("en", languageBody.readUtf8())
        assertTrue(body.contains("filename=\"audio.wav\""))
        assertFalse(body.contains("whisper-large-v3"))
    }


    @Test fun `custom model and automatic language work without authentication`() {
        val req = TranscriberClient.request(byteArrayOf(1, 2), "https://other.example.com/audio/transcriptions", "custom-whisper", "", "")
        assertEquals("https://other.example.com/audio/transcriptions", req.url.toString())
        assertNull(req.header("Authorization"))
        val body = req.body as okhttp3.MultipartBody
        assertFalse(body.parts.any { it.headers?.get("Content-Disposition") == "form-data; name=\"language\"" })
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        assertTrue(buffer.readUtf8().contains("custom-whisper"))
    }

    @Test fun `offline tunnel HTML is a useful error rather than a JSON error`() {
        val result = TranscriberClient.parseResponse("<html>Origin unavailable</html>", 530)
        assertNull(result.text)
        assertTrue(result.error!!.contains("Server returned HTTP 530"))
    }

    @Test fun `rejects unsafe endpoints and invalid header values without network access`() {
        assertNull(TranscriberClient.endpoint("http://speech.example.com/v1/audio/transcriptions"))
        assertNull(TranscriberClient.endpoint("https://user:pass@speech.example.com/v1/audio/transcriptions"))
        assertNull(TranscriberClient.endpoint("not a url"))
        assertNotNull(TranscriberClient.endpoint("https://speech.example.com/v1/audio/transcriptions"))
        var result: TranscriberClient.Result? = null
        TranscriberClient.transcribe(byteArrayOf(1), "not a url", "model", "", "") { result = it }
        assertNotNull(result?.error)
        result = null
        TranscriberClient.transcribe(byteArrayOf(1), "https://speech.example.com/v1/audio/transcriptions", "model", "", "bad\nkey") { result = it }
        assertNotNull(result?.error)
        val unauthorized = TranscriberClient.parseResponse("{\"detail\":\"bad key\"}", 401)
        assertTrue(unauthorized.error!!.contains("API key"))
    }

    @Test fun `parses success response`() {
        val r = TranscriberClient.parseResponse("""{"text": "Hello world"}""")
        assertEquals("Hello world", r.text)
        assertNull(r.error)
    }

    @Test fun `parses error response`() {
        val r = TranscriberClient.parseResponse("""{"error":{"message":"Invalid key","type":"auth"}}""")
        assertNull(r.text)
        assertEquals("Invalid key", r.error)
    }

    @Test fun `handles unknown format`() {
        val r = TranscriberClient.parseResponse("""{"foo":"bar"}""")
        assertNull(r.text)
        assertNotNull(r.error)
    }

    @Test fun `handles malformed json`() {
        val r = TranscriberClient.parseResponse("not json")
        assertNull(r.text)
        assertNotNull(r.error)
    }
}
