package com.example.odyssey.ViewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.model.ChatMessageType
import com.example.odyssey.repository.ChatRepo

class ChatViewModel(private val repository: ChatRepo) : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    fun login(userId: String, userName: String) {
        repository.login(userId, userName) { success, _ ->
            if (success) {
                repository.receiveMessages { newMessages ->
                    val current = _messages.value ?: emptyList()
                    // Filter out duplicates if any
                    val filteredNew = newMessages.filter { nm -> current.none { c -> c.messageId == nm.messageId } }
                    if (filteredNew.isNotEmpty()) {
                        _messages.postValue(current + filteredNew)
                    }
                }
            }
        }
    }

    fun loadHistory(userId: String) {
        repository.queryHistory(userId) { history ->
            _messages.postValue(history.reversed()) // Usually history comes in reverse order
        }
    }

    fun sendMessage(toUserId: String, text: String) {
        repository.sendMessage(toUserId, text) { success, _ ->
            if (success) {
                val newMessage = ChatMessage(
                    senderId = "me", 
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    type = ChatMessageType.TEXT
                )
                addLocalMessage(newMessage)
            }
        }
    }

    fun sendMedia(toUserId: String, uri: Uri, type: ChatMessageType) {
        repository.sendMediaMessage(toUserId, uri, type) { success, _ ->
            if (success) {
                val newMessage = ChatMessage(
                    senderId = "me",
                    message = "[Media]",
                    timestamp = System.currentTimeMillis(),
                    type = type,
                    mediaUrl = uri.toString()
                )
                addLocalMessage(newMessage)
            }
        }
    }

    private fun addLocalMessage(message: ChatMessage) {
        val current = _messages.value ?: emptyList()
        _messages.postValue(current + message)
    }

    override fun onCleared() {
        super.onCleared()
        repository.logout()
    }
}
