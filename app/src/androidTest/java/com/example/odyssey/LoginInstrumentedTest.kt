package com.example.odyssey


import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import org.junit.After

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {


        composeRule.onNodeWithTag("email")
            .performTextInput("sarita.nrish@gmail.com")

        composeRule.onNodeWithTag("password")
            .performTextInput("nrishshrestha")

        composeRule.onNodeWithTag("login")
            .performClick()


        Intents.intended(hasComponent(DashboardActivity::class.java.name))
    }

}