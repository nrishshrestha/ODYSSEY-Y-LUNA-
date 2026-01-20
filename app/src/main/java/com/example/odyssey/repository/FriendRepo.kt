package com.example.odyssey.repository

import com.example.odyssey.model.FriendModel
import com.example.odyssey.model.UserModel

interface FriendRepo {
    // Send a friend request from current user to another user
    fun sendFriendRequest(
        fromUserId: String,
        toUserId: String,
        callback: (Boolean, String) -> Unit
    )

    // Accept a friend request
    fun acceptFriendRequest(
        requestId: String,
        callback: (Boolean, String) -> Unit
    )

    // Reject / cancel a friend request
    fun cancelFriendRequest(
        requestId: String,
        callback: (Boolean, String) -> Unit
    )

    // Remove a friend relation entirely
    fun removeFriend(
        userId: String,
        friendId: String,
        callback: (Boolean, String) -> Unit
    )

    // Get all friends (as FriendModel relations) of a user
    fun getFriends(
        userId: String,
        callback: (Boolean, String, List<FriendModel?>) -> Unit
    )

    // Get all pending friend requests for a user
    fun getPendingRequests(
        userId: String,
        callback: (Boolean, String, List<FriendModel?>) -> Unit
    )

    // Optionally, get full UserModel objects for a user's friends
    fun getFriendUsers(
        userId: String,
        callback: (Boolean, String, List<UserModel?>) -> Unit
    )

    fun getFollowersCount(userId: String, callback: (Int) -> Unit)
    fun getFollowingCount(userId: String, callback: (Int) -> Unit)
}