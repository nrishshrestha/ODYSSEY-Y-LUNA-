package com.example.odyssey.repository

import com.example.odyssey.model.FriendModel
import com.example.odyssey.model.UserModel
import com.google.firebase.database.*

class FriendRepoImpl(
    private val userRepo: UserRepo? = null
) : FriendRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Friend")

    override fun sendFriendRequest(
        fromUserId: String,
        toUserId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val newRef = ref.push()
        val requestId = newRef.key ?: ""

        if (requestId.isEmpty()) {
            callback(false, "Failed to generate request id")
            return
        }

        val model = FriendModel(
            id = requestId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )

        newRef.setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Friend request sent")
            } else {
                callback(false, it.exception?.message ?: "Failed to send friend request")
            }
        }
    }

    override fun acceptFriendRequest(
        requestId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(requestId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(false, "Request not found")
                        return
                    }

                    val current = snapshot.getValue(FriendModel::class.java)
                    if (current == null) {
                        callback(false, "Failed to parse request")
                        return
                    }

                    val updated = current.copy(status = "accepted")
                    ref.child(requestId).setValue(updated).addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback(true, "Friend request accepted")
                        } else {
                            callback(false, it.exception?.message ?: "Failed to accept request")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message)
                }
            })
    }

    override fun cancelFriendRequest(
        requestId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(requestId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Friend request cancelled")
            } else {
                callback(false, it.exception?.message ?: "Failed to cancel request")
            }
        }
    }

    override fun removeFriend(
        userId: String,
        friendId: String,
        callback: (Boolean, String) -> Unit
    ) {
        // Look up any accepted relation between the two users and delete it
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var targetKey: String? = null

                for (child in snapshot.children) {
                    val model = child.getValue(FriendModel::class.java)
                    if (model != null &&
                        model.status == "accepted" &&
                        (
                                (model.fromUserId == userId && model.toUserId == friendId) ||
                                        (model.fromUserId == friendId && model.toUserId == userId)
                                )
                    ) {
                        targetKey = model.id
                        break
                    }
                }

                if (targetKey == null) {
                    callback(false, "Friend relation not found")
                    return
                }

                ref.child(targetKey).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        callback(true, "Friend removed")
                    } else {
                        callback(false, it.exception?.message ?: "Failed to remove friend")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun getFriends(
        userId: String,
        callback: (Boolean, String, List<FriendModel?>) -> Unit
    ) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = arrayListOf<FriendModel?>()
                for (child in snapshot.children) {
                    val model = child.getValue(FriendModel::class.java)
                    if (model != null &&
                        model.status == "accepted" &&
                        (model.fromUserId == userId || model.toUserId == userId)
                    ) {
                        list.add(model)
                    }
                }
                callback(true, "Friends fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getPendingRequests(
        userId: String,
        callback: (Boolean, String, List<FriendModel?>) -> Unit
    ) {
        // Requests where this user is the receiver and status is pending
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = arrayListOf<FriendModel?>()
                for (child in snapshot.children) {
                    val model = child.getValue(FriendModel::class.java)
                    if (model != null &&
                        model.status == "pending" &&
                        model.toUserId == userId
                    ) {
                        list.add(model)
                    }
                }
                callback(true, "Pending requests fetched", list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getFriendUsers(
        userId: String,
        callback: (Boolean, String, List<UserModel?>) -> Unit
    ) {
        if (userRepo == null) {
            callback(false, "UserRepository not provided", emptyList())
            return
        }

        // 1) get all accepted friend relations for this user
        getFriends(userId) { success, msg, relations ->
            if (!success) {
                callback(false, msg, emptyList())
                return@getFriends
            }

            val friendIds = relations.mapNotNull { relation ->
                relation?.let {
                    if (it.fromUserId == userId) it.toUserId else it.fromUserId
                }
            }.distinct()

            if (friendIds.isEmpty()) {
                callback(true, "No friends found", emptyList())
                return@getFriends
            }

            val users = arrayListOf<UserModel?>()
            var completed = 0
            var anyError = false
            var errorMessage = ""

            friendIds.forEach { fid ->
                userRepo.getUserByID(fid) { ok, msg2, user ->
                    completed++

                    if (!ok && !anyError) {
                        anyError = true
                        errorMessage = msg2
                    } else {
                        users.add(user)
                    }

                    if (completed == friendIds.size) {
                        if (anyError) {
                            callback(false, errorMessage, users)
                        } else {
                            callback(true, "Friend users fetched", users)
                        }
                    }
                }
            }
        }
    }
}