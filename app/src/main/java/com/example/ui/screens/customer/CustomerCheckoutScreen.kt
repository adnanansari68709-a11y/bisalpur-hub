package com.example.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.*
import com.example.util.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCheckoutScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: CustomerCheckoutViewModel = viewModel(factory = viewModelFactory { a ->
        CustomerCheckoutViewModel(a.cartRepository, a.shopRepository, a.orderRepository, a.sessionManager)
    })
) {
    val checkoutState by viewModel.checkoutState.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val discount by viewModel.discount.collectAsStateWithLifecycle()

    var customerName by remember { mutableStateOf("Aman Kumar") }
    var customerPhone by remember { mutableStateOf("+91 98765 43210") }
    var address by remember { mutableStateOf("Shop No. 14, Station Road, Near Gandhi Chowk, Bisalpur - 262201") }
    var showAddressDialog by remember { mutableStateOf(false) }
    var tempAddressInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("cod") } // "cod" or "online"
    var couponInput by remember { mutableStateOf("") }
    var couponError by remember { mutableStateOf<String?>(null) }
    var deliveryNotes by remember { mutableStateOf("") }

    val quickAddressList = remember {
        listOf(
            "Station Road, Near Gandhi Chowk, Bisalpur - 262201",
            "Main Market, Opp. Central Bank, Bisalpur - 262201",
            "Civil Lines, Near Tehsil Gate, Bisalpur - 262201",
            "Mandi Road, Near Krishi Mandi, Bisalpur - 262201",
            "Barkhera Road, Adarsh Nagar, Bisalpur - 262201",
            "Bareilly Road, Near Bus Stand, Bisalpur - 262201"
        )
    }

    val subtotal = remember(cartItems) {
        cartItems.sumOf { it.productPrice * it.cartItem.quantity }
    }

    // Success Screen State
    if (checkoutState is CheckoutState.Success) {
        val success = checkoutState as CheckoutState.Success
        OrderSuccessScreen(
            navController = navController,
            orderNumber = success.orderNumber.ifBlank { "BH-982314" },
            orderAmount = success.amount.takeIf { it > 0 } ?: totalAmount,
            deliveryAddress = address,
            onDismiss = {
                viewModel.resetCheckoutState()
            }
        )
        return
    }

    // Change Address Dialog
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = RoyalNavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Delivery Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Quick Select Bisalpur Localities:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMedium
                    )

                    quickAddressList.forEach { loc ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (address == loc) ElectricBlueLight else SurfaceVariantLight,
                            border = if (address == loc) ButtonDefaults.outlinedButtonBorder else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    address = loc
                                    showAddressDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = address == loc,
                                    onClick = {
                                        address = loc
                                        showAddressDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = ElectricBluePrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = loc,
                                    fontSize = 12.sp,
                                    color = TextCharcoal,
                                    fontWeight = if (address == loc) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Or Enter Custom Street / House No:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMedium
                    )

                    OutlinedTextField(
                        value = tempAddressInput,
                        onValueChange = { tempAddressInput = it },
                        placeholder = { Text("House/Shop no, street, Bisalpur - 262201", color = TextSubtle) },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF0B1F3A),
                            fontSize = 13.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0B1F3A),
                            unfocusedTextColor = Color(0xFF0B1F3A),
                            cursorColor = Color(0xFF0B1F3A),
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempAddressInput.isNotBlank()) {
                            address = tempAddressInput.trim()
                        }
                        showAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Checkout & Payment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextCharcoal
                        )
                        Text(
                            text = "Order confirmation • Bisalpur Local",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        bottomBar = {
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
                    if (checkoutState is CheckoutState.Error) {
                        Surface(
                            color = ErrorRedLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = (checkoutState as CheckoutState.Error).message,
                                    color = ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

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
                                    text = "₹${totalAmount.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalNavyPrimary
                                )
                                if (discount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Saved ₹${discount.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.placeOrder(
                                    address = address,
                                    paymentMethod = if (selectedPaymentMethod == "cod") "Cash on Delivery" else "Online UPI"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalNavyPrimary,
                                contentColor = SurfaceWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                            enabled = checkoutState !is CheckoutState.Processing && totalAmount > 0,
                            modifier = Modifier.testTag("place_order_button")
                        ) {
                            if (checkoutState is CheckoutState.Processing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = SurfaceWhite,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedPaymentMethod == "cod") "Place COD Order" else "Pay & Place Order",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Trust Guarantee Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ElectricBlueLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = ElectricBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bisalpur Local Trust Guarantee",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavyDark
                        )
                        Text(
                            text = "Inspected authentic items • Pay cash upon receiving package",
                            fontSize = 10.sp,
                            color = TextMedium
                        )
                    }
                }
            }

            // 1. Delivery Address Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ElectricBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = ElectricBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Delivery Address",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                        }

                        TextButton(
                            onClick = {
                                tempAddressInput = address
                                showAddressDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceVariantLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = customerName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextCharcoal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RoyalNavyPrimary
                                ) {
                                    Text(
                                        text = "HOME",
                                        color = SurfaceWhite,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = address,
                                fontSize = 12.sp,
                                color = TextMedium,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Contact: $customerPhone",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Estimated Delivery: Today by 6:00 PM (Local Express)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarningOrange
                        )
                    }
                }
            }

            // 2. Ordered Products Summary
            if (cartItems.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Items in Order (${cartItems.sumOf { it.cartItem.quantity }})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextCharcoal
                            )
                            Text(
                                text = "₹${subtotal.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoyalNavyPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        cartItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceVariantLight)
                                ) {
                                    if (item.productImageUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = item.productImageUrl,
                                            contentDescription = item.productName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.ShoppingBag, contentDescription = null, tint = RoyalNavyPrimary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextCharcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Qty: ${item.cartItem.quantity} • Size: ${item.cartItem.selectedSize.ifBlank { "Standard" }}",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }

                                Text(
                                    text = "₹${(item.productPrice * item.cartItem.quantity).toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextCharcoal
                                )
                            }

                            if (index < cartItems.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = SurfaceBorderSubtle)
                            }
                        }
                    }
                }
            }

            // 3. Coupons & Offers Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Coupons & Offers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (appliedCoupon != null) {
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Coupon '$appliedCoupon' Applied!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                                        Text("You saved ₹${discount.toInt()} on this order", fontSize = 11.sp, color = TextMedium)
                                    }
                                }
                                TextButton(
                                    onClick = { viewModel.removeCoupon() },
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("Remove", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = {
                                    couponInput = it
                                    couponError = null
                                },
                                placeholder = { Text("Enter Coupon Code (e.g. BISALPUR50)", fontSize = 12.sp, color = TextSubtle) },
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color(0xFF0B1F3A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0B1F3A),
                                    unfocusedTextColor = Color(0xFF0B1F3A),
                                    cursorColor = Color(0xFF0B1F3A),
                                    focusedBorderColor = ElectricBluePrimary,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedContainerColor = SurfaceWhite,
                                    unfocusedContainerColor = SurfaceWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val success = viewModel.applyCoupon(couponInput)
                                    if (!success) {
                                        couponError = "Invalid coupon. Try 'BISALPUR50' or 'FESTIVE10'"
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)
                            ) {
                                Text("Apply", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (couponError != null) {
                            Text(
                                text = couponError!!,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick apply chip
                        SuggestionChip(
                            onClick = {
                                couponInput = "BISALPUR50"
                                viewModel.applyCoupon("BISALPUR50")
                            },
                            label = { Text("🏷️ BISALPUR50 (Flat ₹50 OFF)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // 4. Payment Method Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Payment,
                            contentDescription = null,
                            tint = RoyalNavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Payment Option",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 1: COD (Recommended)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedPaymentMethod == "cod") ElectricBlueLight else SurfaceVariantLight,
                        border = if (selectedPaymentMethod == "cod") ButtonDefaults.outlinedButtonBorder else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "cod" }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == "cod",
                                onClick = { selectedPaymentMethod = "cod" },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricBluePrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Cash / UPI on Delivery",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = RoyalNavyDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SuccessGreenLight
                                    ) {
                                        Text(
                                            text = "POPULAR",
                                            color = SuccessGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Pay cash or scan UPI QR with Bisalpur Hub rider",
                                    fontSize = 11.sp,
                                    color = TextMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Online UPI
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedPaymentMethod == "online") ElectricBlueLight else SurfaceVariantLight,
                        border = if (selectedPaymentMethod == "online") ButtonDefaults.outlinedButtonBorder else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "online" }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == "online",
                                onClick = { selectedPaymentMethod = "online" },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricBluePrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Online UPI / PhonePe / Google Pay / Cards",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextCharcoal
                                )
                                Text(
                                    text = "Instant 100% secure payment gateway",
                                    fontSize = 11.sp,
                                    color = TextMedium
                                )
                            }
                        }
                    }
                }
            }

            // 5. Price Breakdown Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Price Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Items Subtotal", color = TextMedium, fontSize = 13.sp)
                        Text("₹${subtotal.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Delivery Fee (Bisalpur Local)", color = TextMedium, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("₹40", style = MaterialTheme.typography.bodySmall, textDecoration = TextDecoration.LineThrough, color = TextMuted)
                        }
                        Text("FREE", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (discount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Coupon Discount ($appliedCoupon)", color = SuccessGreen, fontSize = 13.sp)
                            Text("- ₹${discount.toInt()}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceBorderSubtle)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount to Pay", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextCharcoal)
                        Text("₹${totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RoyalNavyPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun OrderSuccessScreen(
    navController: NavController,
    orderNumber: String,
    orderAmount: Double,
    deliveryAddress: String,
    onDismiss: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundWarm,
        bottomBar = {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            navController.navigate("orders") {
                                popUpTo("home") { inclusive = false }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View My Orders", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Continue Shopping", fontWeight = FontWeight.SemiBold, color = RoyalNavyPrimary)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Animated / Glowing Success Circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(SuccessGreenLight, CircleShape)
                    .border(2.dp, SuccessGreen.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Order Placed Successfully!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextCharcoal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Thank you for shopping local with Bisalpur Hub!",
                fontSize = 13.sp,
                color = TextMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Order Highlight Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Order ID", fontSize = 11.sp, color = TextMuted)
                            Text(orderNumber, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = RoyalNavyPrimary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreenLight
                        ) {
                            Text(
                                text = "CONFIRMED",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceBorderSubtle)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ElectricBolt, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Estimated Delivery", fontSize = 11.sp, color = TextMuted)
                            Text("Today by 6:00 PM (Local Express)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextCharcoal)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Delivering to", fontSize = 11.sp, color = TextMuted)
                            Text(deliveryAddress, fontSize = 12.sp, color = TextMedium, lineHeight = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Payment Mode", fontSize = 11.sp, color = TextMuted)
                            Text("Cash on Delivery (COD)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextCharcoal)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total to Pay", fontSize = 11.sp, color = TextMuted)
                            Text("₹${orderAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RoyalNavyPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Info Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LuxuryGoldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = RoyalNavyDark, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "You will receive real-time SMS & WhatsApp delivery updates as our rider departs.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = RoyalNavyDark
                    )
                }
            }
        }
    }
}
