package com.example.odyssey.repository

import android.content.Context
import android.net.Uri
import com.example.odyssey.model.UserModel

interface UserRepository {
    fun login(email:String,password:String,callback:(Boolean,String) -> Unit)

    fun register(email:String,password:String,callback:(Boolean,String,String) -> Unit)

    fun forgetPassword(email: String,callback: (Boolean, String) -> Unit)

    fun addUserToDatabase(userId:String, model: UserModel, callback: (Boolean, String) -> Unit)

    fun getUserByID(userId:String,callback: (Boolean, String, UserModel?) -> Unit)

    fun getAllUser(callback: (Boolean, String, List<UserModel?>) -> Unit)

    fun editProfile(userId: String,model: UserModel,callback: (Boolean, String) -> Unit)

    fun deleteAccount(userId: String,callback: (Boolean, String) -> Unit)
    fun uploadImageToCloudinary(context: Context, imageUri: Uri)
    fun updateProfileImage(imageUrl: String)
}