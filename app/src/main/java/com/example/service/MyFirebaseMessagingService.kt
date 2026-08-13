package com.example.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM token received: $token")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (currentUserId.isNotBlank()) {
            FcmTokenManager.saveTokenToFirestore(currentUserId, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message Received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: "ચૌધરી વિવાહ સંસ્થાન"
        val body = notification?.body ?: data["body"] ?: "નવો સંદેશ કે રિક્વેસ્ટ આવેલ છે."

        val type = data["type"] ?: data["notification_type"] ?: "SYSTEM"
        val senderId = data["senderId"] ?: data["sender_id"]
        val targetId = data["targetId"] ?: data["target_id"] ?: senderId
        val chatId = data["chatId"] ?: data["chat_id"] ?: senderId

        Log.d(TAG, "Notification Details: title=$title, type=$type, senderId=$senderId")

        NotificationHelper.showSystemPushNotification(
            context = applicationContext,
            title = title,
            body = body,
            type = type,
            targetId = targetId,
            senderId = senderId,
            chatId = chatId
        )
    }

    companion object {
        private const val TAG = "MyFirebaseMessaging"
    }
}
