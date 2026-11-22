package com.example.cashflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cashflow.screens.Home
import com.example.cashflow.ui.theme.CashflowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_tabsAndSwitching() {
        composeTestRule.setContent {
            CashflowTheme {
                Home()
            }
        }

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transactions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()

        // Switch to Transactions tab
        composeTestRule.onNodeWithText("Transactions").performClick()
        composeTestRule.onNodeWithText("Upcoming Installments").assertIsDisplayed()

        // Switch to Profile tab
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Customer Profile").assertIsDisplayed()
    }

    @Test
    fun homeScreen_loanApplicationDialogValidation() {
        composeTestRule.setContent {
            CashflowTheme {
                Home()
            }
        }

        // Click Apply for New Loan button
        composeTestRule.onNodeWithText("Apply for New Loan (Mandatory Docs Included)").performClick()

        // Verify dialog title and missing docs warning
        composeTestRule.onNodeWithText("Loan Application & Verification").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please upload all 3 required documents to enable submission.").assertIsDisplayed()
    }
}
