package com.example.service

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"

    suspend fun registerAndSyncFcmToken(userId: String) {
        if (userId.isBlank() || userId == "USER_ME") return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (!token.isNullOrBlank()) {
                saveTokenToFirestore(userId, token)
            }
        } catch (e: Exception) {
            Log.w(TAG, "FCM token retrieval not supported or failed on this device/environment: ${e.message}")
        }
    }

    fun saveTokenToFirestore(userId: String, token: String) {
        if (userId.isBlank() || userId == "USER_ME" || token.isBlank()) return
        try {
            val db = FirebaseFirestore.getInstance()
            val updates = mapOf(
                "fcmTokens" to FieldValue.arrayUnion(token),
                "lastFcmToken" to token,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("profiles").document(userId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.i(TAG, "FCM token saved successfully for user $userId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save FCM token to profiles/$userId", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving token for $userId", e)
        }
    }
}
