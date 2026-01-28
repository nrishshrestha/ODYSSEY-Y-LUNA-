package com.example.odyssey

import com.example.odyssey.ViewModel.UserViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.UserRepo
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserViewModelTest {

    @Test
    fun login_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login success")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.login("test@gmail.com", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Login success", messageResult)

        verify(repo).login(eq("test@gmail.com"), eq("123456"), any())
    }

    @Test
    fun login_failure_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Invalid email or password")
            null
        }.`when`(repo).login(any(), any(), any())

        var successResult = true 
        var messageResult = ""

        viewModel.login("wrong@gmail.com", "wrongpass") { success, msg ->
            successResult = success
            messageResult = msg
        }

        Assert.assertFalse(successResult)
        Assert.assertEquals("Invalid email or password", messageResult)
    }

    @Test
    fun signup_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "User created successfully", "")
            null
        }.`when`(repo).register(any(), any(), any())

        var resultMsg = ""
        viewModel.register("newuser@gmail.com", "password123") { success, msg, _ ->
            resultMsg = msg
        }

        Assert.assertEquals("User created successfully", resultMsg)
    }

    @Test
    fun forget_password_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Reset link sent")
            null
        }.`when`(repo).forgetPassword(eq("test@gmail.com"), any())

        var message = ""
        viewModel.forgetPassword("test@gmail.com") { _, msg -> message = msg }

        assertEquals("Reset link sent", message)
    }

    @Test
    fun edit_profile_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)
        val mockUser = UserModel(userId = "123", firstName = "New", lastName = "Name")

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Profile Updated")
            null
        }.`when`(repo).editProfile(eq("123"), any(), any())

        var successResult = false
        viewModel.editProfile("123", mockUser) { success, _ ->
            successResult = success
        }

        Assert.assertTrue(successResult)
    }
}
