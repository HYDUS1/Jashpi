package com.jashpi.ai

import okhttp3.*
import java.io.IOException

object TelegramHelper {
    fun sendSOS(token: String, chatId: String, userName: String) {
        if (token.isEmpty() || chatId.isEmpty()) return
        val client = OkHttpClient()
        val message = "🚨 EMERGENCY! 🚨\n$userName has triggered the SOS button in Jashpi AI!\nPlease check on them immediately."
        
        val url = "https://api.telegram.org/bot$token/sendMessage"
        val body = FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", message)
            .build()

        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { e.printStackTrace() }
            override fun onResponse(call: Call, response: Response) { response.close() }
        })
    }
}
