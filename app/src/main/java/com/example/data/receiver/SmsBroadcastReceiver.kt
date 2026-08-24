package com.example.data.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SmsApplication
import com.example.data.util.OtpParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val pendingResult = goAsync()
            val sender = messages[0].originatingAddress ?: "Unknown"
            val bodyBuilder = StringBuilder()
            var timestamp = System.currentTimeMillis()

            for (sms in messages) {
                bodyBuilder.append(sms.messageBody)
                timestamp = sms.timestampMillis
            }

            val body = bodyBuilder.toString()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? SmsApplication
                    val repository = app?.repository
                    val item = repository?.addIncomingMessage(sender, body, timestamp)

                    // If OTP detected, trigger local high priority notification
                    val otp = item?.otpDetails ?: OtpParser.parseOtp(sender, body, timestamp)
                    if (otp != null) {
                        showOtpNotification(context, otp.serviceName, otp.code, body)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showOtpNotification(
        context: Context,
        serviceName: String,
        code: String,
        body: String
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("KEY_AUTO_OTP", code)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, SmsApplication.OTP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(0xFFFF5F00.toInt())
                .setContentTitle("OTP for $serviceName: $code")
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: Exception) {
            // Notification post permission or security exception handling
        }
    }
}
