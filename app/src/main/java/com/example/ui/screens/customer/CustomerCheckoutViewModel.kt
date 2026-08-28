package com.example.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.OrderItemEntity
import com.example.domain.repository.CartRepository
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.ShopRepository
import com.example.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class CustomerCheckoutViewModel(
    private val cartRepository: CartRepository,
    private val shopRepository: ShopRepository,
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState
    
    val totalAmount = MutableStateFlow(0.0)
    private val _cartItems = MutableStateFlow<List<CartItemWithProduct>>(emptyList())
    val cartItems: StateFlow<List<CartItemWithProduct>> = _cartItems

    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount

    init {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId.firstOrNull()
            if (userId != null) {
                cartRepository.getCartItems(userId).collect { items ->
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
                    _cartItems.value = detailedItems
                    val subtotal = detailedItems.sumOf { it.productPrice * it.cartItem.quantity }
                    calculateTotal(subtotal, _appliedCoupon.value)
                }
            }
        }
    }

    fun applyCoupon(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        if (trimmed == "BISALPUR50" || trimmed == "SAVE50") {
            _appliedCoupon.value = trimmed
            val subtotal = _cartItems.value.sumOf { it.productPrice * it.cartItem.quantity }
            calculateTotal(subtotal, trimmed)
            return true
        } else if (trimmed == "FESTIVE10" || trimmed == "LOCAL10") {
            _appliedCoupon.value = trimmed
            val subtotal = _cartItems.value.sumOf { it.productPrice * it.cartItem.quantity }
            calculateTotal(subtotal, trimmed)
            return true
        }
        return false
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _discount.value = 0.0
        val subtotal = _cartItems.value.sumOf { it.productPrice * it.cartItem.quantity }
        calculateTotal(subtotal, null)
    }

    private fun calculateTotal(subtotal: Double, coupon: String?) {
        val disc = when {
            coupon in listOf("BISALPUR50", "SAVE50") -> 50.0
            coupon in listOf("FESTIVE10", "LOCAL10") -> subtotal * 0.10
            else -> 0.0
        }
        _discount.value = disc
        totalAmount.value = (subtotal - disc).coerceAtLeast(0.0)
    }

    fun placeOrder(address: String, paymentMethod: String = "Cash on Delivery") {
        if (address.isBlank()) {
            _checkoutState.value = CheckoutState.Error("Please enter delivery address")
            return
        }
        
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Processing
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _checkoutState.value = CheckoutState.Error("User not logged in")
                return@launch
            }
            
            val currentItems = _cartItems.value
            if (currentItems.isEmpty()) {
                _checkoutState.value = CheckoutState.Error("Cart is empty")
                return@launch
            }
            
            val shopId = currentItems.first().shopId
            
            val orderItems = currentItems.map {
                OrderItemEntity(
                    orderId = 0,
                    productId = it.cartItem.productId,
                    productName = it.productName,
                    productImageUrl = it.productImageUrl,
                    price = it.productPrice,
                    quantity = it.cartItem.quantity,
                    selectedSize = it.cartItem.selectedSize,
                    selectedColor = it.cartItem.selectedColor
                )
            }
            
            val finalAmount = totalAmount.value
            val result = orderRepository.placeOrder(
                userId = userId,
                shopId = shopId,
                items = orderItems,
                address = address,
                total = finalAmount,
                paymentMethod = paymentMethod
            )
            
            result.onSuccess { orderId ->
                cartRepository.clearCart(userId)
                val orderNum = "BH-${(orderId + 1000).toString().padStart(6, '0')}"
                _checkoutState.value = CheckoutState.Success(orderId = orderId, orderNumber = orderNum, amount = finalAmount)
            }.onFailure {
                _checkoutState.value = CheckoutState.Error(it.message ?: "Failed to place order")
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Processing : CheckoutState()
    data class Success(val orderId: Long = 0L, val orderNumber: String = "", val amount: Double = 0.0) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}
