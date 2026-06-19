package com.rjnsdev.easyfin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rjnsdev.easyfin.ui.auth.AuthScreen
import com.rjnsdev.easyfin.ui.dashboard.DashboardScreen
import com.rjnsdev.easyfin.ui.player.MediaPlayerScreen

@Composable
fun EasyfinNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onAddNewServer = {
                    navController.navigate("auth")
                }
            )
        }
        composable("player") {
            MediaPlayerScreen()
        }
    }
}
