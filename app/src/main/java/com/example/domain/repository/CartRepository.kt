package com.example.domain.repository

import com.example.data.local.dao.CartDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

class CartRepository(
    private val cartDao: CartDao,
    private val productDao: ProductDao
) {
    fun getCartItems(userId: Long): Flow<List<CartItemEntity>> {
        return cartDao.getCartItems(userId)
    }

    suspend fun addToCart(userId: Long, productId: Long, quantity: Int, size: String, color: String) {
        val existing = cartDao.getCartItem(userId, productId, size, color)
        if (existing != null) {
            cartDao.updateCartItem(existing.copy(quantity = existing.quantity + quantity))
        } else {
            cartDao.insertCartItem(
                CartItemEntity(
                    userId = userId,
                    productId = productId,
                    quantity = quantity,
                    selectedSize = size,
                    selectedColor = color
                )
            )
        }
    }

    suspend fun updateQuantity(cartItemId: Long, newQuantity: Int, existing: CartItemEntity) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(cartItemId)
        } else {
            cartDao.updateCartItem(existing.copy(quantity = newQuantity))
        }
    }

    suspend fun removeFromCart(cartItemId: Long) {
        cartDao.deleteCartItem(cartItemId)
    }
    
    suspend fun clearCart(userId: Long) {
        cartDao.clearCart(userId)
    }
}
