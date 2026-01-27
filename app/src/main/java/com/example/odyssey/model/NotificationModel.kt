package com.example.odyssey.model

import com.google.firebase.database.PropertyName

data class NotificationModel(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val type: String = "", // "FOLLOW", "MESSAGE", "TRIP_INVITE"
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,

    val metadata: Map<String, String> = emptyMap()
)
