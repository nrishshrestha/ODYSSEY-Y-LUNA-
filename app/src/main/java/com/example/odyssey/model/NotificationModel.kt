package com.example.odyssey.model

data class NotificationModel(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val type: String = "", // "FOLLOW", "MESSAGE", "TRIP_INVITE"
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
)
