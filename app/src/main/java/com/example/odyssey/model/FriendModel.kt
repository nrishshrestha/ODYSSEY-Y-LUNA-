package com.example.odyssey.model

data class FriendModel(
    val id: String = "",           // Unique id for this friend relation (could be a document id)
    val userId: String = "",       // Current user id
    val friendId: String = "",     // Friend user id
    val status: String = "pending" // Status of the friend relation (e.g., "pending", "accepted", "rejected")
)
