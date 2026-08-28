package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.MyApplication
import com.example.ui.theme.*
import com.example.util.viewModelFactory

@Composable
fun CustomerProfileScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: CustomerHomeViewModel = viewModel(factory = viewModelFactory { a ->
        CustomerHomeViewModel(a.shopRepository, a.userRepository, a.wishlistRepository, a.sessionManager)
    })
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddressModal by remember { mutableStateOf(false) }
    var showSupportModal by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }
    var showTermsModal by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out from Bisalpur Hub?") },
            text = { Text("Are you sure you want to log out? Your cart and active orders will remain safely saved in your account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSupportModal) {
        AlertDialog(
            onDismissRequest = { showSupportModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.HeadsetMic, contentDescription = null, tint = RoyalNavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bisalpur Hub Customer Care", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("We're here to help you shop local with ease!", fontSize = 13.sp, color = TextMedium)
                    Surface(shape = RoundedCornerShape(8.dp), color = SuccessGreenLight, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("💬 WhatsApp Helpline: +91 98765 43210", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SuccessGreen)
                            Text("📞 Phone Support: 1800-BISALPUR (Toll Free)", fontSize = 11.sp, color = TextCharcoal)
                            Text("🕒 Timings: Mon-Sun 8:00 AM - 9:00 PM", fontSize = 11.sp, color = TextMedium)
                        }
                    }
                    Text("Direct Support Desk: Station Road Market, Bisalpur, UP - 262201", fontSize = 11.sp, color = TextMuted)
                }
            },
            confirmButton = {
                Button(onClick = { showSupportModal = false }, colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)) {
                    Text("Got It")
                }
            }
        )
    }

    if (showAddressModal) {
        AlertDialog(
            onDismissRequest = { showAddressModal = false },
            title = { Text("Saved Delivery Addresses", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = SurfaceVariantLight, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Home (Default)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalNavyDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = RoyalNavyPrimary) {
                                    Text("PRIMARY", color = SurfaceWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Text("Station Road, Near Gandhi Chowk, Bisalpur - 262201", fontSize = 11.sp, color = TextMedium)
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = SurfaceVariantLight, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Office / Shop", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextCharcoal)
                            Text("Main Market, Near Tehsil Gate, Bisalpur - 262201", fontSize = 11.sp, color = TextMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAddressModal = false }, colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)) {
                    Text("Close")
                }
            }
        )
    }

    if (showPrivacyModal) {
        AlertDialog(
            onDismissRequest = { showPrivacyModal = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Bisalpur Hub values your privacy. Your personal information, delivery addresses, and contact details are securely handled strictly to facilitate local marketplace orders with verified Bisalpur sellers. We never sell your data to third parties.",
                        fontSize = 12.sp,
                        color = TextMedium,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyModal = false }, colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)) {
                    Text("Close")
                }
            }
        )
    }

    if (showTermsModal) {
        AlertDialog(
            onDismissRequest = { showTermsModal = false },
            title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "1. Delivery is carried out within Bisalpur town limits with standard 2-4 hour local dispatch.\n2. Cash on Delivery is supported for all verified customer orders.\n3. Return & Replacement is available within 7 days for damaged or incorrect items upon seller confirmation.",
                        fontSize = 12.sp,
                        color = TextMedium,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showTermsModal = false }, colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary)) {
                    Text("Understood")
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundWarm,
        topBar = {
            Surface(
                color = RoyalNavyPrimary,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(LuxuryGoldContainer, CircleShape)
                            .border(2.dp, LuxuryGold, CircleShape)
                            .shadow(4.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userProfile?.name?.take(1) ?: "C").uppercase(),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalNavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile?.name ?: "Valued Customer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )

                    Text(
                        text = userProfile?.email ?: userProfile?.phone ?: "customer@bisalpurhub.com",
                        fontSize = 13.sp,
                        color = LuxuryGoldLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RoyalNavySurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Verified, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verified Bisalpur Shopper",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SurfaceWhite
                            )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group 1: Shopping Shortcuts
            Text(
                text = "Shopping & Orders",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "My Wishlist",
                        subtitle = "View and purchase your saved favorite products",
                        onClick = { navController.navigate("wishlist") }
                    )
                    HorizontalDivider(color = SurfaceBorderSubtle)
                    ProfileMenuRow(
                        icon = Icons.Outlined.ShoppingBag,
                        title = "My Orders",
                        subtitle = "Track active shipments, view history & reorder",
                        onClick = { navController.navigate("orders") }
                    )
                    HorizontalDivider(color = SurfaceBorderSubtle)
                    ProfileMenuRow(
                        icon = Icons.Outlined.ShoppingCart,
                        title = "My Cart",
                        subtitle = "Review and checkout saved cart items",
                        onClick = { navController.navigate("cart") }
                    )
                    HorizontalDivider(color = SurfaceBorderSubtle)
                    ProfileMenuRow(
                        icon = Icons.Outlined.Storefront,
                        title = "Explore Bisalpur Shops",
                        subtitle = "Discover local fashion, sweets, and handicrafts",
                        onClick = { navController.navigate("home") }
                    )
                }
            }

            // Group 2: Account & Settings
            Text(
                text = "Account & Preferences",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column {
                    ProfileMenuRow(
                        icon = Icons.Outlined.LocationOn,
                        title = "Saved Delivery Addresses",
                        subtitle = "Manage Bisalpur landmarks & street addresses",
                        onClick = { showAddressModal = true }
                    )
                    HorizontalDivider(color = SurfaceBorderSubtle)
                    ProfileMenuRow(
                        icon = Icons.Outlined.Headphones,
                        title = "Help & WhatsApp Support",
                        subtitle = "Connect with Bisalpur Hub care team 8 AM - 9 PM",
                        onClick = { showSupportModal = true }
                    )
                    HorizontalDivider(color = SurfaceBorderSubtle)
                    ProfileMenuRow(
                        icon = Icons.Outlined.Security,
                        title = "Privacy Policy",
                        subtitle = "How we protect and manage your data",
                        onClick = { showPrivacyModal = true }
                    )
                    HorizontalDivider(color = SurfaceBorderSubtle)
                    ProfileMenuRow(
                        icon = Icons.Outlined.Description,
                        title = "Terms & Conditions",
                        subtitle = "Local marketplace policies and delivery rules",
                        onClick = { showTermsModal = true }
                    )
                }
            }

            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRedLight,
                    contentColor = ErrorRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out from Bisalpur Hub",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ProfileMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SurfaceVariantLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoyalNavyPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextCharcoal
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextSubtle,
            modifier = Modifier.size(16.dp)
        )
    }
}
