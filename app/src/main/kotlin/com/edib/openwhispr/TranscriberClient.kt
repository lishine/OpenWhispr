package com.edib.openwhispr

import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object TranscriberClient {
    data class Result(val text: String?, val error: String?)

    private val client = OkHttpClient.Builder()
        .readTimeout(100, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    internal fun endpoint(value: String): HttpUrl? = value.toHttpUrlOrNull()?.takeIf {
        it.isHttps && it.username.isEmpty() && it.password.isEmpty()
    }

    private fun httpError(code: Int): String = "Server returned HTTP $code. " + when (code) {
        401, 403 -> "Check the API key."
        404 -> "Check the complete transcription URL."
        else -> "Check your server and connection."
    }

    fun parseResponse(json: String, code: Int = 200): Result = try {
        val obj = JSONObject(json)
        when {
            code in 200..299 && obj.has("text") -> Result(obj.getString("text"), null)
            obj.has("error") -> Result(null, obj.getJSONObject("error").getString("message"))
            else -> Result(null, if (code in 200..299) "Unknown response" else httpError(code))
        }
    } catch (e: Exception) {
        Result(null, if (code in 200..299) "Invalid transcription response" else httpError(code))
    }

    internal fun request(wavData: ByteArray, endpoint: String, model: String, language: String, apiKey: String): Request {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", model)
            .apply { if (language.isNotBlank()) addFormDataPart("language", language) }
            .addFormDataPart("file", "audio.wav", wavData.toRequestBody("audio/wav".toMediaType()))
            .build()

        return Request.Builder()
            .url(requireNotNull(endpoint(endpoint)) { "Invalid HTTPS endpoint" })
            .apply { if (apiKey.isNotBlank()) header("Authorization", "Bearer $apiKey") }
            .post(body)
            .build()

    }

    fun transcribe(wavData: ByteArray, endpoint: String, model: String, language: String, apiKey: String, callback: (Result) -> Unit) {
        val request = try { request(wavData, endpoint, model, language, apiKey) } catch (e: IllegalArgumentException) {
            callback(Result(null, "Invalid transcription URL or API key. Check Settings."))
            return
        }
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback(Result(null, e.message))
            override fun onResponse(call: Call, response: Response) {
                val result = try {
                    response.use { parseResponse(it.body?.string() ?: "", it.code) }
                } catch (e: IOException) {
                    Result(null, "Could not read transcription server response. Check your connection.")
                }
                callback(result)
            }
        })
    }
}
