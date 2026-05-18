package com.example.fridgehelper.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.fridgehelper.ui.theme.Green100
import com.example.fridgehelper.ui.theme.Green900
import com.example.fridgehelper.ui.theme.GreenBorder
import com.example.fridgehelper.ui.theme.NavInactive
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fridgehelper.ui.fridge.FridgeScreen
import com.example.fridgehelper.ui.recipes.RecipeDetailScreen
import com.example.fridgehelper.ui.recipes.RecipesScreen
import com.example.fridgehelper.ui.scanner.ScannerScreen
import com.example.fridgehelper.ui.settings.SettingsScreen
import com.example.fridgehelper.ui.statistics.StatisticsScreen

// NAWIGACJA APLIKACJI PRZEŁĄCZANIE PO EKRANIE
sealed class Screen(val route: String, val label: String) {
    object Fridge      : Screen("fridge", "Fridge")
    object Recipes     : Screen("recipes", "Recipes")
    object Statistics  : Screen("statistics", "Statistics")
    object Settings    : Screen("settings", "Settings")
    object Scanner     : Screen("scanner", "Scanner")
    object RecipeDetail : Screen("recipe_detail/{recipeId}", "Recipe") {
        fun createRoute(recipeId: Int) = "recipe_detail/$recipeId"
    }
}

@Composable
fun FridgeNavHost() {
    val navController = rememberNavController()
    val bottomItems = listOf(Screen.Fridge, Screen.Recipes, Screen.Statistics, Screen.Settings)
    val bottomRoutes = bottomItems.map { it.route }.toSet()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute in bottomRoutes) {
                Column {
                    // górny border nawigacji oddziela zawartość od paska
                    HorizontalDivider(color = GreenBorder, thickness = 1.dp)
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
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
                                        Screen.Fridge      -> Icon(Icons.Default.Kitchen, null)
                                        Screen.Recipes     -> Icon(Icons.AutoMirrored.Filled.MenuBook, null)
                                        Screen.Statistics  -> Icon(Icons.Default.BarChart, null)
                                        Screen.Settings    -> Icon(Icons.Default.Settings, null)
                                        else -> {}
                                    }
                                },
                                label = { Text(screen.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor   = Green900,
                                    selectedTextColor   = Green900,
                                    indicatorColor      = Green100,
                                    unselectedIconColor = NavInactive,
                                    unselectedTextColor = NavInactive
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Fridge.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Fridge.route)      { FridgeScreen(navController) }
            composable(Screen.Recipes.route)     { RecipesScreen(navController) }
            composable(Screen.Statistics.route)  { StatisticsScreen() }
            composable(Screen.Settings.route)    { SettingsScreen() }
            composable(Screen.Scanner.route)     { ScannerScreen(navController) }
            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
            ) {
                RecipeDetailScreen(navController)
            }
        }
    }
}
