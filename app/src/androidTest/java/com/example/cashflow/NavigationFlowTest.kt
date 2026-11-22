package com.example.cashflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigation_fromLoginToSignup() {
        composeTestRule.setContent {
            CashflowApp()
        }

        Thread.sleep(2200)

        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Sign up").performClick()
        composeTestRule.onNodeWithText("SignUp").assertIsDisplayed()
    }
}
