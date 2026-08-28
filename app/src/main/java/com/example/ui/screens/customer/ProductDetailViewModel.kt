package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ProductEntity
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ShopRepository
import com.example.domain.repository.WishlistRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productId: Long,
    private val shopRepository: ShopRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _product = MutableStateFlow<ProductEntity?>(null)
    val product: StateFlow<ProductEntity?> = _product

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _addToCartMessage = MutableStateFlow<String?>(null)
    val addToCartMessage: StateFlow<String?> = _addToCartMessage

    private val _showLoginPrompt = MutableStateFlow(false)
    val showLoginPrompt: StateFlow<Boolean> = _showLoginPrompt

    val currentUserId: StateFlow<Long?> = sessionManager.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isWishlisted: StateFlow<Boolean> = sessionManager.currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                wishlistRepository.isWishlisted(userId, productId)
            } else {
                MutableStateFlow(false)
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _isLoading.value = true
            _product.value = shopRepository.getProductById(productId)
            _isLoading.value = false
        }
    }

    fun addToCart(quantity: Int, size: String, color: String, onAdded: (() -> Unit)? = null) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: sessionManager.getUserId()
            if (userId != null) {
                cartRepository.addToCart(userId, productId, quantity, size, color)
                _addToCartMessage.value = "Added to Cart"
                onAdded?.invoke()
            } else {
                _addToCartMessage.value = "Please login first"
                _showLoginPrompt.value = true
            }
        }
    }

    fun toggleWishlist() {
        viewModelScope.launch {
            val userId = currentUserId.value ?: sessionManager.getUserId()
            if (userId != null) {
                val isAdded = wishlistRepository.toggleWishlist(userId, productId)
                _addToCartMessage.value = if (isAdded) "Added to Wishlist ❤️" else "Removed from Wishlist"
            } else {
                _addToCartMessage.value = "Please login first"
                _showLoginPrompt.value = true
            }
        }
    }

    fun dismissLoginPrompt() {
        _showLoginPrompt.value = false
    }
    
    fun clearMessage() {
        _addToCartMessage.value = null
    }
}
