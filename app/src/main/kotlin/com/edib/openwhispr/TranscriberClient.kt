package com.edib.openwhispr

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object TranscriberClient {
    data class Result(val text: String?, val error: String?)

    private val client = OkHttpClient.Builder()
        .readTimeout(100, TimeUnit.SECONDS)
        .build()

    fun parseResponse(json: String, code: Int = 200): Result = try {
        val obj = JSONObject(json)
        when {
            code in 200..299 && obj.has("text") -> Result(obj.getString("text"), null)
            obj.has("error") -> Result(null, obj.getJSONObject("error").getString("message"))
            else -> Result(null, if (code in 200..299) "Unknown response" else "Mac Cohere unavailable (HTTP $code). Check that the Mac is awake and its tunnel is running.")
        }
    } catch (e: Exception) {
        Result(null, if (code in 200..299) "Invalid transcription response" else "Mac Cohere unavailable (HTTP $code). Check that the Mac is awake and its tunnel is running.")
    }

    internal fun request(wavData: ByteArray, apiKey: String): Request {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "cohere-transcribe-03-2026")
            .addFormDataPart("language", "en")
            .addFormDataPart("file", "audio.wav", wavData.toRequestBody("audio/wav".toMediaType()))
            .build()

        return Request.Builder()
            .url("https://cohere-mac.devpark.dev/v1/audio/transcriptions")
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

    }

    fun transcribe(wavData: ByteArray, apiKey: String, callback: (Result) -> Unit) {
        client.newCall(request(wavData, apiKey)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = callback(Result(null, e.message))
            override fun onResponse(call: Call, response: Response) {
                val result = try {
                    response.use { parseResponse(it.body?.string() ?: "", it.code) }
                } catch (e: IOException) {
                    Result(null, "Could not read Mac Cohere response. Check your connection.")
                }
                callback(result)
            }
        })
    }
}
