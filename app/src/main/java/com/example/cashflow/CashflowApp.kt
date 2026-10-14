package com.example.cashflow

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cashflow.navigation.Routes
import com.example.cashflow.screens.Signup
import com.example.cashflow.screens.Home
@Composable
fun CashflowApp(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination =Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            Login(
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }
        composable(Routes.SIGNUP) {
            Signup(
                onNavigateToLogin = {
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

    }
}

