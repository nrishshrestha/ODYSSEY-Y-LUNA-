package com.example.odyssey.ViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.model.ChatMessageType
import com.example.odyssey.repository.ChatRepo

class ChatViewModel(private val repository: ChatRepo) : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    fun initChat(currentUserId: String, currentUserName: String, toUserId: String) {
        Log.d("ChatViewModel", "Initializing chat: $currentUserId -> $toUserId")
        
        // Setup listener first to ensure we don't miss anything while logging in
        repository.receiveMessages { newMessages ->
            Log.d("ChatViewModel", "Received ${newMessages.size} real-time messages")
            val current = _messages.value ?: emptyList()
            
            // Filter: Only show messages relevant to this conversation
            val filteredNew = newMessages.filter { nm -> 
                current.none { c -> c.messageId == nm.messageId } && 
                (nm.senderId == toUserId || nm.senderId == currentUserId)
            }
            
            if (filteredNew.isNotEmpty()) {
                _messages.postValue(current + filteredNew)
            }
        }

        repository.login(currentUserId, currentUserName) { success, _ ->
            if (success) {
                Log.d("ChatViewModel", "Login successful, fetching history...")
                loadHistory(toUserId)
            } else {
                Log.e("ChatViewModel", "Login failed")
            }
        }
    }

    private fun loadHistory(userId: String) {
        repository.queryHistory(userId) { history ->
            Log.d("ChatViewModel", "Loaded ${history.size} history messages")
            // ZIM history is usually newest-first, we want oldest-first for the LazyColumn
            _messages.postValue(history.reversed())
        }
    }

    fun sendMessage(toUserId: String, text: String) {
        repository.sendMessage(toUserId, text) { success, _ ->
            if (success) {
                val newMessage = ChatMessage(
                    senderId = "me", // Marker for local UI
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    type = ChatMessageType.TEXT,
                    messageId = "local_${System.currentTimeMillis()}"
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
                    mediaUrl = uri.toString(),
                    messageId = "local_media_${System.currentTimeMillis()}"
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
        // Optional: logout if you want to close connection when VM dies
        // repository.logout()
    }
}
