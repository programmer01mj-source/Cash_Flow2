package com.example.cashflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cashflow.screens.Signup
import com.example.cashflow.ui.theme.CashflowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signupScreen_elementsAreDisplayed() {
        composeTestRule.setContent {
            CashflowTheme {
                Signup()
            }
        }

        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone Number (+265...)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    @Test
    fun signupScreen_shortPasswordValidation() {
        composeTestRule.setContent {
            CashflowTheme {
                Signup()
            }
        }

        composeTestRule.onNodeWithText("Full Name").performTextInput("Chikondi Banda")
        composeTestRule.onNodeWithText("Email").performTextInput("chikondi@example.com")
        composeTestRule.onNodeWithText("Phone Number (+265...)").performTextInput("+265991234567")
        composeTestRule.onNodeWithText("Password").performTextInput("123")
        composeTestRule.onNodeWithText("Sign Up").performClick()

        composeTestRule.onNodeWithText("Password must be at least 6 characters.").assertIsDisplayed()
    }
}
