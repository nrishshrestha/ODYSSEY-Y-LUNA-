package com.example.odyssey

import com.example.odyssey.repository.UserRepo
import com.example.odyssey.ViewModel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq



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

    }
