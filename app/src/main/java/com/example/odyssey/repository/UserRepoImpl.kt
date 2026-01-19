package com.example.odyssey.repository


import android.content.Context
import android.net.Uri
import com.example.odyssey.model.UserModel
import com.example.odyssey.utils.CloudinaryManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


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
        ref.child(userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Account deleted successfully")
            } else {
                callback(false, it.exception?.message ?: "Failed to delete account")
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