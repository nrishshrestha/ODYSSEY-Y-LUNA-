package com.example.odyssey.repository

import android.app.Application
import android.net.Uri
import com.example.odyssey.model.ChatMessage
import com.example.odyssey.model.ChatMessageType
import im.zego.zim.ZIM
import im.zego.zim.callback.ZIMEventHandler
import im.zego.zim.callback.ZIMLoggedInCallback
import im.zego.zim.callback.ZIMMediaMessageSentCallback
import im.zego.zim.callback.ZIMMessageSentCallback
import im.zego.zim.entity.*
import im.zego.zim.enums.ZIMConversationType
import im.zego.zim.enums.ZIMErrorCode
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

class ChatRepoImpl(private val application: Application) : ChatRepo {

    companion object {
        @Volatile
        private var zimInstance: ZIM? = null
        private const val APP_ID: Long = 1574279685
        private const val APP_SIGN: String = "6084145496e64daa590321bc79b33fb9698679491de8d58073cd57c8e6d1e748"

        private var staticMessageListener: ((List<ChatMessage>) -> Unit)? = null

        @Synchronized
        fun getInstance(application: Application): ZIM {
            if (zimInstance == null) {
                val config = ZIMAppConfig()
                config.appID = APP_ID
                config.appSign = APP_SIGN
                zimInstance = ZIM.create(config, application)

                zimInstance?.setEventHandler(object : ZIMEventHandler() {
                    override fun onPeerMessageReceived(
                        zim: ZIM,
                        messageList: ArrayList<ZIMMessage>,
                        info: ZIMMessageReceivedInfo,
                        fromUserID: String
                    ) {
                        Timber.d("Received Peer Message: ${messageList.size} from $fromUserID")
                        val list = messageList.map { mapZimMessageToChatMessage(it) }
                        staticMessageListener?.invoke(list)
                    }

                    override fun onConnectionStateChanged(
                        zim: ZIM?,
                        state: im.zego.zim.enums.ZIMConnectionState?,
                        event: im.zego.zim.enums.ZIMConnectionEvent?,
                        extendedData: org.json.JSONObject?
                    ) {
                        Timber.d("Connection State: $state, Event: $event")
                    }
                })
            }
            return zimInstance!!
        }

        private fun mapZimMessageToChatMessage(zimMessage: ZIMMessage): ChatMessage {
            var text = ""
            var type = ChatMessageType.TEXT
            var mediaUrl: String? = null
            var thumb: String? = null

            when (zimMessage) {
                is ZIMTextMessage -> {
                    text = zimMessage.message
                    type = ChatMessageType.TEXT
                }
                is ZIMImageMessage -> {
                    type = ChatMessageType.IMAGE
                    mediaUrl = zimMessage.fileDownloadUrl
                    thumb = zimMessage.thumbnailDownloadUrl
                    text = "[Image]"
                }
                is ZIMVideoMessage -> {
                    type = ChatMessageType.VIDEO
                    mediaUrl = zimMessage.fileDownloadUrl
                    thumb = zimMessage.videoFirstFrameDownloadUrl
                    text = "[Video]"
                }
                is ZIMFileMessage -> {
                    type = ChatMessageType.FILE
                    mediaUrl = zimMessage.fileDownloadUrl
                    text = zimMessage.fileName
                }
                else -> {}
            }

            return ChatMessage(
                senderId = zimMessage.senderUserID,
                message = text,
                timestamp = zimMessage.timestamp,
                messageId = zimMessage.messageID.toString(),
                type = type,
                mediaUrl = mediaUrl,
                thumbnailUrl = thumb
            )
        }

        fun clearMessageListener() {
            staticMessageListener = null
        }
    }

    private val zim: ZIM = getInstance(application)

    override fun login(userId: String, userName: String, callback: (Boolean, String) -> Unit) {
        val userInfo = ZIMUserInfo().apply {
            userID = userId
            this.userName = userName
        }

        Timber.d("Login attempt for $userId")
        zim.login(userInfo, object : ZIMLoggedInCallback {
            override fun onLoggedIn(errorInfo: ZIMError) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    Timber.d("Login successful for $userId")
                    callback(true, "Logged in")
                } else {
                    Timber.e("Login failed: ${errorInfo.message}")
                    callback(false, errorInfo.message)
                }
            }
        })
    }

    override fun logout() {
        try {
            zim.logout()
            clearMessageListener()
            Timber.d("Logged out successfully")
        } catch (e: Exception) {
            Timber.e(e, "Logout error")
        }
    }

    override fun sendMessage(toUserId: String, message: String, callback: (Boolean, String) -> Unit) {
        val zimMessage = ZIMTextMessage(message)
        val config = ZIMMessageSendConfig()

        zim.sendMessage(
            zimMessage,
            toUserId,
            ZIMConversationType.PEER,
            config,
            object : ZIMMessageSentCallback {
                override fun onMessageAttached(message: ZIMMessage?) {}
                override fun onMessageSent(message: ZIMMessage?, errorInfo: ZIMError) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        Timber.d("Message sent successfully")
                        callback(true, "Sent")
                    } else {
                        Timber.e("Message send failed: ${errorInfo.message}")
                        callback(false, errorInfo.message)
                    }
                }
            }
        )
    }

    override fun sendMediaMessage(toUserId: String, uri: Uri, type: ChatMessageType, callback: (Boolean, String) -> Unit) {
        val filePath = getFilePathFromUri(uri)
        if (filePath == null) {
            Timber.e("Invalid file path from URI")
            callback(false, "Invalid File Path")
            return
        }

        val zimMessage: ZIMMediaMessage = when (type) {
            ChatMessageType.IMAGE -> ZIMImageMessage(filePath)
            ChatMessageType.VIDEO -> ZIMVideoMessage(filePath, 0)
            else -> ZIMFileMessage(filePath)
        }

        val config = ZIMMessageSendConfig()
        zim.sendMediaMessage(
            zimMessage,
            toUserId,
            ZIMConversationType.PEER,
            config,
            object : ZIMMediaMessageSentCallback {
                override fun onMessageAttached(message: ZIMMediaMessage?) {}
                override fun onMediaUploadingProgress(message: ZIMMediaMessage?, currentFileSize: Long, totalFileSize: Long) {}
                override fun onMessageSent(message: ZIMMediaMessage?, errorInfo: ZIMError) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        Timber.d("Media sent successfully")
                        callback(true, "Media Sent")
                    } else {
                        Timber.e("Media send failed: ${errorInfo.message}")
                        callback(false, errorInfo.message)
                    }
                }
            }
        )
    }

    override fun receiveMessages(callback: (List<ChatMessage>) -> Unit) {
        staticMessageListener = callback
    }

    override fun queryHistory(userId: String, callback: (List<ChatMessage>) -> Unit) {
        val config = ZIMMessageQueryConfig().apply {
            count = 50
            reverse = true
        }

        zim.queryHistoryMessage(userId, ZIMConversationType.PEER, config) {
                _, _, messageList, errorInfo ->
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                Timber.d("History query successful: ${messageList.size} messages")
                val list = messageList.map { mapZimMessageToChatMessage(it) }
                callback(list)
            } else {
                Timber.e("History query failed: ${errorInfo.message}")
                callback(emptyList())
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val file = File(application.cacheDir, "upload_${System.currentTimeMillis()}")
            application.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "File path conversion error")
            null
        }
    }
}