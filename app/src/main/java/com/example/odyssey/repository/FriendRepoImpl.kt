package com.example.odyssey.repository

import com.example.odyssey.model.FriendModel
import com.example.odyssey.model.UserModel

class FriendRepoImpl: FriendRepo {
    override fun sendFriendRequest(
        fromUserId: String,
        toUserId: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun acceptFriendRequest(
        requestId: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun cancelFriendRequest(
        requestId: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeFriend(
        userId: String,
        friendId: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getFriends(
        userId: String,
        callback: (Boolean, String, List<FriendModel?>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getPendingRequests(
        userId: String,
        callback: (Boolean, String, List<FriendModel?>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getFriendUsers(
        userId: String,
        callback: (Boolean, String, List<UserModel?>) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}