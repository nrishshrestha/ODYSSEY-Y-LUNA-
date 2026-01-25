package com.example.odyssey.repository

import android.app.Application
import android.content.Context
import com.example.odyssey.model.UserModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.zegocloud.zimkit.services.ZIMKit
import com.zegocloud.zimkit.services.ZIMKitConfig
import com.zegocloud.zimkit.common.ZIMKitRouter
import com.zegocloud.zimkit.common.enums.ZIMKitConversationType

class ChatRepoImpl (private val application: Application) : ChatRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("Chat")
    private val notificationRepo: NotificationRepo = NotificationRepoImpl()

    private val appID: Long = 110418707
    private val appSign = "963ba596976489846f7c10a7217bc0c42754dcd9da1398276f90246a2faeb95d"

    override fun initZegoCloudUser(
        userId: String,
        userName: String,
        callback: (Boolean, String) -> Unit
    ) {
        // 1. Initialize ZIMKit
        ZIMKit.initWith(application, appID, appSign, ZIMKitConfig())
        
        // 2. Connect User
        ZIMKit.connectUser(userId, userName, "") { error ->
            if (error.code == com.zegocloud.zimkit.services.model.ZIMKitCode.SUCCESS) {
                callback(true, "ZegoCloud connected successfully")
            } else {
                callback(false, "ZegoCloud connection failed: ${error.message}")
            }
        }
    }

    override fun startOneOnOneChat(
        context: Context,
        targetUserModel: UserModel,
        callback: (Boolean, String) -> Unit
    ) {
        // Start one-on-one chat using ZIMKitRouter
        ZIMKitRouter.toMessageActivity(
            context,
            targetUserModel.userId,
            ZIMKitConversationType.ZIMKitConversationTypePeer
        )
        callback(true, "Chat started")
    }

    override fun logoutZegoCloud() {
        ZIMKit.disconnectUser()
    }
}
