package com.example.cashflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cashflow.ui.theme.CashflowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_elementsAreDisplayed() {
        composeTestRule.setContent {
            CashflowTheme {
                Login()
            }
        }

        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun loginScreen_invalidEmailValidation() {
        composeTestRule.setContent {
            CashflowTheme {
                Login()
            }
        }

        composeTestRule.onNodeWithText("Email").performTextInput("invalidemail")
        composeTestRule.onNodeWithText("Password").performTextInput("SecurePass123")
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Please enter a valid email address.").assertIsDisplayed()
    }

    @Test
    fun loginScreen_shortPasswordValidation() {
        composeTestRule.setContent {
            CashflowTheme {
                Login()
            }
        }

        composeTestRule.onNodeWithText("Email").performTextInput("alex.borrower@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("123")
        composeTestRule.onNodeWithText("Login").performClick()

        composeTestRule.onNodeWithText("Password must be at least 6 characters long.").assertIsDisplayed()
    }
}
