package com.example.odyssey.repository


import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.odyssey.model.UserModel
import com.example.odyssey.utils.CloudinaryManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserRepoImpl : UserRepo {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("User")

    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Login Successful")
                } else {
                    callback(false, it.exception?.message ?: "Unknown login error")
                }
            }
    }

    override fun register(
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Registration Successful", auth.currentUser?.uid ?: "")
                } else {
                    callback(false, it.exception?.message ?: "Unknown registration error", "")
                }
            }
    }

    override fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Verification email sent to $email")
                } else {
                    callback(
                        false,
                        it.exception?.message ?: "Unknown error sending password reset email"
                    )
                }
            }
    }

    override fun addUserToDatabase(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Registration successful")
            } else {
                callback(false, it.exception?.message ?: "Failed to add user to database")
            }
        }
    }

    override fun getUserByID(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit
    ) {
        ref.child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            callback(true, "Profile fetched", user)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "${error.message}", null)
                }
            })
    }

    override fun getAllUser(callback: (Boolean, String, List<UserModel?>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allUsers = arrayListOf<UserModel>()
                for (childSnapshot in snapshot.children) {
                    val user = childSnapshot.getValue(UserModel::class.java)
                    if (user != null) {
                        allUsers.add(user)
                    }
                }
                callback(true, "Users fetched successfully", allUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun editProfile(
        userId: String,
        model: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        val userMap = model.toMap()
        ref.child(userId).updateChildren(userMap).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Profile updated successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to update profile")
            }
        }
    }

    override fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        Log.d("UserRepoImpl", "Starting comprehensive account deletion for user: $userId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Track deletion progress
                val deletionTasks = mutableListOf<String>()

                // 1. Delete all Friend relationships
                try {
                    val friendRef = database.getReference("Friend")
                    val friendSnapshot = friendRef.get().await()

                    val friendDeletions = mutableListOf<String>()
                    for (childSnapshot in friendSnapshot.children) {
                        val fromUserId = childSnapshot.child("fromUserId").getValue(String::class.java)
                        val toUserId = childSnapshot.child("toUserId").getValue(String::class.java)

                        if (fromUserId == userId || toUserId == userId) {
                            childSnapshot.ref.removeValue().await()
                            friendDeletions.add(childSnapshot.key ?: "")
                        }
                    }
                    Log.d("UserRepoImpl", "Deleted ${friendDeletions.size} friend relationships")
                    deletionTasks.add("Friends: ${friendDeletions.size}")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting friends: ${e.message}", e)
                }

                // 2. Delete all Notifications
                try {
                    val notificationRef = database.getReference("Notifications")
                    val notificationSnapshot = notificationRef.get().await()

                    val notificationDeletions = mutableListOf<String>()
                    for (childSnapshot in notificationSnapshot.children) {
                        val toUserId = childSnapshot.child("toUserId").getValue(String::class.java)
                        val fromUserId = childSnapshot.child("fromUserId").getValue(String::class.java)

                        if (toUserId == userId || fromUserId == userId) {
                            childSnapshot.ref.removeValue().await()
                            notificationDeletions.add(childSnapshot.key ?: "")
                        }
                    }
                    Log.d("UserRepoImpl", "Deleted ${notificationDeletions.size} notifications")
                    deletionTasks.add("Notifications: ${notificationDeletions.size}")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting notifications: ${e.message}", e)
                }

                // 3. Delete all Chats (with various possible key formats)
                try {
                    val chatRef = database.getReference("Chats")
                    val chatSnapshot = chatRef.get().await()

                    val chatDeletions = mutableListOf<String>()
                    for (childSnapshot in chatSnapshot.children) {
                        val chatKey = childSnapshot.key ?: continue

                        // Check if chat key contains the userId in any format
                        // Common formats: "userId1_userId2", "userId1-userId2", or as a field
                        if (chatKey.contains(userId)) {
                            childSnapshot.ref.removeValue().await()
                            chatDeletions.add(chatKey)
                            continue
                        }

                        // Also check for userId in chat data fields
                        val participants = childSnapshot.child("participants")
                        for (participant in participants.children) {
                            if (participant.getValue(String::class.java) == userId) {
                                childSnapshot.ref.removeValue().await()
                                chatDeletions.add(chatKey)
                                break
                            }
                        }
                    }
                    Log.d("UserRepoImpl", "Deleted ${chatDeletions.size} chat conversations")
                    deletionTasks.add("Chats: ${chatDeletions.size}")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting chats: ${e.message}", e)
                }

                // 4. Delete all Routes created by user
                try {
                    val routeRef = database.getReference("routes")
                    val routeSnapshot = routeRef.get().await()

                    val routeDeletions = mutableListOf<String>()
                    for (childSnapshot in routeSnapshot.children) {
                        val routeUserId = childSnapshot.child("userId").getValue(String::class.java)

                        if (routeUserId == userId) {
                            childSnapshot.ref.removeValue().await()
                            routeDeletions.add(childSnapshot.key ?: "")
                        }
                    }
                    Log.d("UserRepoImpl", "Deleted ${routeDeletions.size} routes")
                    deletionTasks.add("Routes: ${routeDeletions.size}")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting routes: ${e.message}", e)
                }

                // 5. Delete all Posts created by user (if exists)
                try {
                    val postRef = database.getReference("Posts")
                    val postSnapshot = postRef.get().await()

                    val postDeletions = mutableListOf<String>()
                    for (childSnapshot in postSnapshot.children) {
                        val postUserId = childSnapshot.child("userId").getValue(String::class.java)

                        if (postUserId == userId) {
                            childSnapshot.ref.removeValue().await()
                            postDeletions.add(childSnapshot.key ?: "")
                        }
                    }
                    Log.d("UserRepoImpl", "Deleted ${postDeletions.size} posts")
                    deletionTasks.add("Posts: ${postDeletions.size}")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting posts: ${e.message}", e)
                }

                // 6. Delete all Comments made by user (if exists)
                try {
                    val commentRef = database.getReference("Comments")
                    val commentSnapshot = commentRef.get().await()

                    val commentDeletions = mutableListOf<String>()
                    for (childSnapshot in commentSnapshot.children) {
                        val commentUserId = childSnapshot.child("userId").getValue(String::class.java)

                        if (commentUserId == userId) {
                            childSnapshot.ref.removeValue().await()
                            commentDeletions.add(childSnapshot.key ?: "")
                        }
                    }
                    Log.d("UserRepoImpl", "Deleted ${commentDeletions.size} comments")
                    deletionTasks.add("Comments: ${commentDeletions.size}")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting comments: ${e.message}", e)
                }

                // 7. Delete all Likes made by user (if exists)
                try {
                    val likeRef = database.getReference("Likes")
                    val likeSnapshot = likeRef.get().await()

                    val likeDeletions = mutableListOf<String>()
                    for (childSnapshot in likeSnapshot.children) {
                        val likeUserId = childSnapshot.child("userId").getValue(String::class.java)

                        if (likeUserId == userId) {
                            childSnapshot.ref.removeValue().await()
                            likeDeletions.add(childSnapshot.key ?: "")
                        }
                    }
                    Log.d("UserRepoImpl", "Deleted ${likeDeletions.size} likes")
                    deletionTasks.add("Likes: ${likeDeletions.size}")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting likes: ${e.message}", e)
                }

                // 8. Finally, delete the User profile
                try {
                    ref.child(userId).removeValue().await()
                    Log.d("UserRepoImpl", "Deleted user profile")
                    deletionTasks.add("User Profile: Deleted")
                } catch (e: Exception) {
                    Log.e("UserRepoImpl", "Error deleting user profile: ${e.message}", e)
                    throw e
                }

                Log.d("UserRepoImpl", "Account deletion completed successfully. Summary: ${deletionTasks.joinToString(", ")}")
                callback(true, "Account deleted successfully")

            } catch (e: Exception) {
                Log.e("UserRepoImpl", "Critical error during account deletion: ${e.message}", e)
                callback(false, "Failed to delete account: ${e.message}")
            }
        }
    }

    override fun uploadImageToCloudinary(context: Context, imageUri: Uri) {
        android.util.Log.d("UserRepoImpl", "=== Starting upload process ===")
        CloudinaryManager.uploadImage(
            context,
            imageUri,
            onSuccess = { imageUrl ->
                android.util.Log.d("UserRepoImpl", "Cloudinary success! URL: $imageUrl")
                updateProfileImage(imageUrl)
            },
            onError = { exception ->
                android.util.Log.e(
                    "UserRepoImpl",
                    "Cloudinary upload failed: ${exception.message}",
                    exception
                )
            }
        )
    }

    override fun updateProfileImage(imageUrl: String) {
        android.util.Log.d("UserRepoImpl", "=== Updating Firebase ===")
        val userId = auth.currentUser?.uid
        android.util.Log.d("UserRepoImpl", "User ID: $userId")
        android.util.Log.d("UserRepoImpl", "Image URL: $imageUrl")

        if (userId != null) {
            ref.child(userId).child("imageUrl").setValue(imageUrl)
                .addOnSuccessListener {
                    android.util.Log.d("UserRepoImpl", "Firebase imageUrl updated successfully!")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("UserRepoImpl", "Failed to update imageUrl: ${e.message}", e)
                }
        } else {
            android.util.Log.e("UserRepoImpl", "No user logged in!")
        }
    }
}