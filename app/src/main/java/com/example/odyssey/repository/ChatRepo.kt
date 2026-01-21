package com.example.odyssey.repository

import com.example.odyssey.model.ChatMessage
import im.zego.zim.entity.ZIMMessage

interface ChatRepo {
    fun login(userId: String, userName: String, callback: (Boolean, String) -> Unit)
    fun logout()
    fun sendMessage(toUserId: String, message: String, callback: (Boolean, String) -> Unit)
    fun receiveMessages(callback: (List<ChatMessage>) -> Unit)
    fun queryHistory(userId: String, callback: (List<ChatMessage>) -> Unit)
}
