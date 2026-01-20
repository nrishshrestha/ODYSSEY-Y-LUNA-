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

    fun fetchNotifications(userId: String) {
        repository.getNotifications(userId) { success, _, list ->
            if (success) {
                _notifications.postValue(list)
                _unreadCount.postValue(list.count { !it.isRead })
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        repository.markAsRead(notificationId) { _, _ -> }
    }

    fun sendNotification(notification: NotificationModel, callback: (Boolean, String) -> Unit = { _, _ -> }) {
        repository.sendNotification(notification, callback)
    }

    fun updateNotification(notificationId: String, updates: Map<String, Any>, callback: (Boolean, String) -> Unit = { _, _ -> }) {
        repository.updateNotification(notificationId, updates, callback)
    }

    fun markAllAsRead(userId: String) {
        repository.markAllAsRead(userId) { _, _ -> }
    }
}
