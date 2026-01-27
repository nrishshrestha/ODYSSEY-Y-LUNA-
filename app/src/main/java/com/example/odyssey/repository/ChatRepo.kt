package com.example.odyssey.repository

import android.net.Uri
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.model.ChatMessageType

interface ChatRepo {
    fun login(userId: String, userName: String, callback: (Boolean, String) -> Unit)
    fun logout()
    fun sendMessage(toUserId: String, message: String, callback: (Boolean, String) -> Unit)
    fun sendMediaMessage(toUserId: String, uri: Uri, type: ChatMessageType, callback: (Boolean, String) -> Unit)
    fun receiveMessages(callback: (List<ChatMessage>) -> Unit)
    fun queryHistory(userId: String, callback: (List<ChatMessage>) -> Unit)
}
