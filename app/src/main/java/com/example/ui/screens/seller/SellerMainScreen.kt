package com.example.ui.screens.seller

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.MyApplication
import com.example.ui.theme.*

data class SellerNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun SellerMainScreen(app: MyApplication) {
    val navController = rememberNavController()

    val items = remember {
        listOf(
            SellerNavItem("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
            SellerNavItem("products", "Products", Icons.Filled.Inventory, Icons.Outlined.Inventory),
            SellerNavItem("orders", "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
            SellerNavItem("profile", "Shop Info", Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    Scaffold(
        containerColor = BackgroundWarm,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route
            val showBottomBar = currentRoute in listOf("dashboard", "products", "orders", "profile")

            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = SurfaceWhite
                ) {
                    NavigationBar(
                        containerColor = SurfaceWhite,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    ) {
                        items.forEach { screen ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        screen.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                selected = isSelected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalNavyPrimary,
                                    selectedTextColor = RoyalNavyPrimary,
                                    indicatorColor = ElectricBlueLight,
                                    unselectedIconColor = TextSubtle,
                                    unselectedTextColor = TextMuted
                                ),
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
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
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { SellerDashboardScreen(app, navController) }
            composable("products") { SellerProductsScreen(app, navController) }
            composable("orders") { SellerOrdersScreen(app, navController) }
            composable("profile") { SellerProfileScreen(app, navController) }
            composable("add_product") { SellerAddProductScreen(app, navController) }
        }
    }
}
