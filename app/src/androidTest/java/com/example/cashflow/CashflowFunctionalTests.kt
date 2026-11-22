package com.example.cashflow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cashflow.screens.Home
import com.example.cashflow.screens.Signup
import com.example.cashflow.screens.SplashScreen
import com.example.cashflow.screens.AdminDashboard
import com.example.cashflow.screens.LoanOfficerDashboard
import com.example.cashflow.ui.theme.CashflowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CashflowFunctionalTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashScreen_displaysBrand() {
        composeTestRule.setContent {
            CashflowTheme {
                SplashScreen()
            }
        }
        composeTestRule.onNodeWithText("Cashflow").assertIsDisplayed()
    }

    @Test
    fun func_login_validatesEmail() {
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
    fun func_login_validatesPasswordLength() {
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

    @Test
    fun func_signup_validatesPassword() {
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

    @Test
    fun func_home_tabsAndLoanApplicationDialog() {
        composeTestRule.setContent {
            CashflowTheme {
                Home()
            }
        }

        // Verify tabs
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transactions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()

        // Open Loan Application dialog
        composeTestRule.onNodeWithText("Apply for New Loan (Mandatory Docs Included)").performClick()
        composeTestRule.onNodeWithText("Loan Application & Verification").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please upload all 3 required documents to enable submission.").assertIsDisplayed()
    }

    @Test
    fun func_transactions_tabContent() {
        composeTestRule.setContent {
            CashflowTheme {
                Home()
            }
        }

        // Switch to Transactions tab
        composeTestRule.onNodeWithText("Transactions").performClick()
        composeTestRule.onNodeWithText("Upcoming Installments").assertIsDisplayed()
        composeTestRule.onNodeWithText("Payment History").assertIsDisplayed()
    }

    @Test
    fun func_profile_tabContent() {
        composeTestRule.setContent {
            CashflowTheme {
                Home()
            }
        }

        // Switch to Profile tab
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Customer Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
    }

    @Test
    fun func_loanOfficerDashboard_displaysContent() {
        composeTestRule.setContent {
            CashflowTheme {
                LoanOfficerDashboard()
            }
        }
        composeTestRule.onNodeWithText("Loan Officer Portal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pending Loans").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Borrowers").assertIsDisplayed()
    }

    @Test
    fun func_adminDashboard_displaysContent() {
        composeTestRule.setContent {
            CashflowTheme {
                AdminDashboard()
            }
        }
        composeTestRule.onNodeWithText("Admin Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("System Users").assertIsDisplayed()
    }
}
20