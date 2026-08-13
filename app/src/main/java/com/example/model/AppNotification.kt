package com.example.model

data class AppNotification(
    val id: String,
    val userId: String,
    val profileId: String = "",
    val senderProfileId: String = "",
    val title: String,
    val message: String,
    val type: String, // "CHAT", "INTEREST", "LOGIN_ALERT", "SYSTEM"
    val targetId: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
