package com.rjnsdev.easyfin.ui.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rjnsdev.easyfin.ui.dashboard.collection.CollectionScreen
import com.rjnsdev.easyfin.ui.dashboard.explore.ExploreScreen
import com.rjnsdev.easyfin.ui.dashboard.settings.SettingsScreen

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onAddNewServer: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { DashboardBottomBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "explore",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("explore") {
                ExploreScreen()
            }
            composable("collection") {
                CollectionScreen()
            }
            composable("settings") {
                SettingsScreen(
                    onLogoutAll = onLogout,
                    onAddNewServer = onAddNewServer
                )
            }
        }
    }
}

@Composable
fun DashboardBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("explore", "Explore", Icons.Filled.Home),
        BottomNavItem("collection", "Collection", Icons.Filled.List),
        BottomNavItem("settings", "Settings", Icons.Filled.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (route, title, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
