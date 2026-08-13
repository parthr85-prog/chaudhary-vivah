package com.example.model

data class InterestRequest(
    val id: String, // format: "${senderId}_${receiverId}"
    val senderId: String,
    val receiverId: String,
    val senderPhone: String = "",
    val receiverPhone: String = "",
    val senderName: String = "",
    val receiverName: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, BLOCKED
    val timestamp: Long = System.currentTimeMillis()
)
