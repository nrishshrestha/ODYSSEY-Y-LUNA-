package com.example.odyssey.repository

import android.content.Context
import com.example.odyssey.model.UserModel

interface ChatRepo {

    fun initZegoCloudUser(userId: String, userName: String, callback: (Boolean, String) -> Unit)

    fun startOneOnOneChat(context: Context, targetUserModel: UserModel, callback: (Boolean, String) -> Unit)

    fun logoutZegoCloud()
}
