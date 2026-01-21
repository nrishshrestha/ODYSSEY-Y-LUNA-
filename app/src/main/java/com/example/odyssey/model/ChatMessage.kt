package com.example.odyssey.model

enum class ChatMessageType {
    TEXT, IMAGE, VIDEO, FILE
}

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val messageId: String = "",
    val type: ChatMessageType = ChatMessageType.TEXT,
    val mediaUrl: String? = null,
    val thumbnailUrl: String? = null
)
