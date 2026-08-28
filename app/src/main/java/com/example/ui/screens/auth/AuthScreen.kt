package com.example.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.model.Role
import com.example.ui.theme.*
import com.example.util.viewModelFactory

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = viewModelFactory { app ->
        AuthViewModel(app.userRepository, app.sessionManager, app.shopRepository)
    })
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(Role.CUSTOMER) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var shopName by rememberSaveable { mutableStateOf("") }
    var shopAddress by rememberSaveable { mutableStateOf("") }
    var shopCity by rememberSaveable { mutableStateOf("Bisalpur") }
    var shopPincode by rememberSaveable { mutableStateOf("262201") }
    var shopDescription by rememberSaveable { mutableStateOf("") }

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF0B1F3A),
        unfocusedTextColor = Color(0xFF0B1F3A),
        focusedContainerColor = SurfaceWhite,
        unfocusedContainerColor = SurfaceWhite,
        disabledContainerColor = SurfaceWhite,
        cursorColor = Color(0xFF0B1F3A),
        focusedBorderColor = ElectricBluePrimary,
        unfocusedBorderColor = SurfaceBorder,
        focusedLabelColor = ElectricBluePrimary,
        unfocusedLabelColor = TextMuted,
        focusedPlaceholderColor = TextSubtle,
        unfocusedPlaceholderColor = TextSubtle,
        focusedLeadingIconColor = ElectricBluePrimary,
        unfocusedLeadingIconColor = ElectricBluePrimary,
        focusedTrailingIconColor = TextMuted,
        unfocusedTrailingIconColor = TextSubtle
    )

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onAuthSuccess()
        }
    }

    if (uiState.showForgotPasswordModal) {
        ForgotPasswordDialog(
            uiState = uiState,
            viewModel = viewModel,
            inputColors = inputColors
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWarm)
    ) {
        // Top Royal Header Background Decoration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(RoyalNavyDark, RoyalNavyPrimary)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Logo & Header Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(LuxuryGoldContainer, CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingBag,
                    contentDescription = null,
                    tint = RoyalNavyDark,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bisalpur Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = SurfaceWhite,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Apne Shehar Ki Premium Shopping",
                style = MaterialTheme.typography.bodyMedium,
                color = LuxuryGoldLight,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Auth Mode Switcher Tab (Login vs Sign Up)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(SurfaceVariantLight, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (uiState.isLogin) RoyalNavyPrimary else Color.Transparent)
                                .clickable {
                                    if (!uiState.isLogin) viewModel.toggleMode()
                                }
                                .testTag("tab_login"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Login",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (uiState.isLogin) SurfaceWhite else TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!uiState.isLogin) RoyalNavyPrimary else Color.Transparent)
                                .clickable {
                                    if (uiState.isLogin) viewModel.toggleMode()
                                }
                                .testTag("tab_signup"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign Up",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (!uiState.isLogin) SurfaceWhite else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Secondary Sub-Tabs: Email vs Mobile Number
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(BackgroundWarm, RoundedCornerShape(10.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isEmail = uiState.selectedTab == AuthTab.EMAIL
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setAuthTab(AuthTab.EMAIL)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isEmail) SurfaceWhite else Color.Transparent,
                            shadowElevation = if (isEmail) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = if (isEmail) ElectricBluePrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Email",
                                    fontWeight = if (isEmail) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isEmail) RoyalNavyPrimary else TextMuted
                                )
                            }
                        }

                        val isMobile = uiState.selectedTab == AuthTab.MOBILE
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setAuthTab(AuthTab.MOBILE)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMobile) SurfaceWhite else Color.Transparent,
                            shadowElevation = if (isMobile) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (isMobile) ElectricBluePrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mobile Number",
                                    fontWeight = if (isMobile) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isMobile) RoyalNavyPrimary else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sign Up: Full Name
                    if (!uiState.isLogin) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                viewModel.clearError()
                            },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Aman Kumar", color = TextSubtle) },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF0B1F3A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            colors = inputColors,
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = "Full Name", tint = ElectricBluePrimary)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Input Field based on Selected Tab (Email vs Mobile)
                    if (uiState.selectedTab == AuthTab.EMAIL) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                viewModel.clearError()
                            },
                            label = { Text("Email Address") },
                            placeholder = { Text("e.g. aman@example.com", color = TextSubtle) },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF0B1F3A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            colors = inputColors,
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = "Email Address", tint = ElectricBluePrimary)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )
                    } else {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                val formatted = if (clean.length > 10 && clean.startsWith("91")) {
                                    clean.drop(2).take(10)
                                } else if (clean.length > 10 && clean.startsWith("0")) {
                                    clean.drop(1).take(10)
                                } else {
                                    clean.take(10)
                                }
                                phone = formatted
                                viewModel.clearError()
                            },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("9876543210", color = TextSubtle) },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF0B1F3A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            colors = inputColors,
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = "Phone", tint = ElectricBluePrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+91",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RoyalNavyPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(SurfaceBorder))
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { newPassword ->
                            if (newPassword != password) {
                                password = newPassword
                                if (uiState.error != null) {
                                    viewModel.clearError()
                                }
                            }
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password", color = TextSubtle) },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF0B1F3A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        colors = inputColors,
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = "Password", tint = ElectricBluePrimary)
                        },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val desc = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("password_visibility_toggle")
                            ) {
                                Icon(imageVector = icon, contentDescription = desc, tint = TextMuted)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (passwordVisible) KeyboardType.Text else KeyboardType.Password
                        ),
                        singleLine = true
                    )

                    // Forgot Password Link (Visible in Login Mode)
                    if (uiState.isLogin) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricBluePrimary,
                                modifier = Modifier
                                    .clickable { viewModel.openForgotPasswordModal() }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                    .testTag("forgot_password_button")
                            )
                        }
                    }

                    // Role Selector when Signing Up
                    if (!uiState.isLogin) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "I want to join as:",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Customer Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        role = Role.CUSTOMER
                                        viewModel.clearError()
                                    }
                                    .testTag("role_customer"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (role == Role.CUSTOMER) ElectricBlueLight else SurfaceVariantLight,
                                border = if (role == Role.CUSTOMER) ButtonDefaults.outlinedButtonBorder else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = role == Role.CUSTOMER,
                                        onClick = {
                                            role = Role.CUSTOMER
                                            viewModel.clearError()
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Customer",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (role == Role.CUSTOMER) ElectricBluePrimary else TextMedium
                                    )
                                }
                            }

                            // Seller Card
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        role = Role.SELLER
                                        viewModel.clearError()
                                    }
                                    .testTag("role_seller"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (role == Role.SELLER) LuxuryGoldContainer else SurfaceVariantLight,
                                border = if (role == Role.SELLER) ButtonDefaults.outlinedButtonBorder else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = role == Role.SELLER,
                                        onClick = {
                                            role = Role.SELLER
                                            viewModel.clearError()
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Shop Seller",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (role == Role.SELLER) RoyalNavyDark else TextMedium
                                    )
                                }
                            }
                        }

                        // Additional Seller Registration Fields
                        if (role == Role.SELLER) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LuxuryGoldContainer.copy(alpha = 0.4f),
                                border = ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Store,
                                            contentDescription = null,
                                            tint = LuxuryGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Shop & Business Details",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = RoyalNavyDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Shop Name
                                    OutlinedTextField(
                                        value = shopName,
                                        onValueChange = {
                                            shopName = it
                                            viewModel.clearError()
                                        },
                                        label = { Text("Shop / Store Name *") },
                                        placeholder = { Text("e.g. Bisalpur Sweets, Gupta Sarees") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Store,
                                                contentDescription = "Shop Name"
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = inputColors,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("shop_name_input")
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Shop Address
                                    OutlinedTextField(
                                        value = shopAddress,
                                        onValueChange = {
                                            shopAddress = it
                                            viewModel.clearError()
                                        },
                                        label = { Text("Shop Address in Bisalpur *") },
                                        placeholder = { Text("e.g. Main Market, Station Road") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Store,
                                                contentDescription = "Shop Address"
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = inputColors,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("shop_address_input")
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // City
                                        OutlinedTextField(
                                            value = shopCity,
                                            onValueChange = {
                                                shopCity = it
                                                viewModel.clearError()
                                            },
                                            label = { Text("City") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = inputColors,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("shop_city_input")
                                        )

                                        // PIN Code
                                        OutlinedTextField(
                                            value = shopPincode,
                                            onValueChange = {
                                                if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                                    shopPincode = it
                                                    viewModel.clearError()
                                                }
                                            },
                                            label = { Text("PIN Code") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = inputColors,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("shop_pincode_input")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Shop Description / Category
                                    OutlinedTextField(
                                        value = shopDescription,
                                        onValueChange = {
                                            shopDescription = it
                                            viewModel.clearError()
                                        },
                                        label = { Text("Shop Category / Description (Optional)") },
                                        placeholder = { Text("e.g. Traditional Handlooms & Silk Sarees") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.ShoppingBag,
                                                contentDescription = "Shop Description"
                                            )
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = inputColors,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("shop_description_input")
                                    )
                                }
                            }
                        }
                    }

                    // Error Message
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = ErrorRedLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Submit Button
                    Button(
                        onClick = {
                            viewModel.submit(
                                name = name,
                                email = email,
                                phone = phone,
                                password = password,
                                role = role,
                                shopName = shopName,
                                shopDescription = shopDescription,
                                shopAddress = shopAddress,
                                shopCity = shopCity,
                                shopPincode = shopPincode
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalNavyPrimary,
                            contentColor = SurfaceWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_button"),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (uiState.isLogin) "Login to Bisalpur Hub" else "Create Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Continue with Google Native Button
                    OutlinedButton(
                        onClick = {
                            viewModel.continueWithGoogle(context)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceWhite,
                            contentColor = TextCharcoal
                        ),
                        border = ButtonDefaults.outlinedButtonBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_sign_in_button"),
                        enabled = !uiState.isLoading
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextCharcoal
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TextButton(
                        onClick = { viewModel.toggleMode() },
                        modifier = Modifier.testTag("toggle_auth_button")
                    ) {
                        Text(
                            text = if (uiState.isLogin) "Don't have an account? Sign up" else "Already have an account? Login",
                            color = ElectricBluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    uiState: AuthUiState,
    viewModel: AuthViewModel,
    inputColors: TextFieldColors
) {
    var recoveryInput by rememberSaveable { mutableStateOf("") }
    var otpInput by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var newPassVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPassVisible by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { viewModel.closeForgotPasswordModal() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(LuxuryGoldContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LockReset,
                            contentDescription = null,
                            tint = RoyalNavyDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Account Recovery",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = RoyalNavyDark
                        )
                        Text(
                            text = "Reset your Bisalpur Hub password",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = { viewModel.closeForgotPasswordModal() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recovery Method Switcher Tabs
                if (uiState.recoveryStep == RecoveryStep.INPUT_IDENTIFIER) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(BackgroundWarm, RoundedCornerShape(10.dp))
                            .padding(3.dp)
                    ) {
                        val isEmail = uiState.recoveryMethod == RecoveryMethod.EMAIL
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setRecoveryMethod(RecoveryMethod.EMAIL) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isEmail) SurfaceWhite else Color.Transparent,
                            shadowElevation = if (isEmail) 2.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Email Recovery",
                                    fontWeight = if (isEmail) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isEmail) RoyalNavyPrimary else TextMuted
                                )
                            }
                        }

                        val isMobile = uiState.recoveryMethod == RecoveryMethod.MOBILE
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setRecoveryMethod(RecoveryMethod.MOBILE) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMobile) SurfaceWhite else Color.Transparent,
                            shadowElevation = if (isMobile) 2.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Mobile OTP",
                                    fontWeight = if (isMobile) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isMobile) RoyalNavyPrimary else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Status Banner / Instructions
                if (uiState.recoverySuccessMessage != null) {
                    Surface(
                        color = SuccessGreenLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.recoverySuccessMessage,
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Error Banner
                if (uiState.recoveryError != null) {
                    Surface(
                        color = ErrorRedLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.recoveryError,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Active Step Content
                when (uiState.recoveryStep) {
                    RecoveryStep.INPUT_IDENTIFIER -> {
                        if (uiState.recoveryMethod == RecoveryMethod.EMAIL) {
                            Text(
                                text = "Enter your registered email address to receive password recovery instructions.",
                                fontSize = 13.sp,
                                color = TextMedium,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = recoveryInput,
                                onValueChange = {
                                    recoveryInput = it
                                    viewModel.clearRecoveryError()
                                },
                                label = { Text("Registered Email") },
                                placeholder = { Text("e.g. yourname@example.com", color = TextSubtle) },
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color(0xFF0B1F3A),
                                    fontSize = 14.sp
                                ),
                                colors = inputColors,
                                leadingIcon = {
                                    Icon(Icons.Filled.Email, contentDescription = null, tint = ElectricBluePrimary)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recovery_email_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { viewModel.requestEmailRecovery(recoveryInput) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_email_recovery_button"),
                                enabled = !uiState.recoveryLoading
                            ) {
                                if (uiState.recoveryLoading) {
                                    CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Send Reset Link", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        } else {
                            // Mobile OTP Request
                            Text(
                                text = "Enter your 10-digit registered mobile number to receive a one-time verification code (OTP).",
                                fontSize = 13.sp,
                                color = TextMedium,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = recoveryInput,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }
                                    val formatted = if (clean.length > 10 && clean.startsWith("91")) {
                                        clean.drop(2).take(10)
                                    } else if (clean.length > 10 && clean.startsWith("0")) {
                                        clean.drop(1).take(10)
                                    } else {
                                        clean.take(10)
                                    }
                                    recoveryInput = formatted
                                    viewModel.clearRecoveryError()
                                },
                                label = { Text("Registered Mobile") },
                                placeholder = { Text("9876543210", color = TextSubtle) },
                                textStyle = LocalTextStyle.current.copy(
                                    color = Color(0xFF0B1F3A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                colors = inputColors,
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 10.dp, end = 6.dp)
                                    ) {
                                        Text("+91", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalNavyPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(modifier = Modifier.width(1.dp).height(18.dp).background(SurfaceBorder))
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recovery_phone_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { viewModel.requestMobileOtp(recoveryInput) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_mobile_otp_button"),
                                enabled = !uiState.recoveryLoading
                            ) {
                                if (uiState.recoveryLoading) {
                                    CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Send 6-Digit OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    RecoveryStep.VERIFY_OTP -> {
                        // Helpful test OTP alert card so tester can verify directly without SMS hardware
                        if (uiState.generatedOtpHint != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = LuxuryGoldContainer,
                                border = ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔑 Test OTP: ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = RoyalNavyDark
                                    )
                                    Text(
                                        text = uiState.generatedOtpHint,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = RoyalNavyPrimary,
                                        letterSpacing = 2.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(
                                        onClick = { otpInput = uiState.generatedOtpHint },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Auto-fill", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalNavyPrimary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = "Enter the 6-digit OTP code sent to your registered number:",
                            fontSize = 13.sp,
                            color = TextMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { input ->
                                if (input.length <= 6 && input.all { it.isDigit() }) {
                                    otpInput = input
                                    viewModel.clearRecoveryError()
                                }
                            },
                            label = { Text("6-Digit OTP Code") },
                            placeholder = { Text("• • • • • •", color = TextSubtle) },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF0B1F3A),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 6.sp,
                                textAlign = TextAlign.Center
                            ),
                            colors = inputColors,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input_field"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.otpResendCountdown > 0) {
                                Text(
                                    text = "Resend OTP in ${uiState.otpResendCountdown}s",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            } else {
                                TextButton(onClick = { viewModel.resendOtp() }) {
                                    Text("Resend OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricBluePrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.verifyOtp(otpInput) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("verify_otp_button"),
                            enabled = otpInput.length == 6
                        ) {
                            Text("Verify Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    RecoveryStep.SET_NEW_PASSWORD -> {
                        Text(
                            text = "Create a new strong password (minimum 6 characters) for your account.",
                            fontSize = 13.sp,
                            color = TextMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newText ->
                                if (newText != newPassword) {
                                    newPassword = newText
                                    if (uiState.recoveryError != null) {
                                        viewModel.clearRecoveryError()
                                    }
                                }
                            },
                            label = { Text("New Password") },
                            placeholder = { Text("Enter new password", color = TextSubtle) },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF0B1F3A),
                                fontSize = 14.sp
                            ),
                            colors = inputColors,
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = ElectricBluePrimary)
                            },
                            trailingIcon = {
                                val icon = if (newPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                val desc = if (newPassVisible) "Hide password" else "Show password"
                                IconButton(
                                    onClick = { newPassVisible = !newPassVisible },
                                    modifier = Modifier.testTag("new_password_visibility_toggle")
                                ) {
                                    Icon(icon, contentDescription = desc, tint = TextMuted)
                                }
                            },
                            visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (newPassVisible) KeyboardType.Text else KeyboardType.Password
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { newText ->
                                if (newText != confirmPassword) {
                                    confirmPassword = newText
                                    if (uiState.recoveryError != null) {
                                        viewModel.clearRecoveryError()
                                    }
                                }
                            },
                            label = { Text("Confirm New Password") },
                            placeholder = { Text("Re-enter new password", color = TextSubtle) },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF0B1F3A),
                                fontSize = 14.sp
                            ),
                            colors = inputColors,
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = ElectricBluePrimary)
                            },
                            trailingIcon = {
                                val icon = if (confirmPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                val desc = if (confirmPassVisible) "Hide password" else "Show password"
                                IconButton(
                                    onClick = { confirmPassVisible = !confirmPassVisible },
                                    modifier = Modifier.testTag("confirm_password_visibility_toggle")
                                ) {
                                    Icon(icon, contentDescription = desc, tint = TextMuted)
                                }
                            },
                            visualTransformation = if (confirmPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (confirmPassVisible) KeyboardType.Text else KeyboardType.Password
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.completePasswordReset(newPassword, confirmPassword) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("update_password_button"),
                            enabled = !uiState.recoveryLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                        ) {
                            if (uiState.recoveryLoading) {
                                CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Update Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    RecoveryStep.SUCCESS -> {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(SuccessGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = SuccessGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Password Reset Complete!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RoyalNavyDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your old password has been invalidated. You can now log into Bisalpur Hub with your new credentials.",
                            fontSize = 12.sp,
                            color = TextMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.closeForgotPasswordModal() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavyPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("back_to_login_button")
                        ) {
                            Text("Back to Login", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
