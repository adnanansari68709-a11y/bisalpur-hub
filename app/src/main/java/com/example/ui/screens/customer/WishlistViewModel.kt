package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProductEntity
import com.example.domain.repository.CartRepository
import com.example.domain.repository.WishlistRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistViewModel(
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val currentUserId: StateFlow<Long?> = sessionManager.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val wishlistProducts: StateFlow<List<ProductEntity>> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                wishlistRepository.getWishlistProducts(userId)
            } else {
                MutableStateFlow(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userLoggedIn: StateFlow<Boolean> = sessionManager.currentUserId
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _showLoginPrompt = MutableStateFlow(false)
    val showLoginPrompt: StateFlow<Boolean> = _showLoginPrompt

    fun removeFromWishlist(productId: Long) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: sessionManager.getUserId()
            if (userId != null) {
                wishlistRepository.removeFromWishlist(userId, productId)
                _message.value = "Removed from Wishlist"
            }
        }
    }

    fun addToCart(product: ProductEntity) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: sessionManager.getUserId()
            if (userId != null) {
                val size = product.sizes.firstOrNull() ?: "Standard"
                val color = product.colors.firstOrNull() ?: "Default"
                cartRepository.addToCart(userId, product.id, 1, size, color)
                _message.value = "Added ${product.name} to Cart"
            } else {
                _message.value = "Please login first"
                _showLoginPrompt.value = true
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun dismissLoginPrompt() {
        _showLoginPrompt.value = false
    }
}
