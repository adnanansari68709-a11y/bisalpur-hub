package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItem(cartItemId: Long)

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: Long): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId AND selectedSize = :size AND selectedColor = :color LIMIT 1")
    suspend fun getCartItem(userId: Long, productId: Long, size: String, color: String): CartItemEntity?
    
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)
}
