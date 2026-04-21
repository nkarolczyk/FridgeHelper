package com.example.fridgehelper.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fridgehelper.ui.fridge.FridgeScreen
import com.example.fridgehelper.ui.recipes.RecipesScreen
import com.example.fridgehelper.ui.scanner.ScannerScreen
import com.example.fridgehelper.ui.settings.SettingsScreen

// NAWIGACJA APLIKACJI PRZEŁĄCZANIE PO EKRANIE

sealed class Screen(val route: String, val label: String) {
    object Fridge   : Screen("fridge", "Lodówka")
    object Recipes  : Screen("recipes", "Przepisy")
    object Settings : Screen("settings", "Ustawienia")
    object Scanner  : Screen("scanner", "Skaner")
}

@Composable
fun FridgeNavHost() {
    val navController = rememberNavController()
    val bottomItems = listOf(Screen.Fridge, Screen.Recipes, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDest = navBackStackEntry?.destination
                bottomItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            when (screen) {
                                Screen.Fridge   -> Icon(Icons.Default.Kitchen, null)
                                Screen.Recipes  -> Icon(Icons.Default.MenuBook, null)
                                Screen.Settings -> Icon(Icons.Default.Settings, null)
                                else -> {}
                            }
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Fridge.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Fridge.route)   { FridgeScreen(navController) }
            composable(Screen.Recipes.route)  { RecipesScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Scanner.route)  { ScannerScreen(navController) }
        }
    }
}