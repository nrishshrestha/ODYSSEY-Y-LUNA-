package com.example.odyssey.repository

import android.app.Application
import com.example.odyssey.model.ChatMessage
import im.zego.zim.ZIM
import im.zego.zim.callback.ZIMEventHandler
import im.zego.zim.callback.ZIMLoggedInCallback
import im.zego.zim.callback.ZIMMessageSentCallback
import im.zego.zim.entity.*
import im.zego.zim.enums.ZIMConversationType
import im.zego.zim.enums.ZIMErrorCode
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

    override fun receiveMessages(callback: (List<ChatMessage>) -> Unit) {
        zim?.setEventHandler(object : ZIMEventHandler() {
            override fun onPeerMessageReceived(
                zim: ZIM,
                messageList: ArrayList<ZIMMessage>,
                info: ZIMMessageReceivedInfo,
                fromUserID: String
            ) {
                val list = messageList?.filterIsInstance<ZIMTextMessage>()?.map {
                    ChatMessage(
                        senderId = it.senderUserID,
                        message = it.message,
                        timestamp = it.timestamp,
                        messageId = it.messageID.toString()
                    )
                } ?: emptyList()
                callback(list)
            }
        })
    }

    override fun queryHistory(userId: String, callback: (List<ChatMessage>) -> Unit) {
        val config = ZIMMessageQueryConfig()
        config.count = 20
        config.reverse = true
        
        zim?.queryHistoryMessage(userId, ZIMConversationType.PEER, config) { _, _, messageList, errorInfo ->
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                val list = messageList.filterIsInstance<ZIMTextMessage>().map {
                    ChatMessage(
                        senderId = it.senderUserID,
                        message = it.message,
                        timestamp = it.timestamp,
                        messageId = it.messageID.toString()
                    )
                }
                callback(list)
            }
        }
    }
}
