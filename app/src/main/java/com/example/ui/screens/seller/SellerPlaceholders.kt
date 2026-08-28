package com.example.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun SellerDashboardScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: SellerDashboardViewModel = viewModel(factory = viewModelFactory { a ->
        SellerDashboardViewModel(a.shopRepository, a.orderRepository, a.sessionManager)
    })
) {
    val orders by viewModel.ordersFlow.collectAsStateWithLifecycle()
    val products by viewModel.productsFlow.collectAsStateWithLifecycle()

    val totalSales = orders.filter { it.status == "Delivered" }.sumOf { it.totalAmount }
    val pendingOrders = orders.count { it.status != "Delivered" && it.status != "Cancelled" }

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            Surface(color = RoyalNavyPrimary, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Bisalpur Seller Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                        Text(
                            text = "Manage Shop, Products & Local Orders",
                            fontSize = 11.sp,
                            color = LuxuryGoldLight
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LuxuryGoldContainer
                    ) {
                        Text(
                            text = "Verified Shop",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalNavyDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SuccessGreenLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CurrencyRupee, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Total Sales", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "₹${totalSales.toInt()}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalNavyPrimary
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(WarningOrangeLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PendingActions, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pending", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$pendingOrders orders",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = WarningOrange
                        )
                    }
                }
            }

            // Stats Row 2
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ElectricBlueLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Inventory2, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Live Catalog", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextCharcoal)
                            Text("${products.size} active products listed", fontSize = 12.sp, color = TextMuted)
                        }
                    }

                    Button(
                        onClick = { navController.navigate("add_product") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Product", fontSize = 12.sp)
                    }
                }
            }

            // Recent Orders Header
            Text(
                text = "Recent Shop Orders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextCharcoal
            )

            if (orders.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No Orders Received Yet",
                    description = "When customers from Bisalpur place orders from your shop, they will appear here.",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                orders.take(5).forEach { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("₹${order.totalAmount.toInt()} • ${order.status}", fontSize = 11.sp, color = TextMuted)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (order.status == "Delivered") SuccessGreenLight else ElectricBlueLight
                            ) {
                                Text(
                                    text = order.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.status == "Delivered") SuccessGreen else ElectricBluePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerProductsScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: SellerProductsViewModel = viewModel(factory = viewModelFactory { a ->
        SellerProductsViewModel(a.shopRepository, a.sessionManager)
    })
) {
    val products by viewModel.products.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            Surface(color = SurfaceWhite, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "My Products Catalog",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${products.size} items",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_product") },
                containerColor = RoyalNavyPrimary,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        if (products.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.Inventory2,
                title = "No Products in Catalog",
                description = "Start selling by adding your first product with price, images, and sizes.",
                actionButtonText = "Add New Product",
                onActionClick = { navController.navigate("add_product") },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
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
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(product.category.take(2).uppercase(), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCharcoal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = product.category,
                                    fontSize = 11.sp,
                                    color = ElectricBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${product.price.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = RoyalNavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (product.stock > 0) SuccessGreenLight else ErrorRedLight
                                    ) {
                                        Text(
                                            text = if (product.stock > 0) "Stock: ${product.stock}" else "Out of Stock",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (product.stock > 0) SuccessGreen else ErrorRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: SellerOrdersViewModel = viewModel(factory = viewModelFactory { a ->
        SellerOrdersViewModel(a.shopRepository, a.orderRepository, a.sessionManager)
    })
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val statuses = listOf("Order Placed", "Confirmed", "Preparing", "Ready for Delivery", "Out for Delivery", "Delivered", "Cancelled")

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            Surface(color = SurfaceWhite, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                }
            }
        }
    ) { innerPadding ->
        if (orders.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.LocalShipping,
                title = "No Orders to Fulfil",
                description = "Customer orders will appear here for processing and dispatch.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(orders) { order ->
                    var expanded by remember { mutableStateOf(false) }

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
                                Text(
                                    text = "Order #${order.orderNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${order.totalAmount.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalNavyPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Address: ${order.deliveryAddress}",
                                fontSize = 12.sp,
                                color = TextMedium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (order.status == "Cancelled") {
                                Surface(
                                    color = ErrorRedLight,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Cancel, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Order Cancelled by Customer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ErrorRed)
                                            if (!order.cancelReason.isNullOrBlank()) {
                                                Text("Reason: ${order.cancelReason}", fontSize = 11.sp, color = TextMedium)
                                            }
                                        }
                                    }
                                }
                            } else {
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = order.status,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Update Order Status") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        statuses.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    viewModel.updateOrderStatus(order, option)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerProfileScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: SellerShopViewModel = viewModel(factory = viewModelFactory { a ->
        SellerShopViewModel(a.shopRepository, a.sessionManager)
    })
) {
    val shop by viewModel.currentShop.collectAsStateWithLifecycle()
    var shopName by remember { mutableStateOf("") }
    var shopDescription by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            Surface(color = RoyalNavyPrimary, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seller Shop Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (shop == null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Register Your Bisalpur Store",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        val sellerInputColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0B1F3A),
                            unfocusedTextColor = Color(0xFF0B1F3A),
                            cursorColor = Color(0xFF0B1F3A),
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                        val sellerTextStyle = LocalTextStyle.current.copy(color = Color(0xFF0B1F3A), fontSize = 14.sp)

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop Name") },
                            textStyle = sellerTextStyle,
                            colors = sellerInputColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = shopDescription,
                            onValueChange = { shopDescription = it },
                            label = { Text("Shop Description") },
                            textStyle = sellerTextStyle,
                            colors = sellerInputColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { viewModel.createShop(shopName, shopDescription) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Create Shop")
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(LuxuryGoldContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Store, contentDescription = null, tint = RoyalNavyDark, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = shop!!.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextCharcoal
                                )
                                Text(
                                    text = "Verified Bisalpur Store",
                                    fontSize = 11.sp,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = SurfaceBorderSubtle)

                        Text(
                            text = "About Shop",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = shop!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.logout() },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRedLight,
                    contentColor = ErrorRed
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Seller Account", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerAddProductScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: SellerProductsViewModel = viewModel(factory = viewModelFactory { a ->
        SellerProductsViewModel(a.shopRepository, a.sessionManager)
    })
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var originalPrice by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fashion") }
    var gender by remember { mutableStateOf("Unisex") }
    var sizes by remember { mutableStateOf("Free Size") }
    var colors by remember { mutableStateOf("Default") }
    var stock by remember { mutableStateOf("25") }

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            TopAppBar(
                title = { Text("Add New Product", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
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
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                val formColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0B1F3A),
                    unfocusedTextColor = Color(0xFF0B1F3A),
                    cursorColor = Color(0xFF0B1F3A),
                    focusedBorderColor = ElectricBluePrimary,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
                val formTextStyle = LocalTextStyle.current.copy(color = Color(0xFF0B1F3A), fontSize = 14.sp)

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Title") },
                        textStyle = formTextStyle,
                        colors = formColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Product Description") },
                        textStyle = formTextStyle,
                        colors = formColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Selling Price (₹)") },
                            textStyle = formTextStyle,
                            colors = formColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = originalPrice,
                            onValueChange = { originalPrice = it },
                            label = { Text("MRP (₹)") },
                            textStyle = formTextStyle,
                            colors = formColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            textStyle = formTextStyle,
                            colors = formColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender / Audience") },
                            textStyle = formTextStyle,
                            colors = formColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = sizes,
                        onValueChange = { sizes = it },
                        label = { Text("Available Sizes (comma separated)") },
                        textStyle = formTextStyle,
                        colors = formColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = colors,
                        onValueChange = { colors = it },
                        label = { Text("Available Colors (comma separated)") },
                        textStyle = formTextStyle,
                        colors = formColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Stock Quantity") },
                        textStyle = formTextStyle,
                        colors = formColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.addProduct(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        originalPrice = originalPrice.toDoubleOrNull() ?: 0.0,
                        category = category,
                        gender = gender,
                        sizes = sizes,
                        colors = colors,
                        stock = stock.toIntOrNull() ?: 0
                    )
                    navController.popBackStack()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Publish Product to Marketplace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
