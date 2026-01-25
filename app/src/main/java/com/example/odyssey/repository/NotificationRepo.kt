package com.example.odyssey.repository

import com.example.odyssey.model.NotificationModel

interface NotificationRepo {
    fun sendNotification(notification: NotificationModel, callback: (Boolean, String) -> Unit)
    fun getNotifications(userId: String, callback: (Boolean, String, List<NotificationModel>) -> Unit)
    fun markAsRead(notificationId: String, callback: (Boolean, String) -> Unit)
    fun updateNotification(notificationId: String, updates: Map<String, Any>, callback: (Boolean, String) -> Unit)
    fun markAllAsRead(userId: String, callback: (Boolean, String) -> Unit)
}
