package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.repository.SmsRepository

class SmsApplication : Application() {

    lateinit var repository: SmsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = SmsRepository(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                OTP_CHANNEL_ID,
                "OTP & SMS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for detected OTP verification codes"
                enableVibration(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val OTP_CHANNEL_ID = "otp_sms_channel"
        lateinit var instance: SmsApplication
            private set
    }
}
