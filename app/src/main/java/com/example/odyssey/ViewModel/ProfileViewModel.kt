package com.example.odyssey.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.UserRepoImpl
import com.example.odyssey.repository.UserRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class   ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")
    private val userRepository: UserRepo = UserRepoImpl()

    // Use a StateFlow to hold the user data, so Composables can observe it
    private val _user = MutableStateFlow<UserModel?>(null)
    val user: StateFlow<UserModel?> = _user

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                Log.d("ProfileViewModel", "User is logged in. Fetching data for UID: $userId")
                try {
                    val snapshot = database.child(userId).get().await()

                    if (snapshot.exists()) {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        if (userModel != null) {
                            _user.value = userModel
                            Log.d(
                                "ProfileViewModel",
                                "Successfully fetched user data. ImageUrl: ${userModel.imageUrl}"
                            )
                        } else {
                            Log.e(
                                "ProfileViewModel",
                                "Failed to parse snapshot into UserModel. Check data structure."
                            )
                        }
                    } else {
                        Log.e(
                            "ProfileViewModel",
                            "No data found at database path for user: $userId"
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "An error occurred while fetching user data.", e)
                    _user.value = null
                }
            } else {
                Log.d(
                    "ProfileViewModel",
                    "No user is currently logged in. (auth.currentUser is null)"
                )
            }
        }
    }

    // Profile image upload
    fun uploadProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uploading.value = true
            _uploadError.value = null

            try {
                Log.d("ProfileViewModel", "Starting image upload")
                userRepository.uploadImageToCloudinary(context, uri)
                Log.d("ProfileViewModel", "Image upload complete, refreshing user data")

                // Refresh user data to get the new image URL
                fetchCurrentUser()

                Log.d("ProfileViewModel", "Upload successful")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Upload failed: ${e.message}", e)
                _uploadError.value = e.message ?: "Upload failed"
            } finally {
                _uploading.value = false
            }
        }
    }

    // Optional: Method to manually refresh user data
    fun refreshUserData() {
        fetchCurrentUser()
    }
}