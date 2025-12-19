package com.example.odyssey

import com.example.odyssey.model.ProfileModel


interface ProfileRepository {

    // Fetch current user's profile
    fun getProfile(
        userId: String,
        callback: (Boolean, String, ProfileModel?) -> Unit
    )

    // Update profile details (name, phone, image, etc.)
    fun updateProfile(
        userId: String,
        userModel: ProfileModel,
        callback: (Boolean, String) -> Unit
    )

    // Update profile image only
    fun updateProfileImage(
        userId: String,
        imageUrl: String,
        callback: (Boolean, String) -> Unit
    )

    // Change email
    fun updateEmail(
        userId: String,
        newEmail: String,
        callback: (Boolean, String) -> Unit
    )

    // Change password
    fun updatePassword(
        newPassword: String,
        callback: (Boolean, String) -> Unit
    )

    // Logout user
    fun logout(
        callback: (Boolean, String) -> Unit
    )
}
