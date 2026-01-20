package com.example.odyssey.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.ChatRepo

class ChatViewModel(private val chatRepo: ChatRepo) : ViewModel() {

    // Initializes ZegoCloud for the current user.
    // Call this after successful login to the app.
    fun initChat(user: UserModel, callback: (Boolean, String) -> Unit = { _, _ -> }) {
        val fullName = "${user.firstName} ${user.lastName}".trim()
        chatRepo.initZegoCloudUser(user.userId, fullName, callback)
    }

    // Starts a chat session with another user.
    fun onChatClicked(context: Context, friend: UserModel, callback: (Boolean, String) -> Unit = { _, _ -> }) {
        chatRepo.startOneOnOneChat(context, friend, callback)
    }

    // Logs out the user from ZegoCloud.
    fun logout() {
        chatRepo.logoutZegoCloud()
    }
}
