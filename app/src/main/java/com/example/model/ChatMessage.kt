package com.example.model

data class ChatMessage(
    val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val profileId: String = "", // The match profile ID
    val senderName: String = "",
    val isUserSender: Boolean = false,
    val text: String = "",
    val timestamp: String = "",
    val timestampMs: Long = System.currentTimeMillis(),
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val audioUrl: String = ""
)
