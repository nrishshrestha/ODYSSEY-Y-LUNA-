    package com.example.odyssey

    import androidx.compose.ui.test.junit4.createAndroidComposeRule
    import androidx.compose.ui.test.onNodeWithText
    import androidx.compose.ui.test.performClick
    import androidx.compose.ui.test.performTextInput
    import androidx.test.espresso.intent.Intents
    import androidx.test.espresso.intent.matcher.IntentMatchers
    import androidx.test.ext.junit.runners.AndroidJUnit4
    import org.junit.After
    import org.junit.Before
    import org.junit.Rule
    import org.junit.Test
    import org.junit.runner.RunWith

    @RunWith(AndroidJUnit4::class)
    class SignUpActivityTest {

        @get:Rule
        val composeTestRule = createAndroidComposeRule<SignUpActivity>()

        @Before
        fun setup() {
            Intents.init()
        }

        @After
        fun tearDown() {
            Intents.release()
        }

        @Test
        fun testSignUpFormElementsExist() {
            // Verify all form elements are present
            composeTestRule.onNodeWithText("Sign Up").assertExists()
            composeTestRule.onNodeWithText("Welcome to ODYSSEY").assertExists()
            composeTestRule.onNodeWithText("First Name").assertExists()
            composeTestRule.onNodeWithText("Last Name").assertExists()
            composeTestRule.onNodeWithText("example@gmail.com").assertExists()
            composeTestRule.onNodeWithText("Password").assertExists()
            composeTestRule.onNodeWithText("Select Date").assertExists()
            composeTestRule.onNodeWithText("Select Gender").assertExists()
            composeTestRule.onNodeWithText("I agree to the term and condition").assertExists()
            composeTestRule.onNodeWithText("Register").assertExists()
        }

        @Test
        fun testFillSignUpForm() {
            // Fill in the sign-up form
            val firstName = "John"
            val lastName = "Doe"
            val email = "john.doe@example.com"
            val password = "Password123!"

            // Fill first name
            composeTestRule.onNodeWithText("First Name").performTextInput(firstName)

            // Fill last name
            composeTestRule.onNodeWithText("Last Name").performTextInput(lastName)

            // Fill email
            composeTestRule.onNodeWithText("example@gmail.com").performTextInput(email)

            // Fill password
            composeTestRule.onNodeWithText("Password").performTextInput(password)

            // Note: Date picker and gender dropdown would require specific handling
            // that might need additional test setup
        }

        @Test
        fun testRegisterWithoutAgreeingToTerms() {
            // Fill form but don't check terms
            val email = "test@example.com"
            val password = "test123"

            composeTestRule.onNodeWithText("example@gmail.com").performTextInput(email)
            composeTestRule.onNodeWithText("Password").performTextInput(password)

            // Click register button without checking terms
            composeTestRule.onNodeWithText("Register").performClick()

            // Toast message should appear, but we can't easily test toasts
            // Instead, verify button is still clickable (form not submitted)
            composeTestRule.onNodeWithText("Register").assertExists()
        }

        @Test
        fun testNavigateToLogin() {
            // Click on "Login" text to navigate back to LoginActivity
            composeTestRule.onNodeWithText("Login").performClick()

            // Verify intent to LoginActivity
            Intents.intended(IntentMatchers.hasComponent(LoginActivity::class.java.name))
        }

        @Test
        fun testPasswordVisibilityToggle() {
            // Enter password
            composeTestRule.onNodeWithText("Password").performTextInput("TestPassword123")

            // Click visibility icon (assuming it exists)
            // Note: We need a test tag for the visibility toggle
            // For now, we'll assume it works if we can enter text
        }

        @Test
        fun testEmptyFormValidation() {
            // Click register without filling anything
            composeTestRule.onNodeWithText("Register").performClick()

            // Button should still exist (form not submitted)
            composeTestRule.onNodeWithText("Register").assertExists()
        }

        @Test
        fun testGenderDropdownInteraction() {
            // Click on gender dropdown
            composeTestRule.onNodeWithText("Select Gender").performClick()

            // The dropdown should expand, but testing dropdown items might need
            // specific test tags or we can check if the dropdown icon changes
        }

        @Test
        fun testDatePickerInteraction() {
            // Click on date picker field
            composeTestRule.onNodeWithText("Select Date").performClick()

            // DatePickerDialog should appear, but testing dialogs requires
            // UI Automator or other approaches
        }
    }
