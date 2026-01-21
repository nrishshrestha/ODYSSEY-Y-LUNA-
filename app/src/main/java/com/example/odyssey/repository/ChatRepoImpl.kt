package com.example.odyssey.repository

import android.app.Application
import android.net.Uri
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.model.ChatMessageType
import im.zego.zim.ZIM
import im.zego.zim.callback.ZIMEventHandler
import im.zego.zim.callback.ZIMLoggedInCallback
import im.zego.zim.callback.ZIMMessageSentCallback
import im.zego.zim.entity.*
import im.zego.zim.enums.ZIMConversationType
import im.zego.zim.enums.ZIMErrorCode
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

class ChatRepoImpl(private val application: Application) : ChatRepo {
    private var zim: ZIM? = null
    
    // Placeholder values - user should replace these with actual ZegoCloud credentials
    private val appID: Long = 1574279685
    private val appSign: String = "6084145496e64daa590321bc79b33fb9698679491de8d58073cd57c8e6d1e748"

    init {
        val config = ZIMAppConfig()
        config.appID = this.appID
        config.appSign = this.appSign
        zim = ZIM.create(config, application)
    }

    override fun login(userId: String, userName: String, callback: (Boolean, String) -> Unit) {
        val userInfo = ZIMUserInfo()
        userInfo.userID = userId
        userInfo.userName = userName
        
        zim?.login(userInfo, object : ZIMLoggedInCallback {
            override fun onLoggedIn(errorInfo: ZIMError) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    callback(true, "Logged in to ZIM")
                } else {
                    callback(false, errorInfo.message)
                }
            }
        })
    }

    override fun logout() {
        zim?.logout()
    }

    override fun sendMessage(toUserId: String, message: String, callback: (Boolean, String) -> Unit) {
        val zimMessage = ZIMTextMessage(message)
        val config = ZIMMessageSendConfig()
        
        zim?.sendMessage(zimMessage, toUserId, ZIMConversationType.PEER, config, object : ZIMMessageSentCallback {
            override fun onMessageAttached(message: ZIMMessage?) {}
            override fun onMessageSent(message: ZIMMessage?, errorInfo: ZIMError) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    callback(true, "Sent")
                } else {
                    callback(false, errorInfo.message)
                }
            }
        })
    }

    override fun sendMediaMessage(toUserId: String, uri: Uri, type: ChatMessageType, callback: (Boolean, String) -> Unit) {
        val filePath = getFilePathFromUri(uri) ?: return callback(false, "Invalid File Path")
        
        val zimMessage: ZIMMediaMessage = when (type) {
            ChatMessageType.IMAGE -> ZIMImageMessage(filePath)
            ChatMessageType.VIDEO -> ZIMVideoMessage(filePath)
            else -> ZIMFileMessage(filePath)
        }

        val config = ZIMMessageSendConfig()
        zim?.sendMediaMessage(zimMessage, toUserId, ZIMConversationType.PEER, config, object : ZIMMessageSentCallback {
            override fun onMessageAttached(message: ZIMMessage?) {}
            override fun onMessageSent(message: ZIMMessage?, errorInfo: ZIMError) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    callback(true, "Media Sent")
                } else {
                    callback(false, errorInfo.message)
                }
            }
        })
    }

    override fun receiveMessages(callback: (List<ChatMessage>) -> Unit) {
        zim?.setEventHandler(object : ZIMEventHandler() {
            override fun onPeerMessageReceived(
                zim: ZIM,
                messageList: ArrayList<ZIMMessage>,
                info: ZIMMessageReceivedInfo,
                fromUserID: String
            ) {
                val list = messageList.map { mapZimMessageToChatMessage(it) }
                callback(list)
            }
        })
    }

    override fun queryHistory(userId: String, callback: (List<ChatMessage>) -> Unit) {
        val config = ZIMMessageQueryConfig()
        config.count = 50
        config.reverse = true
        
        zim?.queryHistoryMessage(userId, ZIMConversationType.PEER, config) { _, _, messageList, errorInfo ->
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                val list = messageList.map { mapZimMessageToChatMessage(it) }
                callback(list)
            }
        }
    }

    private fun mapZimMessageToChatMessage(zimMessage: ZIMMessage): ChatMessage {
        var messageText = ""
        var type = ChatMessageType.TEXT
        var mediaUrl: String? = null
        var thumbnailUrl: String? = null

        when (zimMessage) {
            is ZIMTextMessage -> {
                messageText = zimMessage.message
                type = ChatMessageType.TEXT
            }
            is ZIMImageMessage -> {
                type = ChatMessageType.IMAGE
                mediaUrl = zimMessage.fileDownloadUrl
                thumbnailUrl = zimMessage.thumbnailDownloadUrl
                messageText = "[Image]"
            }
            is ZIMVideoMessage -> {
                type = ChatMessageType.VIDEO
                mediaUrl = zimMessage.fileDownloadUrl
                thumbnailUrl = zimMessage.videoFirstFrameDownloadUrl
                messageText = "[Video]"
            }
            is ZIMFileMessage -> {
                type = ChatMessageType.FILE
                mediaUrl = zimMessage.fileDownloadUrl
                messageText = zimMessage.fileName
            }
            else -> {}
        }

        return ChatMessage(
            senderId = zimMessage.senderUserID,
            message = messageText,
            timestamp = zimMessage.timestamp,
            messageId = zimMessage.messageID.toString(),
            type = type,
            mediaUrl = mediaUrl,
            thumbnailUrl = thumbnailUrl
        )
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val contentResolver = application.contentResolver
            val fileName = "temp_file_" + System.currentTimeMillis()
            val file = File(application.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
