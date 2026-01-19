package com.example.odyssey.model

data class ChatModel(
    val userId: String,
    val userName: String,
    val profileImageUrl: String? = null
)
