package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.MyApplication
import com.example.ui.theme.*
import com.example.util.viewModelFactory

data class CustomerNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun CustomerMainScreen(app: MyApplication) {
    val navController = rememberNavController()

    val cartViewModel: CustomerCartViewModel = viewModel(factory = viewModelFactory { a ->
        CustomerCartViewModel(a.cartRepository, a.shopRepository, a.sessionManager)
    })
    val cartItems by cartViewModel.cartItemsWithProducts.collectAsStateWithLifecycle()
    val cartCount = cartItems.sumOf { it.cartItem.quantity }

    val navItems = remember {
        listOf(
            CustomerNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            CustomerNavItem("search", "Search", Icons.Filled.Search, Icons.Outlined.Search, "nav_search"),
            CustomerNavItem("cart", "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_cart"),
            CustomerNavItem("orders", "Orders", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_orders"),
            CustomerNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
        )
    }

    Scaffold(
        containerColor = BackgroundWarm,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route

            // Hide bottom bar on detail and checkout for cleaner immersive flow
            val showBottomBar = currentRoute in listOf("home", "search", "cart", "orders", "profile")

            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = SurfaceWhite,
                    tonalElevation = 8.dp
                ) {
                    NavigationBar(
                        containerColor = SurfaceWhite,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    ) {
                        navItems.forEach { item ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                            NavigationBarItem(
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (item.route == "cart" && cartCount > 0) {
                                                Badge(
                                                    containerColor = ErrorRed,
                                                    contentColor = SurfaceWhite
                                                ) {
                                                    Text(
                                                        text = "$cartCount",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.title,
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
                                modifier = Modifier.testTag(item.testTag),
                                onClick = {
                                    navController.navigate(item.route) {
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
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { CustomerHomeScreen(app, navController) }
            composable("search") { CustomerSearchScreen(app, navController) }
            composable("cart") { CustomerCartScreen(app, navController) }
            composable("orders") { CustomerOrdersScreen(app, navController) }
            composable("profile") { CustomerProfileScreen(app, navController) }
            composable("product/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                ProductDetailScreen(app, productId, navController)
            }
            composable("checkout") { CustomerCheckoutScreen(app, navController) }
            composable("wishlist") { WishlistScreen(app, navController) }
        }
    }
}
