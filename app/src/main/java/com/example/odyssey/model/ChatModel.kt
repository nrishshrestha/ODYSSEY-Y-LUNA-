package com.example.odyssey.model


data class ChatModel(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageContent: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "TEXT", // e.g., "TEXT", "IMAGE", "VOICE"
    val imageUrl: String? = null,
    val voiceUrl: String? = null,
    val isRead: Boolean = false,
    val status: String = "SENT" // e.g., "SENT", "DELIVERED", "READ"
)
