package com.donacion_app.t2_apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        Screen.Menu,
        Screen.Cart,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFAF3E0), // fondo claro cálido
                tonalElevation = 4.dp
            ) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                                tint = if (selected) Color(0xFFB78018) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color(0xFFB78018) else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFDE7C2) // resaltado suave
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "menu",
            modifier = Modifier.padding(padding)
        ) {
            composable("menu") { MenuScreen(navController) }
            composable("cart") { CartScreen() }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    rootNavController = rootNavController
                )
            }
            composable("register_dish") { RegisterDishScreenFirebase(navController) }
            composable("edit_dish/{dishId}") { backStackEntry ->
                val dishId = backStackEntry.arguments?.getString("dishId") ?: ""
                EditDishScreen(navController = navController, dishId = dishId)
            }
            composable(
                route = "edit_user/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                EditUserScreen(navController = navController, userId = userId)
            }
        }
    }
}

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Menu : Screen("menu", Icons.Default.RestaurantMenu, "Menú")
    object Cart : Screen("cart", Icons.Default.ShoppingCart, "Carrito")
    object Profile : Screen("profile", Icons.Default.Person, "Perfil")
}