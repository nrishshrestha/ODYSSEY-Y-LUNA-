package com.example.odyssey.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.repository.ChatRepo

class ChatViewModel(private val repository: ChatRepo) : ViewModel() {
    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    fun login(userId: String, userName: String) {
        repository.login(userId, userName) { success, _ ->
            if (success) {
                repository.receiveMessages { newMessages ->
                    val current = _messages.value ?: emptyList()
                    _messages.postValue(current + newMessages)
                }
            }
        }
    }

    fun loadHistory(userId: String) {
        repository.queryHistory(userId) { history ->
            _messages.postValue(history)
        }
    }

    fun sendMessage(toUserId: String, text: String) {
        repository.sendMessage(toUserId, text) { success, _ ->
            if (success) {
                // Manually add to list for instant feedback
                val newMessage = ChatMessage(
                    senderId = "me", // Should ideally be currentUserId
                    message = text,
                    timestamp = System.currentTimeMillis()
                )
                val current = _messages.value ?: emptyList()
                _messages.postValue(current + newMessage)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.logout()
    }
}
