package com.example.odyssey.model

data class FriendModel(
    val id: String = "",          // Firebase key for this relation
    val fromUserId: String = "",  // Who sent the request
    val toUserId: String = "",    // Who receives the request
    val status: String = "pending", // "pending" | "accepted"
    val createdAt: Long = 0L      // System.currentTimeMillis()
)
