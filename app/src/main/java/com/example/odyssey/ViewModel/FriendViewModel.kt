package com.example.odyssey.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.odyssey.model.FriendModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.FriendRepo
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendViewModel(
    private val friendRepository: FriendRepo
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _friends = MutableStateFlow<List<UserModel>>(emptyList())
    val friends: StateFlow<List<UserModel>> = _friends

    private val _pendingRequests = MutableStateFlow<List<FriendModel>>(emptyList())
    val pendingRequests: StateFlow<List<FriendModel>> = _pendingRequests

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        refreshAll()
    }

    fun refreshAll() {
        val uid = auth.currentUser?.uid ?: return
        loadFriends(uid)
        loadPendingRequests(uid)
    }

    fun sendFriendRequest(toUserId: String) {
        val fromUserId = auth.currentUser?.uid
        if (fromUserId == null) {
            _message.value = "User not logged in"
            return
        }

        _loading.value = true
        viewModelScope.launch {
            friendRepository.sendFriendRequest(fromUserId, toUserId) { success, msg ->
                Log.d("FriendViewModel", "sendFriendRequest: $success, $msg")
                _message.value = msg
                _loading.value = false
                if (success) {
                    refreshAll()
                }
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        val uid = auth.currentUser?.uid ?: return

        _loading.value = true
        viewModelScope.launch {
            friendRepository.acceptFriendRequest(requestId) { success, msg ->
                Log.d("FriendViewModel", "acceptFriendRequest: $success, $msg")
                _message.value = msg
                _loading.value = false
                if (success) {
                    loadFriends(uid)
                    loadPendingRequests(uid)
                }
            }
        }
    }

    fun cancelFriendRequest(requestId: String) {
        val uid = auth.currentUser?.uid ?: return

        _loading.value = true
        viewModelScope.launch {
            friendRepository.cancelFriendRequest(requestId) { success, msg ->
                Log.d("FriendViewModel", "cancelFriendRequest: $success, $msg")
                _message.value = msg
                _loading.value = false
                if (success) {
                    loadPendingRequests(uid)
                }
            }
        }
    }

    fun removeFriend(otherUserId: String) {
        val uid = auth.currentUser?.uid ?: return

        _loading.value = true
        viewModelScope.launch {
            friendRepository.removeFriend(uid, otherUserId) { success, msg ->
                Log.d("FriendViewModel", "removeFriend: $success, $msg")
                _message.value = msg
                _loading.value = false
                if (success) {
                    loadFriends(uid)
                }
            }
        }
    }

    fun loadFriends(userId: String? = auth.currentUser?.uid) {
        val uid = userId ?: return
        _loading.value = true
        viewModelScope.launch {
            friendRepository.getFriendUsers(userId) { success, msg, list ->
                Log.d("FriendViewModel", "loadFriends: $success, $msg")
                _message.value = msg
                _loading.value = false
                if (success) {
                    _friends.value = list.filterNotNull()
                }
            }
        }
    }

    fun loadPendingRequests(userId: String? = auth.currentUser?.uid ) {
        val uid = userId ?: return
        _loading.value = true
        viewModelScope.launch {
            friendRepository.getPendingRequests(userId) { success, msg, list ->
                Log.d("FriendViewModel", "loadPendingRequests: $success, $msg")
                _message.value = msg
                _loading.value = false
                if (success) {
                    _pendingRequests.value = list.filterNotNull()
                }
            }
        }
    }
}