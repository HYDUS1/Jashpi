package com.jashpi.ai

import android.app.Service
import android.content.Intent
import android.os.IBinder

class VoiceService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Voice wake word logic goes here later
        return START_STICKY
    }
}
