package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CartItemEntity
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ShopRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItemWithProduct(
    val cartItem: CartItemEntity,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val availableStock: Int,
    val shopId: Long
)

class CustomerCartViewModel(
    private val cartRepository: CartRepository,
    private val shopRepository: ShopRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val currentUserId = sessionManager.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _cartItemsWithProducts = MutableStateFlow<List<CartItemWithProduct>>(emptyList())
    val cartItemsWithProducts: StateFlow<List<CartItemWithProduct>> = _cartItemsWithProducts

    val totalAmount = MutableStateFlow(0.0)

    init {
        viewModelScope.launch {
            currentUserId.flatMapLatest { userId ->
                if (userId != null) {
                    cartRepository.getCartItems(userId)
                } else {
                    MutableStateFlow(emptyList())
                }
            }.collect { items ->
                val detailedItems = items.mapNotNull { item ->
                    val product = shopRepository.getProductById(item.productId)
                    if (product != null) {
                        CartItemWithProduct(
                            cartItem = item,
                            productName = product.name,
                            productPrice = product.price,
                            productImageUrl = product.imageUrls.firstOrNull() ?: "",
                            availableStock = product.stock,
                            shopId = product.shopId
                        )
                    } else null
                }
                _cartItemsWithProducts.value = detailedItems
                totalAmount.value = detailedItems.sumOf { it.productPrice * it.cartItem.quantity }
            }
        }
    }

    fun updateQuantity(item: CartItemEntity, newQuantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(item.id, newQuantity, item)
        }
    }

    fun removeCartItem(item: CartItemEntity) {
        viewModelScope.launch {
            cartRepository.removeFromCart(item.id)
        }
    }
}
