package com.example.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.util.viewModelFactory
import kotlinx.coroutines.launch

@Composable
fun CustomerCartScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: CustomerCartViewModel = viewModel(factory = viewModelFactory { a ->
        CustomerCartViewModel(a.cartRepository, a.shopRepository, a.sessionManager)
    })
) {
    val cartItems by viewModel.cartItemsWithProducts.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    var promoCodeApplied by remember { mutableStateOf(false) }
    var promoInput by remember { mutableStateOf("") }
    var appliedPromoCode by remember { mutableStateOf("") }
    var promoDiscountAmount by remember { mutableStateOf(0.0) }
    var promoError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val mrpTotal = remember(cartItems) {
        cartItems.sumOf { (it.productPrice * 1.35) * it.cartItem.quantity }
    }
    val productSavings = remember(mrpTotal, totalAmount) {
        (mrpTotal - totalAmount).coerceAtLeast(0.0)
    }
    val finalPayable = remember(totalAmount, promoDiscountAmount) {
        (totalAmount - promoDiscountAmount).coerceAtLeast(0.0)
    }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Shopping Cart",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (cartItems.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = ElectricBlueLight
                            ) {
                                Text(
                                    text = "${cartItems.sumOf { it.cartItem.quantity }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBluePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (cartItems.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bisalpur (262201)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMedium
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    color = SurfaceWhite,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Payable Amount",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "₹${finalPayable.toInt()}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RoyalNavyPrimary
                                    )
                                    if (productSavings + promoDiscountAmount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Save ₹${(productSavings + promoDiscountAmount).toInt()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { navController.navigate("checkout") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RoyalNavyPrimary,
                                    contentColor = SurfaceWhite
                                ),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                                modifier = Modifier.testTag("proceed_to_checkout_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Proceed to Checkout",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.ShoppingCart,
                title = "Your Shopping Cart is Empty",
                description = "Explore curated collections from Bisalpur's finest local stores and fill your bag with great deals!",
                actionButtonText = "Start Shopping in Bisalpur",
                onActionClick = { navController.navigate("home") },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Free Delivery Badge Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SuccessGreenLight,
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalShipping,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FREE Same-Day Delivery Unlocked! ⚡",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "Delivered directly from Bisalpur verified shops within 2-4 hours",
                                    fontSize = 11.sp,
                                    color = TextMedium
                                )
                            }
                        }
                    }
                }

                // Cart Items List
                items(cartItems) { itemWithProduct ->
                    val item = itemWithProduct.cartItem
                    val mrpPrice = (itemWithProduct.productPrice * 1.35).toInt()
                    val currentPrice = itemWithProduct.productPrice.toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Product Thumbnail
                                Box(
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceVariantLight)
                                ) {
                                    if (itemWithProduct.productImageUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = itemWithProduct.productImageUrl,
                                            contentDescription = itemWithProduct.productName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.ShoppingBag,
                                                contentDescription = null,
                                                tint = RoyalNavyPrimary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                                        color = RoyalNavyPrimary,
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = "LOCAL",
                                            color = SurfaceWhite,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Product Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = itemWithProduct.productName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (item.selectedSize.isNotEmpty()) {
                                            Surface(
                                                color = SurfaceVariantLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Size: ${item.selectedSize}",
                                                    fontSize = 10.sp,
                                                    color = TextMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (item.selectedColor.isNotEmpty()) {
                                            Surface(
                                                color = SurfaceVariantLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.selectedColor,
                                                    fontSize = 10.sp,
                                                    color = TextMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Pricing
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${currentPrice * item.quantity}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = RoyalNavyPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "₹${mrpPrice * item.quantity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            textDecoration = TextDecoration.LineThrough,
                                            color = TextMuted
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "25% OFF",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorderSubtle)

                            // Action Row: Quantity Stepper + Save for later + Delete
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(SurfaceVariantLight, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (item.quantity > 1) {
                                                viewModel.updateQuantity(item, item.quantity - 1)
                                            } else {
                                                viewModel.removeCartItem(item)
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.quantity == 1) Icons.Filled.DeleteOutline else Icons.Filled.Remove,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(16.dp),
                                            tint = if (item.quantity == 1) ErrorRed else TextCharcoal
                                        )
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    IconButton(
                                        onClick = { viewModel.updateQuantity(item, item.quantity + 1) },
                                        enabled = item.quantity < itemWithProduct.availableStock,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Increase",
                                            modifier = Modifier.size(16.dp),
                                            tint = TextCharcoal
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Item saved to Wishlist")
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextMedium)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save for later", fontSize = 11.sp, color = TextMedium, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { viewModel.removeCartItem(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Remove",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Coupon / Promo Code Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalOffer,
                                        contentDescription = null,
                                        tint = LuxuryGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Apply Bisalpur Hub Coupon",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextCharcoal
                                    )
                                }
                                if (promoCodeApplied) {
                                    TextButton(
                                        onClick = {
                                            promoCodeApplied = false
                                            appliedPromoCode = ""
                                            promoDiscountAmount = 0.0
                                            promoError = null
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Text("Remove", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (promoCodeApplied) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SuccessGreenLight,
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Coupon '$appliedPromoCode' Applied!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                                            Text("You saved ₹${promoDiscountAmount.toInt()} extra on this order", fontSize = 11.sp, color = TextMedium)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = promoInput,
                                        onValueChange = {
                                            promoInput = it
                                            promoError = null
                                        },
                                        placeholder = { Text("Enter code (e.g. BISALPUR50)", fontSize = 12.sp) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            val trimmed = promoInput.trim().uppercase()
                                            if (trimmed == "BISALPUR50" || trimmed == "SAVE50") {
                                                promoCodeApplied = true
                                                appliedPromoCode = trimmed
                                                promoDiscountAmount = 50.0
                                                promoError = null
                                            } else if (trimmed == "LOCAL10" || trimmed == "FESTIVE10") {
                                                promoCodeApplied = true
                                                appliedPromoCode = trimmed
                                                promoDiscountAmount = totalAmount * 0.10
                                                promoError = null
                                            } else {
                                                promoError = "Invalid code. Try 'BISALPUR50' or 'LOCAL10'"
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)
                                    ) {
                                        Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (promoError != null) {
                                    Text(
                                        text = promoError!!,
                                        color = ErrorRed,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SuggestionChip(
                                        onClick = {
                                            promoInput = "BISALPUR50"
                                            promoCodeApplied = true
                                            appliedPromoCode = "BISALPUR50"
                                            promoDiscountAmount = 50.0
                                            promoError = null
                                        },
                                        label = { Text("🏷️ BISALPUR50 (₹50 OFF)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    SuggestionChip(
                                        onClick = {
                                            promoInput = "LOCAL10"
                                            promoCodeApplied = true
                                            appliedPromoCode = "LOCAL10"
                                            promoDiscountAmount = totalAmount * 0.10
                                            promoError = null
                                        },
                                        label = { Text("🏷️ LOCAL10 (10% OFF)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Detailed Bill Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Order Price Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total MRP (${cartItems.sumOf { it.cartItem.quantity }} items)", color = TextMedium, fontSize = 13.sp)
                                Text("₹${mrpTotal.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Product Discount", color = SuccessGreen, fontSize = 13.sp)
                                Text("- ₹${productSavings.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            if (promoCodeApplied) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Coupon Savings ($appliedPromoCode)", color = SuccessGreen, fontSize = 13.sp)
                                    Text("- ₹${promoDiscountAmount.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Delivery Charges", color = TextMedium, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("₹40", style = MaterialTheme.typography.bodySmall, textDecoration = TextDecoration.LineThrough, color = TextMuted)
                                }
                                Text("FREE", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceBorderSubtle)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total Payable Amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCharcoal
                                )
                                Text(
                                    "₹${finalPayable.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalNavyPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessGreenLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🎉 You will save ₹${(productSavings + promoDiscountAmount).toInt()} on this order",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Trust Badge
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "100% Safe & Secure Checkout • Bisalpur Local Market",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
