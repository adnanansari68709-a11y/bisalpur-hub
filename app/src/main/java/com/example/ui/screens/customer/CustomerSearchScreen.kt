package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.MyApplication
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import com.example.util.viewModelFactory
import kotlinx.coroutines.launch

@Composable
fun CustomerSearchScreen(
    app: MyApplication,
    navController: NavController,
    viewModel: CustomerSearchViewModel = viewModel(factory = viewModelFactory { a ->
        CustomerSearchViewModel(a.shopRepository, a.wishlistRepository, a.sessionManager)
    })
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val wishlistedIds by viewModel.wishlistedIds.collectAsStateWithLifecycle()
    val allProducts by app.shopRepository.getAllProducts().collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedFilterCategory by remember { mutableStateOf("All") }
    val displayProducts = remember(query, results, allProducts, selectedFilterCategory) {
        val baseList = if (query.isBlank()) allProducts else results
        if (selectedFilterCategory == "All") {
            baseList
        } else {
            baseList.filter { it.category.equals(selectedFilterCategory, ignoreCase = true) }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = BackgroundWarm,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = SurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Search Bisalpur products, shops, brands...", color = TextSubtle) },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF0B1F3A),
                            fontSize = 14.sp
                        ),
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = ElectricBluePrimary)
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0B1F3A),
                            unfocusedTextColor = Color(0xFF0B1F3A),
                            cursorColor = Color(0xFF0B1F3A),
                            focusedBorderColor = ElectricBluePrimary,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedPlaceholderColor = TextSubtle,
                            unfocusedPlaceholderColor = TextSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Pills
                    val filterCategories = listOf("All", "Fashion", "Sweets & Food", "Footwear", "Groceries", "Home & Living")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterCategories) { cat ->
                            val isSelected = selectedFilterCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilterCategory = cat },
                                label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoyalNavyPrimary,
                                    selectedLabelColor = SurfaceWhite
                                ),
                                shape = RoundedCornerShape(8.dp)
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
        ) {
            if (displayProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.SearchOff,
                    title = "No products found",
                    description = if (query.isNotBlank()) "We couldn't find matches for \"$query\"" else "Try selecting another category",
                    actionButtonText = "Clear Filters",
                    onActionClick = {
                        viewModel.onSearchQueryChange("")
                        selectedFilterCategory = "All"
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (query.isNotBlank()) "Results for \"$query\"" else "Showing $selectedFilterCategory Items",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextCharcoal
                    )
                    Text(
                        text = "${displayProducts.size} items",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayProducts, key = { it.id }) { product ->
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
    }
}
