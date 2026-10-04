package com.edib.openwhispr

import org.junit.Assert.*
import org.junit.Test

class TranscriberClientTest {
    @Test fun `routes WAV to Mac Cohere with its connection key`() {
        val req = TranscriberClient.request(byteArrayOf(1, 2), "test-connection-key")
        assertEquals("https://cohere-mac.devpark.dev/v1/audio/transcriptions", req.url.toString())
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


    @Test fun `offline tunnel HTML is a useful error rather than a JSON error`() {
        val result = TranscriberClient.parseResponse("<html>Origin unavailable</html>", 530)
        assertNull(result.text)
        assertTrue(result.error!!.contains("Mac Cohere unavailable (HTTP 530)"))
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
