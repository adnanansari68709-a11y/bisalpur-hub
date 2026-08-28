package com.example.ui.screens.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShopEntity
import com.example.domain.repository.ShopRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SellerProductsViewModel(
    private val shopRepository: ShopRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val currentShop: StateFlow<ShopEntity?> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) shopRepository.getShopBySellerIdFlow(userId) else MutableStateFlow(null)
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val products = currentShop.flatMapLatest { shop ->
        if (shop != null) {
            shopRepository.getProductsByShop(shop.id)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addProduct(
        name: String,
        description: String,
        price: Double,
        originalPrice: Double,
        category: String,
        gender: String,
        sizes: String,
        colors: String,
        stock: Int
    ) {
        viewModelScope.launch {
            val shop = currentShop.value
            if (shop != null) {
                shopRepository.addProduct(
                    ProductEntity(
                        shopId = shop.id,
                        name = name,
                        description = description,
                        price = price,
                        originalPrice = originalPrice,
                        category = category,
                        gender = gender,
                        sizes = sizes.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        colors = colors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        stock = stock,
                        imageUrls = listOf("https://picsum.photos/400?random=${System.currentTimeMillis()}") // Placeholder
                    )
                )
            }
        }
    }
}
