package com.example.odyssey.repository

import com.example.odyssey.model.NotificationModel
import com.google.firebase.database.*

class NotificationRepoImpl : NotificationRepo {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Notifications")

    override fun sendNotification(notification: NotificationModel, callback: (Boolean, String) -> Unit) {
        val id = ref.push().key ?: return callback(false, "Failed to generate ID")
        val finalNotification = notification.copy(id = id)
        ref.child(id).setValue(finalNotification).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Notification sent")
            else callback(false, it.exception?.message ?: "Failed to send")
        }
    }

    override fun getNotifications(userId: String, callback: (Boolean, String, List<NotificationModel>) -> Unit) {
        ref.orderByChild("toUserId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotificationModel>()
                for (child in snapshot.children) {
                    child.getValue(NotificationModel::class.java)?.let { list.add(it) }
                }
                callback(true, "Fetched", list.sortedByDescending { it.timestamp })
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun markAsRead(notificationId: String, callback: (Boolean, String) -> Unit) {
        // Changed "read" to "isRead" to match NotificationModel property name exactly
        ref.child(notificationId).child("isRead").setValue(true).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Marked as read")
            else callback(false, it.exception?.message ?: "Failed")
        }
    }

    override fun updateNotification(notificationId: String, updates: Map<String, Any>, callback: (Boolean, String) -> Unit) {
        ref.child(notificationId).updateChildren(updates).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Notification updated")
            else callback(false, it.exception?.message ?: "Failed to update")
        }
    }

    override fun markAllAsRead(userId: String, callback: (Boolean, String) -> Unit) {
        ref.orderByChild("toUserId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = mutableMapOf<String, Any>()
                for (child in snapshot.children) {
                    val isRead = child.child("isRead").getValue(Boolean::class.java) ?: false
                    if (!isRead) {
                        updates["${child.key}/isRead"] = true
                    }
                }
                if (updates.isNotEmpty()) {
                    ref.updateChildren(updates).addOnCompleteListener {
                        if (it.isSuccessful) callback(true, "All marked as read")
                        else callback(false, it.exception?.message ?: "Failed")
                    }
                } else {
                    callback(true, "No unread notifications")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }
}
