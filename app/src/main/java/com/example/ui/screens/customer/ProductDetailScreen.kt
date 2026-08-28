package com.example.ui.screens.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.MyApplication
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    app: MyApplication,
    productId: Long,
    navController: NavController
) {
    val viewModel = remember(productId) {
        ProductDetailViewModel(
            productId,
            app.shopRepository,
            app.cartRepository,
            app.wishlistRepository,
            app.sessionManager
        )
    }

    val product by viewModel.product.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val message by viewModel.addToCartMessage.collectAsStateWithLifecycle()
    val isWishlisted by viewModel.isWishlisted.collectAsStateWithLifecycle()
    val showLoginPrompt by viewModel.showLoginPrompt.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message!!)
            viewModel.clearMessage()
        }
    }

    var selectedSize by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("") }
    var selectedQuantity by remember { mutableStateOf(1) }
    var selectedImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(product) {
        if (product != null) {
            if (product!!.sizes.isNotEmpty()) selectedSize = product!!.sizes.first()
            if (product!!.colors.isNotEmpty()) selectedColor = product!!.colors.first()
        }
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoginPrompt() },
            icon = {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = RoyalNavyPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Please Log In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextCharcoal
                )
            },
            text = {
                Text(
                    text = "You need to be logged in to add items to your cart and save your wishlist.",
                    color = TextMedium,
                    fontSize = 14.sp
                )
            },
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
                    Text(
                        text = product?.name ?: "Product Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextCharcoal
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleWishlist()
                        },
                        modifier = Modifier.testTag("product_detail_wishlist_button")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) ErrorRed else TextCharcoal
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate("cart") },
                        modifier = Modifier.testTag("product_detail_cart_button")
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
        },
        bottomBar = {
            if (product != null && product!!.stock > 0) {
                Surface(
                    color = SurfaceWhite,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addToCart(selectedQuantity, selectedSize, selectedColor)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("product_add_to_cart_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalNavyPrimary),
                            border = BorderStroke(1.5.dp, RoyalNavyPrimary)
                        ) {
                            Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Cart", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.addToCart(selectedQuantity, selectedSize, selectedColor) {
                                    navController.navigate("checkout")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("product_buy_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RoyalNavyPrimary)
            }
        } else if (product == null) {
            EmptyStateView(
                icon = Icons.Outlined.SearchOff,
                title = "Product Not Found",
                description = "This item might have been unlisted by the shop or is currently unavailable in Bisalpur.",
                actionButtonText = "Back to Home",
                onActionClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        } else {
            val p = product!!
            val discountPercent = if (p.originalPrice > p.price && p.originalPrice > 0) {
                (((p.originalPrice - p.price) / p.originalPrice) * 100).toInt()
            } else 25
            val effectiveOriginalPrice = if (p.originalPrice > p.price) p.originalPrice else (p.price * 1.35)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Product Hero Gallery Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .background(SurfaceWhite)
                ) {
                    val currentImageUrl = if (p.imageUrls.isNotEmpty() && selectedImageIndex < p.imageUrls.size) {
                        p.imageUrls[selectedImageIndex]
                    } else p.imageUrls.firstOrNull() ?: ""

                    if (currentImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = currentImageUrl,
                            contentDescription = p.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(ElectricBlueLight, LuxuryGoldContainer))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.category,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoyalNavyPrimary
                            )
                        }
                    }

                    // Special Offer Tag
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 10.dp),
                        color = RoyalNavyPrimary,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "BISALPUR LOCAL • $discountPercent% OFF",
                            color = SurfaceWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Stock Tag
                    Surface(
                        shape = RoundedCornerShape(topStart = 10.dp),
                        color = if (p.stock > 5) SuccessGreen else WarningOrange,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = if (p.stock > 5) "IN STOCK (Local Store)" else "ONLY ${p.stock} LEFT",
                            color = SurfaceWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Image Gallery Selector Strip
                if (p.imageUrls.size > 1) {
                    Surface(color = SurfaceWhite, modifier = Modifier.fillMaxWidth()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(p.imageUrls.size) { idx ->
                                val isImgSelected = selectedImageIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isImgSelected) 2.dp else 1.dp,
                                            color = if (isImgSelected) RoyalNavyPrimary else SurfaceBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedImageIndex = idx }
                                ) {
                                    AsyncImage(
                                        model = p.imageUrls[idx],
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Details Card
                Card(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-12).dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Category & Rating Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElectricBlueLight
                            ) {
                                Text(
                                    text = p.category.uppercase(),
                                    color = ElectricBluePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(LuxuryGoldContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("4.9 (184 Ratings & 42 Reviews)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Price & Savings
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${p.price.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalNavyPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${effectiveOriginalPrice.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = TextDecoration.LineThrough,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save ₹${(effectiveOriginalPrice - p.price).toInt()} ($discountPercent% OFF)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }

                        Text(
                            text = "Inclusive of all local taxes • Free Delivery in Bisalpur",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SurfaceBorderSubtle)

                        // Available Offers / Coupons Section
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LuxuryGoldContainer.copy(alpha = 0.5f),
                            border = ButtonDefaults.outlinedButtonBorder,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Available Local Offers & Coupons", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextCharcoal)
                                }
                                Text("• Use code 'BISALPUR50' to get flat ₹50 OFF on your checkout", fontSize = 11.sp, color = TextMedium)
                                Text("• Free Express delivery within Bisalpur on orders above ₹199", fontSize = 11.sp, color = TextMedium)
                                Text("• Pay Cash on Delivery or scan QR upon arrival", fontSize = 11.sp, color = TextMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Size Selector
                        if (p.sizes.isNotEmpty()) {
                            Text(
                                text = "Select Size / Quantity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(p.sizes) { size ->
                                    val isSelected = selectedSize == size
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedSize = size },
                                        label = { Text(size, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalNavyPrimary,
                                            selectedLabelColor = SurfaceWhite
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Color Selector
                        if (p.colors.isNotEmpty()) {
                            Text(
                                text = "Select Variant / Color",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(p.colors) { color ->
                                    val isSelected = selectedColor == color
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedColor = color },
                                        label = { Text(color, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalNavyPrimary,
                                            selectedLabelColor = SurfaceWhite
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Quantity Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Quantity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(SurfaceVariantLight, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { if (selectedQuantity > 1) selectedQuantity-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = "$selectedQuantity",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextCharcoal,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                IconButton(
                                    onClick = { if (selectedQuantity < p.stock) selectedQuantity++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Delivery location and estimate Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundWarm,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Deliver to: Bisalpur - 262201 (Local)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ElectricBolt, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("⚡ FREE Same-Day Delivery by Today 7:00 PM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Store, contentDescription = null, tint = RoyalNavyPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Dispatched directly from Bisalpur verified seller", fontSize = 11.sp, color = TextMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Product Description
                        Text(
                            text = "Product Details & Description",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = p.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Product Highlights / Specifications Table
                        Text(
                            text = "Product Specifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariantLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Category", fontSize = 12.sp, color = TextMuted)
                                    Text(p.category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextCharcoal)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Origin", fontSize = 12.sp, color = TextMuted)
                                    Text("Handcrafted in Bisalpur, UP", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextCharcoal)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Authenticity", fontSize = 12.sp, color = TextMuted)
                                    Text("100% Genuine Local Heritage", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextCharcoal)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Return Policy", fontSize = 12.sp, color = TextMuted)
                                    Text("7 Days Easy Replacement", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextCharcoal)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Payment", fontSize = 12.sp, color = TextMuted)
                                    Text("Cash / UPI on Delivery Available", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextCharcoal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Customer Reviews Section
                        Text(
                            text = "Customer Reviews (42)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundWarm,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Amit Sharma", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextCharcoal)
                                        Text("Station Road, Bisalpur", fontSize = 10.sp, color = TextMuted)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(5) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Excellent quality and authentic local taste/fabric! Received delivery within 2 hours of ordering.", fontSize = 12.sp, color = TextMedium, lineHeight = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Verified Bisalpur Buyer • 2 days ago", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundWarm,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Priya Verma", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextCharcoal)
                                        Text("Civil Lines, Bisalpur", fontSize = 10.sp, color = TextMuted)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(5) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("So convenient to shop from our own Bisalpur shops without going to the crowded market!", fontSize = 12.sp, color = TextMedium, lineHeight = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Verified Bisalpur Buyer • 1 week ago", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
