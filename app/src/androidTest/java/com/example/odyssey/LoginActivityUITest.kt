package com.example.odyssey

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import org.hamcrest.Matchers.not

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
    fun testSuccessfulLogin_navigatesToSignUp() {
        // 1. Enter Email
        composeRule.onNodeWithTag("email")
            .performTextInput("sarita.nrish@example.com")

        // 2. Enter Password
        composeRule.onNodeWithTag("password")
            .performTextInput("nrishshrestha")

        // 3. Click "Sign Up" text (mapped via testTag in LoginActivity)
        composeRule.onNodeWithTag("register")
            .performClick()

        // 4. Verify navigation to SignUpActivity
        Intents.intended(hasComponent(SignUpActivity::class.java.name))
    }

    @Test
    fun testInvalidLogin_showsErrorMessage() {
        // Type invalid credentials
        composeRule.onNodeWithTag("email").performTextInput("wrong@user.com")
        composeRule.onNodeWithTag("password").performTextInput("123")

        // Click Login
        composeRule.onNodeWithTag("login_button").performClick()

        // Give the Toast a moment to appear
        Thread.sleep(500)

        // Verify Toast message
        onView(withText("Invalid email or password"))
            .inRoot(withDecorView(not(composeRule.activity.window.decorView)))
            .check(matches(isDisplayed()))
    }
}
