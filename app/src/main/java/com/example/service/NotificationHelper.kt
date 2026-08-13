package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.util.UUID

object NotificationHelper {
    private const val CHANNEL_ID = "chaudhary_vivah_general_channel"
    private const val CHANNEL_NAME = "Chaudhary Vivah Notifications"
    private const val CHANNEL_DESC = "General Notifications for chat messages, interest requests, and security alerts."

    @Volatile
    var activeChatPartnerId: String? = null

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val mainChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(mainChannel)

            val channel1 = NotificationChannel("chaudhary_vivah_alerts", "Chaudhary Vivah Alerts", importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel1)

            val channel2 = NotificationChannel("chaudhary_vivah_notifications", "Chaudhary Vivah Push Notifications", importance).apply {
                description = "Push notifications for messages and requests"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel2)
        }
    }

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("vivah_app_prefs", Context.MODE_PRIVATE)
        val storedId = prefs.getString("device_id", null)
        if (!storedId.isNullOrBlank()) {
            return storedId
        }
        val newId = "DEV_${UUID.randomUUID().toString().take(12)}"
        prefs.edit().putString("device_id", newId).apply()
        return newId
    }

    fun showSystemPushNotification(
        context: Context,
        title: String,
        body: String,
        type: String? = null,
        targetId: String? = null,
        senderId: String? = null,
        chatId: String? = null,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        try {
            // Check if user is currently looking at this active chat
            if (!senderId.isNullOrBlank() && senderId == activeChatPartnerId && (type == "CHAT" || type == "NEW_MESSAGE")) {
                Log.d("NotificationHelper", "Skipping notification banner as chat with $senderId is currently open")
                return
            }

            initNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (!type.isNullOrBlank()) putExtra("notification_type", type)
                if (!targetId.isNullOrBlank()) putExtra("target_id", targetId)
                if (!senderId.isNullOrBlank()) putExtra("sender_id", senderId)
                if (!chatId.isNullOrBlank()) putExtra("chat_id", chatId)
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to post system notification", e)
        }
    }
}

private fun String?.isNull_Blank(): Boolean {
    return this == null || this.trim().isEmpty()
}
