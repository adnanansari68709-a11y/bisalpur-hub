package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.MyApplication
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.viewModelFactory
import kotlinx.coroutines.launch

@Composable
fun CustomerHomeScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: CustomerHomeViewModel = viewModel(factory = viewModelFactory { a ->
        CustomerHomeViewModel(a.shopRepository, a.userRepository, a.wishlistRepository, a.sessionManager)
    })
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedIds.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("all") }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "all") {
            products
        } else {
            products.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val trendingProducts = remember(products) {
        products.take(6)
    }

    val dealProducts = remember(products) {
        products.filter { it.originalPrice > it.price }.take(5)
    }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = RoyalNavyPrimary,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Top Bar: Location, Wishlist & Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Location",
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Deliver to Bisalpur, 262201",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SurfaceWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SurfaceWhite.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Bisalpur Hub • Apne Shehar Ki Shopping",
                                fontSize = 11.sp,
                                color = LuxuryGoldLight
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wishlist Heart Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(RoyalNavySurface, CircleShape)
                                    .clickable {
                                        navController.navigate("wishlist")
                                    }
                                    .testTag("home_wishlist_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (wishlistedIds.isNotEmpty()) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Wishlist",
                                    tint = if (wishlistedIds.isNotEmpty()) ErrorRed else SurfaceWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (wishlistedIds.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(8.dp)
                                            .background(ErrorRed, CircleShape)
                                    )
                                }
                            }

                            // Notification Bell
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(RoyalNavySurface, CircleShape)
                                    .clickable {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("No new notifications")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = SurfaceWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(8.dp)
                                        .background(LuxuryGold, CircleShape)
                                    )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clickable Search Bar Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clickable {
                                navController.navigate("search")
                            }
                            .testTag("home_search_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search kurtas, sweets, sarees, oils...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Promotional Carousel Banner
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HomePromoBanner(
                    onBannerClick = { promo ->
                        navController.navigate("search")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Categories Row
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Explore Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Text(
                            text = "See All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricBluePrimary,
                            modifier = Modifier.clickable { navController.navigate("search") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(defaultCategories) { cat ->
                            CategoryItem(
                                category = cat,
                                isSelected = selectedCategory == cat.id,
                                onClick = {
                                    selectedCategory = cat.id
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. Flash Deals Horizontal Section
            if (dealProducts.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceWhite)
                            .padding(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(ErrorRedLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FlashOn,
                                        contentDescription = null,
                                        tint = ErrorRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Bisalpur Flash Deals",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal
                                    )
                                    Text(
                                        text = "Up to 50% off on local favourites",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(dealProducts) { product ->
                                Box(modifier = Modifier.width(170.dp)) {
                                    ProductCard(
                                        product = product,
                                        isWishlisted = wishlistedIds.contains(product.id),
                                        onToggleWishlist = {
                                            viewModel.toggleWishlist(product.id) { isAdded ->
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        if (isAdded) "Added ${product.name} to Wishlist ❤️" else "Removed from Wishlist"
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            navController.navigate("product/${product.id}")
                                        },
                                        onAddToCart = {
                                            coroutineScope.launch {
                                                val userId = app.sessionManager.getUserId()
                                                if (userId != null) {
                                                    app.cartRepository.addToCart(
                                                        userId,
                                                        product.id,
                                                        1,
                                                        product.sizes.firstOrNull() ?: "Standard",
                                                        product.colors.firstOrNull() ?: "Default"
                                                    )
                                                    snackbarHostState.showSnackbar("Added ${product.name} to Cart")
                                                } else {
                                                    snackbarHostState.showSnackbar("Please login first")
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 4. Trending & Category Products Grid Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "all") "Trending in Bisalpur" else "$selectedCategory Collection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${filteredProducts.size} items",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 5. Products Grid
            if (filteredProducts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.Inventory2,
                        title = "No products found in this category",
                        description = "Check back soon as local sellers add more items.",
                        actionButtonText = "View All Products",
                        onActionClick = { selectedCategory = "all" },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            } else {
                val chunkedProducts = filteredProducts.chunked(2)
                items(chunkedProducts) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(
                                product = pair[0],
                                isWishlisted = wishlistedIds.contains(pair[0].id),
                                onToggleWishlist = {
                                    viewModel.toggleWishlist(pair[0].id) { isAdded ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (isAdded) "Added ${pair[0].name} to Wishlist ❤️" else "Removed from Wishlist"
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    navController.navigate("product/${pair[0].id}")
                                },
                                onAddToCart = {
                                    coroutineScope.launch {
                                        val userId = app.sessionManager.getUserId()
                                        if (userId != null) {
                                            app.cartRepository.addToCart(
                                                userId,
                                                pair[0].id,
                                                1,
                                                pair[0].sizes.firstOrNull() ?: "Standard",
                                                pair[0].colors.firstOrNull() ?: "Default"
                                            )
                                            snackbarHostState.showSnackbar("Added ${pair[0].name} to Cart")
                                        } else {
                                            snackbarHostState.showSnackbar("Please login first")
                                        }
                                    }
                                }
                            )
                        }

                        if (pair.size > 1) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    product = pair[1],
                                    isWishlisted = wishlistedIds.contains(pair[1].id),
                                    onToggleWishlist = {
                                        viewModel.toggleWishlist(pair[1].id) { isAdded ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (isAdded) "Added ${pair[1].name} to Wishlist ❤️" else "Removed from Wishlist"
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        navController.navigate("product/${pair[1].id}")
                                    },
                                    onAddToCart = {
                                        coroutineScope.launch {
                                            val userId = app.sessionManager.getUserId()
                                            if (userId != null) {
                                                app.cartRepository.addToCart(
                                                    userId,
                                                    pair[1].id,
                                                    1,
                                                    pair[1].sizes.firstOrNull() ?: "Standard",
                                                    pair[1].colors.firstOrNull() ?: "Default"
                                                )
                                                snackbarHostState.showSnackbar("Added ${pair[1].name} to Cart")
                                            } else {
                                                snackbarHostState.showSnackbar("Please login first")
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
