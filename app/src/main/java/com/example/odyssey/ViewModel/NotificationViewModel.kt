package com.example.odyssey.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.NotificationModel
import com.example.odyssey.repository.NotificationRepo
import com.example.odyssey.repository.NotificationRepoImpl

class NotificationViewModel(private val repository: NotificationRepo = NotificationRepoImpl()) : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>> = _notifications

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount

    private var isFetching = false

    fun fetchNotifications(userId: String) {
        if (isFetching) return
        isFetching = true
        repository.getNotifications(userId) { success, _, list ->
            if (success) {
                _notifications.postValue(list)
                _unreadCount.postValue(list.count { !it.isRead })
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        // Immediate local update for better UX
        val currentList = _notifications.value ?: emptyList()
        val updatedList = currentList.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
        _notifications.postValue(updatedList)
        _unreadCount.postValue(updatedList.count { !it.isRead })

        repository.markAsRead(notificationId) { _, _ -> }
    }

    fun sendNotification(notification: NotificationModel, callback: (Boolean, String) -> Unit = { _, _ -> }) {
        repository.sendNotification(notification, callback)
    }

    fun updateNotification(notificationId: String, updates: Map<String, Any>, callback: (Boolean, String) -> Unit = { _, _ -> }) {
        // If updates contains isRead=true, we can also update locally
        if (updates["isRead"] == true) {
            val currentList = _notifications.value ?: emptyList()
            val updatedList = currentList.map {
                if (it.id == notificationId) {
                    // This is a bit tricky since updates is a Map, but we know what we expect
                    it.copy(
                        isRead = true,
                        content = (updates["content"] as? String) ?: it.content,
                        type = (updates["type"] as? String) ?: it.type
                    )
                } else it
            }
            _notifications.postValue(updatedList)
            _unreadCount.postValue(updatedList.count { !it.isRead })
        }
        
        repository.updateNotification(notificationId, updates, callback)
    }

    fun markAllAsRead(userId: String) {
        // Immediate local update for better UX
        val currentList = _notifications.value ?: emptyList()
        val updatedList = currentList.map { it.copy(isRead = true) }
        _notifications.postValue(updatedList)
        _unreadCount.postValue(0)

        repository.markAllAsRead(userId) { _, _ -> }
    }
}
