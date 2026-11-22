package com.example.cashflow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cashflow.navigation.Routes
import com.example.cashflow.screens.AdminDashboard
import com.example.cashflow.screens.Home
import com.example.cashflow.screens.LoanOfficerDashboard
import com.example.cashflow.screens.Signup
import com.example.cashflow.screens.SplashScreen

@Composable
fun CashflowApp(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            Login(
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                },
                onLoginSuccess = { role, token ->
                    println("Authenticated successfully with token: $token")
                    when (role) {
                        "Loan Officer" -> {
                            navController.navigate(Routes.LOAN_OFFICER_DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        "Admin" -> {
                            navController.navigate(Routes.ADMIN_DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        composable(Routes.SIGNUP) {
            Signup(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignupSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable (Routes.HOME){
            Home {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            }
        }
        composable(Routes.LOAN_OFFICER_DASHBOARD) {
            LoanOfficerDashboard(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOAN_OFFICER_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboard(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}
