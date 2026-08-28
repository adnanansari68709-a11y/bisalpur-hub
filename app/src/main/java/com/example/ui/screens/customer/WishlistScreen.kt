package com.example.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.MyApplication
import com.example.data.local.entity.ProductEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.util.viewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: WishlistViewModel = viewModel(factory = viewModelFactory { a ->
        WishlistViewModel(a.wishlistRepository, a.cartRepository, a.sessionManager)
    })
) {
    val wishlistProducts by viewModel.wishlistProducts.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val showLoginPrompt by viewModel.showLoginPrompt.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message!!)
            viewModel.clearMessage()
        }
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoginPrompt() },
            title = { Text("Please Log In", fontWeight = FontWeight.Bold) },
            text = { Text("You need to be logged in to manage your wishlist and cart.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissLoginPrompt()
                        coroutineScope.launch {
                            app.sessionManager.clearSession()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)
                ) {
                    Text("Go to Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLoginPrompt() }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceWhite
        )
    }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Wishlist",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextCharcoal
                        )
                        Text(
                            text = if (wishlistProducts.isNotEmpty()) "${wishlistProducts.size} items saved" else "0 items",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("wishlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextCharcoal
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("cart") },
                        modifier = Modifier.testTag("wishlist_cart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Cart",
                            tint = TextCharcoal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { innerPadding ->
        if (wishlistProducts.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Your Wishlist is empty",
                description = "Save products you love and find them here later.",
                actionButtonText = "Explore Products",
                onActionClick = {
                    navController.navigate("home")
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("empty_wishlist_view")
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("wishlist_grid")
            ) {
                items(wishlistProducts, key = { it.id }) { product ->
                    WishlistItemCard(
                        product = product,
                        onClick = {
                            navController.navigate("product/${product.id}")
                        },
                        onRemove = {
                            viewModel.removeFromWishlist(product.id)
                        },
                        onAddToCart = {
                            viewModel.addToCart(product)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WishlistItemCard(
    product: ProductEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountPercent = if (product.originalPrice > product.price && product.originalPrice > 0) {
        (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
    } else 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color.Black.copy(alpha = 0.05f))
            .testTag("wishlist_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Product Image Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .background(SurfaceVariantLight)
            ) {
                if (product.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrls.first(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.category.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavyPrimary
                        )
                    }
                }

                // Discount Badge (Top-Left)
                if (discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .background(
                                color = ErrorRed,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            color = SurfaceWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Remove from Wishlist Button (Top-Right)
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .background(SurfaceWhite.copy(alpha = 0.95f), CircleShape)
                        .clickable(onClick = onRemove)
                        .testTag("remove_wishlist_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Remove from Wishlist",
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Product Details Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Category & Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBluePrimary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(LuxuryGoldContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "4.8",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextCharcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "₹${product.price.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoyalNavyPrimary
                    )
                    if (product.originalPrice > product.price) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "₹${product.originalPrice.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart Button
                Button(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalNavyPrimary,
                        contentColor = SurfaceWhite
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("wishlist_add_to_cart_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add to Cart",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
