package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.MyApplication
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.OrderItemEntity
import com.example.domain.repository.OrderRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*
import com.example.util.viewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrdersScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: CustomerOrdersViewModel = viewModel(factory = viewModelFactory { a ->
        CustomerOrdersViewModel(a.orderRepository, a.sessionManager)
    })
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val orderItemsMap by viewModel.orderItemsMap.collectAsStateWithLifecycle()
    val cancelState by viewModel.cancelState.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableStateOf("All") }
    var selectedOrderForTracking by remember { mutableStateOf<OrderEntity?>(null) }
    
    // Order selected for cancellation flow
    var orderToCancel by remember { mutableStateOf<OrderEntity?>(null) }
    var showConfirmCancelDialog by remember { mutableStateOf(false) }
    var showReasonCancelDialog by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Sync selectedOrderForTracking when orders change (e.g. after cancellation)
    LaunchedEffect(orders) {
        val current = selectedOrderForTracking
        if (current != null) {
            val updated = orders.find { it.id == current.id }
            if (updated != null) {
                selectedOrderForTracking = updated
            }
        }
    }

    // React to cancellation success
    LaunchedEffect(cancelState) {
        if (cancelState is CancelOrderUiState.Success) {
            val msg = (cancelState as CancelOrderUiState.Success).message
            snackbarHostState.showSnackbar(msg)
            viewModel.resetCancelState()
        } else if (cancelState is CancelOrderUiState.Error) {
            val msg = (cancelState as CancelOrderUiState.Error).message
            snackbarHostState.showSnackbar(msg)
            viewModel.resetCancelState()
        }
    }

    val tabs = listOf("All", "Active Orders", "Delivered", "Cancelled")

    val filteredOrders = remember(orders, selectedTab) {
        when (selectedTab) {
            "Active Orders" -> orders.filter { it.status !in listOf("Delivered", "Cancelled") }
            "Delivered" -> orders.filter { it.status == "Delivered" }
            "Cancelled" -> orders.filter { it.status == "Cancelled" }
            else -> orders
        }
    }

    // Step 1: Confirmation Dialog ("Cancel this order?")
    if (showConfirmCancelDialog && orderToCancel != null) {
        val targetOrder = orderToCancel!!
        LaunchedEffect(targetOrder.id) {
            viewModel.loadOrderItems(targetOrder.id)
        }
        val items = orderItemsMap[targetOrder.id] ?: emptyList()

        AlertDialog(
            onDismissRequest = {
                showConfirmCancelDialog = false
                orderToCancel = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Cancel this order?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextCharcoal
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Are you sure you want to cancel this order? Once cancelled, this order will be permanently stopped.",
                        fontSize = 13.sp,
                        color = TextMedium,
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceVariantLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Order ID:", fontSize = 12.sp, color = TextMuted)
                                Text("#${targetOrder.orderNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalNavyDark)
                            }

                            if (items.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Summary:", fontSize = 12.sp, color = TextMuted)
                                    Text(
                                        text = "${items.size} item(s) • ${items.first().productName}${if (items.size > 1) " +more" else ""}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextCharcoal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 160.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount:", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    "₹${targetOrder.totalAmount.toInt()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalNavyPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment:", fontSize = 12.sp, color = TextMuted)
                                Text(
                                    targetOrder.paymentMethod,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMedium
                                )
                            }

                            HorizontalDivider(color = SurfaceBorderSubtle)

                            Column {
                                Text("Delivery Address:", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    targetOrder.deliveryAddress.ifBlank { "Bisalpur, Uttar Pradesh" },
                                    fontSize = 12.sp,
                                    color = TextMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCancelDialog = false
                        showReasonCancelDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("continue_to_cancel_button")
                ) {
                    Text("Continue to Cancel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmCancelDialog = false
                        orderToCancel = null
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("keep_order_button")
                ) {
                    Text("Keep Order", color = TextCharcoal, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // Step 2: Reason Selection Dialog
    if (showReasonCancelDialog && orderToCancel != null) {
        val targetOrder = orderToCancel!!
        var selectedReason by remember { mutableStateOf("Ordered by mistake") }
        var otherReasonText by remember { mutableStateOf("") }
        val reasonOptions = listOf(
            "Ordered by mistake",
            "Found a better price",
            "Delivery taking too long",
            "Changed my mind",
            "Product no longer required",
            "Other"
        )
        val isSubmitting = cancelState is CancelOrderUiState.Submitting
        val isFormValid = selectedReason != "Other" || otherReasonText.isNotBlank()

        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) {
                    showReasonCancelDialog = false
                    orderToCancel = null
                }
            },
            title = {
                Column {
                    Text(
                        text = "Select Cancellation Reason",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextCharcoal
                    )
                    Text(
                        text = "Order #${targetOrder.orderNumber}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Please tell us why you wish to cancel this order. Your feedback helps us improve our service:",
                        fontSize = 12.sp,
                        color = TextMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    reasonOptions.forEach { reason ->
                        val isSelected = selectedReason == reason
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ElectricBlueLight else SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ElectricBluePrimary else SurfaceBorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedReason = reason },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = RoyalNavyPrimary
                                    ),
                                    modifier = Modifier.testTag("reason_radio_${reason.replace(" ", "_").lowercase()}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = reason,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) RoyalNavyDark else TextCharcoal
                                )
                            }
                        }
                    }

                    if (selectedReason == "Other") {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = otherReasonText,
                            onValueChange = { otherReasonText = it },
                            placeholder = { Text("Please write a short reason...", fontSize = 12.sp) },
                            label = { Text("Cancellation reason") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("other_reason_input"),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )
                    }

                    if (OrderRepository.isPrepaidPayment(targetOrder.paymentMethod)) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricBlueLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = ElectricBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Refund of ₹${targetOrder.totalAmount.toInt()} will be initiated to your original payment method within 3-5 business days upon cancellation.",
                                    fontSize = 11.sp,
                                    color = RoyalNavyDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (selectedReason == "Other") otherReasonText.trim() else selectedReason
                        viewModel.cancelOrder(targetOrder.id, finalReason) { success ->
                            if (success) {
                                showReasonCancelDialog = false
                                orderToCancel = null
                                selectedOrderForTracking = null
                            }
                        }
                    },
                    enabled = isFormValid && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        disabledContainerColor = SurfaceBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_cancellation_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = SurfaceWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm Cancellation", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isSubmitting) {
                            showReasonCancelDialog = false
                            showConfirmCancelDialog = true
                        }
                    }
                ) {
                    Text("Back", color = TextMedium)
                }
            }
        )
    }

    // Order Details & Tracking Dialog
    if (selectedOrderForTracking != null) {
        val o = selectedOrderForTracking!!
        LaunchedEffect(o.id) {
            viewModel.loadOrderItems(o.id)
        }
        val items = orderItemsMap[o.id] ?: emptyList()
        val isCancellable = OrderRepository.isOrderCancellable(o.status)
        val isCancelled = o.status.equals("Cancelled", ignoreCase = true)

        AlertDialog(
            onDismissRequest = { selectedOrderForTracking = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = RoyalNavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Order #${o.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isCancelled -> ErrorRedLight
                            o.status == "Delivered" -> SuccessGreenLight
                            o.status in listOf("Out for Delivery", "Ready for Delivery") -> ElectricBlueLight
                            else -> WarningOrangeLight
                        }
                    ) {
                        Text(
                            text = o.status.uppercase(),
                            color = when {
                                isCancelled -> ErrorRed
                                o.status == "Delivered" -> SuccessGreen
                                o.status in listOf("Out for Delivery", "Ready for Delivery") -> ElectricBluePrimary
                                else -> WarningOrange
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Banner
                    if (isCancelled) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ErrorRedLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Cancel, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Order Cancelled", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ErrorRed)
                                }
                                if (o.cancelledAt != null) {
                                    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(o.cancelledAt))
                                    Text("Cancelled on: $dateStr", fontSize = 11.sp, color = TextMedium)
                                }
                                if (!o.cancelReason.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Reason: ${o.cancelReason}", fontSize = 11.sp, color = TextCharcoal, fontWeight = FontWeight.Medium)
                                }
                                
                                // Refund details
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = ErrorRed.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                if (OrderRepository.isPrepaidPayment(o.paymentMethod)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = ElectricBluePrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Refund Status: ${o.refundStatus ?: "Refund Pending"}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalNavyDark
                                        )
                                    }
                                    Text(
                                        text = "Amount of ₹${o.totalAmount.toInt()} will be credited back to your original payment method in 3-5 business days.",
                                        fontSize = 10.sp,
                                        color = TextMedium
                                    )
                                } else {
                                    Text(
                                        text = "Payment Method: Cash on Delivery (No payment collected, no refund required)",
                                        fontSize = 11.sp,
                                        color = TextMedium
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElectricBlueLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Current Status", fontSize = 11.sp, color = TextMuted)
                                Text(o.status, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = RoyalNavyDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("⚡ Estimated Delivery: Today, within 2-4 hours by Bisalpur Local Express", fontSize = 11.sp, color = TextMedium)
                            }
                        }
                    }

                    // Order Items Summary
                    if (items.isNotEmpty()) {
                        Text("Order Items (${items.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextCharcoal)
                        items.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceVariantLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextCharcoal)
                                        Text("Qty: ${item.quantity} • ₹${item.price.toInt()} each", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Text("₹${(item.price * item.quantity).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = RoyalNavyPrimary)
                                }
                            }
                        }
                    }

                    // Timeline
                    if (!isCancelled) {
                        Text("Tracking Timeline:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextCharcoal)

                        val timelineSteps = listOf(
                            "Order Placed" to "Your order was received by the seller",
                            "Order Confirmed" to "Shop verified and packed your item",
                            "Out for Delivery" to "Bisalpur Hub Rider assigned for dispatch",
                            "Delivered" to "Delivered to your doorstep"
                        )

                        val activeIdx = when (o.status) {
                            "Delivered" -> 3
                            "Out for Delivery", "Ready for Delivery" -> 2
                            "Confirmed", "Preparing" -> 1
                            else -> 0
                        }

                        timelineSteps.forEachIndexed { idx, (stepName, stepDesc) ->
                            val isDone = idx <= activeIdx
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(if (isDone) SuccessGreen else TextSubtle, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                    if (idx < timelineSteps.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(26.dp)
                                                .background(if (idx < activeIdx) SuccessGreen else SurfaceBorder)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = stepName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isDone) TextCharcoal else TextMuted
                                    )
                                    Text(
                                        text = stepDesc,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = SurfaceBorderSubtle)

                    Text("Delivery Address:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextCharcoal)
                    Text(o.deliveryAddress.ifBlank { "Bisalpur, Uttar Pradesh - 262201" }, fontSize = 11.sp, color = TextMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment Method:", fontSize = 12.sp, color = TextMuted)
                        Text(o.paymentMethod, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextCharcoal)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextCharcoal)
                        Text("₹${o.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = RoyalNavyPrimary)
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCancellable) {
                        OutlinedButton(
                            onClick = {
                                orderToCancel = o
                                showConfirmCancelDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ErrorRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("cancel_order_button")
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(14.dp), tint = ErrorRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel Order", fontWeight = FontWeight.Bold, color = ErrorRed)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = { selectedOrderForTracking = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = SurfaceWhite, shadowElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Orders",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = ElectricBlueLight
                        ) {
                            Text(
                                text = "${orders.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBluePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Tab Selector
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tabs) { tab ->
                            val isSelected = selectedTab == tab
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                label = { Text(tab, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalNavyPrimary,
                                    selectedLabelColor = SurfaceWhite
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    ) { innerPadding ->
        if (filteredOrders.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.ReceiptLong,
                title = if (selectedTab == "All") "No Orders Placed Yet" else "No $selectedTab",
                description = "Looks like you haven't made any purchases here. Discover great deals in Bisalpur local marketplace!",
                actionButtonText = "Explore Bisalpur Hub",
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
                items(filteredOrders) { order ->
                    OrderCard(
                        order = order,
                        onTrackClick = { selectedOrderForTracking = order },
                        onCancelClick = {
                            orderToCancel = order
                            showConfirmCancelDialog = true
                        },
                        onReorderClick = {
                            navController.navigate("cart")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderEntity,
    onTrackClick: () -> Unit,
    onCancelClick: () -> Unit,
    onReorderClick: () -> Unit
) {
    val isCancelled = order.status.equals("Cancelled", ignoreCase = true)
    val isDelivered = order.status.equals("Delivered", ignoreCase = true)
    val isCancellable = OrderRepository.isOrderCancellable(order.status)

    val statusColor = when {
        isDelivered -> SuccessGreen
        isCancelled -> ErrorRed
        order.status in listOf("Out for Delivery", "Ready for Delivery") -> ElectricBluePrimary
        else -> WarningOrange
    }

    val statusBg = when {
        isDelivered -> SuccessGreenLight
        isCancelled -> ErrorRedLight
        order.status in listOf("Out for Delivery", "Ready for Delivery") -> ElectricBlueLight
        else -> WarningOrangeLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onTrackClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory2, contentDescription = null, tint = RoyalNavyPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Order #${order.orderNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextCharcoal
                        )
                    }
                    Text(
                        text = "${order.paymentMethod} • Bisalpur Express",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = order.status.uppercase(),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isCancelled && !order.cancelReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ErrorRedLight.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelled: ${order.cancelReason}",
                        fontSize = 11.sp,
                        color = ErrorRed,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SurfaceBorderSubtle)

            // Address Preview
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = ElectricBluePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.deliveryAddress.ifBlank { "Bisalpur, Uttar Pradesh - 262201" },
                    fontSize = 12.sp,
                    color = TextMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Tracker visualization
            if (!isCancelled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf("Placed", "Confirmed", "Shipped", "Delivered")
                    val activeStepIndex = when (order.status) {
                        "Delivered" -> 3
                        "Out for Delivery", "Ready for Delivery" -> 2
                        "Confirmed", "Preparing" -> 1
                        else -> 0
                    }

                    steps.forEachIndexed { index, step ->
                        val isDone = index <= activeStepIndex
                        val dotColor = if (isDone) SuccessGreen else TextSubtle

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(dotColor, CircleShape)
                        )
                        if (index < steps.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = if (index < activeStepIndex) SuccessGreen else SurfaceBorder,
                                thickness = 2.dp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Total Amount & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Amount",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "₹${order.totalAmount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = RoyalNavyPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isCancellable) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("card_cancel_order_button")
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ErrorRed)
                        }
                    }

                    OutlinedButton(
                        onClick = onTrackClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("track_order_button")
                    ) {
                        Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalNavyPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalNavyPrimary)
                    }

                    if (isCancelled || isDelivered) {
                        Button(
                            onClick = onReorderClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reorder", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
