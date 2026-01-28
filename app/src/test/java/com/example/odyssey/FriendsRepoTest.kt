package com.example.odyssey

import com.example.odyssey.model.FriendModel
import com.example.odyssey.repository.FriendRepo
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class FriendRepoTest {

    @Test
    fun fetch_friends_list_test() {
        val repo = mock<FriendRepo>()

        val mockFriends = listOf(
            FriendModel(id = "friend1", fromUserId = "user1", toUserId = "user2"),
            FriendModel(id = "friend2", fromUserId = "user3", toUserId = "user1")
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, List<FriendModel?>) -> Unit>(1)
            callback(true, "Success", mockFriends)
            null
        }.`when`(repo).getFriends(any(), any())

        repo.getFriends("user1") { success, message, friends ->
            Assert.assertTrue(success)
            Assert.assertEquals("Success", message)
            Assert.assertNotNull(friends)
            Assert.assertEquals(2, friends?.size)
            Assert.assertEquals("user1", friends?.get(0)?.fromUserId)
        }

        verify(repo).getFriends(eq("user1"), any())
    }

    @Test
    fun fetch_friends_network_error_test() {
        val repo = mock<FriendRepo>()

        // Simulate a network timeout or error
        doAnswer { invocation ->
            // The callback is at index 1
            val callback = invocation.getArgument<(Boolean, String, List<FriendModel?>) -> Unit>(1)
            callback(false, "Connection Timeout", emptyList())
            null
        }.`when`(repo).getFriends(any(), any())

        repo.getFriends("user1") { success, message, friends ->
            Assert.assertFalse(success)
            Assert.assertEquals("Connection Timeout", message)
            Assert.assertTrue(friends.isEmpty())
        }
    }
}
