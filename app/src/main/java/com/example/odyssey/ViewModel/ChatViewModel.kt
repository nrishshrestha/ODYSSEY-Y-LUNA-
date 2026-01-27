package com.example.odyssey.ViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.model.ChatMessageType
import com.example.odyssey.repository.ChatRepo
import timber.log.Timber

class ChatViewModel(private val repository: ChatRepo) : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    fun initChat(currentUserId: String, currentUserName: String, toUserId: String) {
        Timber.tag("ChatViewModel").d("Initializing chat: $currentUserId -> $toUserId")

        // Clear existing messages when switching chats
        _messages.value = emptyList()

        // Setup listener first to ensure we don't miss anything
        repository.receiveMessages { newMessages ->
            Log.d("ChatViewModel", "Received ${newMessages.size} real-time messages")
            val current = _messages.value ?: emptyList()

            // Filter: Only show messages relevant to this conversation
            val filteredNew = newMessages.filter { nm ->
                current.none { c -> c.messageId == nm.messageId } &&
                        (nm.senderId == toUserId || nm.senderId == currentUserId)
            }

            if (filteredNew.isNotEmpty()) {
                // For real-time messages, we append them to the end
                _messages.postValue(current + filteredNew)
            }
        }

        repository.login(currentUserId, currentUserName) { success, _ ->
            if (success) {
                Timber.tag("ChatViewModel").d("Login successful, fetching history...")
                loadHistory(toUserId)
            } else {
                Timber.tag("ChatViewModel").e("Login failed")
                // Still try to load history even if login says already logged in or similar
                loadHistory(toUserId)
            }
        }
    }

    private fun loadHistory(userId: String) {
        repository.queryHistory(userId) { history ->
            Timber.tag("ChatViewModel").d("Loaded ${history.size} history messages")
            // ZIM queryHistory with reverse = true returns oldest first.
            // repository.queryHistory uses reverse = true, so messageList is oldest first.
            // We want oldest at the top, newest at the bottom for LazyColumn.
            _messages.postValue(history)
        }
    }

    fun sendMessage(toUserId: String, text: String) {
        repository.sendMessage(toUserId, text) { success, _ ->
            if (success) {
                // In a real app, the real-time listener should catch the sent message too if configured.
                // If not, we manually add it.
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
        // Check if message already exists (e.g. if listener already caught it)
        if (current.none { it.messageId == message.messageId }) {
            _messages.postValue(current + message)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
