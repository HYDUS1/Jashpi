package com.jashpi.ai

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object GeminiApi {
    suspend fun getResponse(apiKey: String, prompt: String): String {
        if (apiKey.isEmpty()) return "Please enter your Gemini API Key in Settings."
        val client = OkHttpClient()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        
        val json = """
            {
              "contents": [{
                "parts":[{"text": "$prompt"}]
              }]
            }
        """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return "Error: No response"
            val jsonObject = JSONObject(responseBody)
            val candidates = jsonObject.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
