package com.example.odyssey

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenUITest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun testSearchFunctionality() {
        // 1. Perform Login
        // Assuming valid credentials for testing purposes.
        composeRule.onNodeWithTag("email").performTextInput("sarita.nrish@gmail.com")
        composeRule.onNodeWithTag("password").performTextInput("nrihshshrestha")
        composeRule.onNodeWithTag("login_button").performClick()

        // 2. WAIT for the Dashboard to appear (specifically the search icon)
        // This prevents the test from failing if the login API takes a second
        composeRule.waitUntil(timeoutMillis = 10000) { // Increased timeout for stability
            composeRule
                .onAllNodesWithTag("search_icon")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Click Search Icon in Dashboard Header
        composeRule.onNodeWithTag("search_icon").performClick()

        // 4. Interaction with the Search Bar
        // We click first to ensure focus, then type
        composeRule.onNodeWithTag("search_bar")
            .performClick()
            .performTextInput("Avsh Dulal")

        // 5. Verification
        composeRule.onNodeWithTag("search_bar")
            .assertTextContains("Avsh Dulal")
    }
}
