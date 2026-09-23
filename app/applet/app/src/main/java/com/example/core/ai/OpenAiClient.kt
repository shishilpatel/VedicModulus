package com.example.core.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object OpenAiClient {
    private val client = OkHttpClient()
    private const val URL = "https://api.openai.com/v1/chat/completions"

    fun isAvailable(): Boolean {
        val key = BuildConfig.OPENAI_API_KEY
        return key.isNotBlank() && key != "MY_OPENAI_API_KEY" && key.startsWith("sk-")
    }

    suspend fun generateContent(prompt: String, systemPrompt: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        if (!isAvailable()) {
            throw IllegalStateException("OpenAI API Key is not configured or invalid.")
        }

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            val messagesArray = JSONArray()
            if (!systemPrompt.isNullOrBlank()) {
                messagesArray.put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
            }
            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
            put("messages", messagesArray)
        }

        val body = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw RuntimeException("OpenAI API error (${response.code}): $errorBody")
            }
            val responseString = response.body?.string() ?: throw RuntimeException("Empty response from OpenAI")
            val jsonResponse = JSONObject(responseString)
            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                return@withContext message.getString("content")
            } else {
                throw RuntimeException("No choices returned from OpenAI")
            }
        }
    }
}
