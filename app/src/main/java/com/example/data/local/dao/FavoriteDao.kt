package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND productId = :productId")
    suspend fun removeFavorite(userId: Long, productId: Long)

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesForUser(userId: Long): Flow<List<FavoriteEntity>>
    
    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE userId = :userId AND productId = :productId")
    fun isFavorite(userId: Long, productId: Long): Flow<Boolean>

    @Query("SELECT productId FROM favorites WHERE userId = :userId")
    fun getFavoriteProductIds(userId: Long): Flow<List<Long>>

    @Query("SELECT p.* FROM products p INNER JOIN favorites f ON p.id = f.productId WHERE f.userId = :userId ORDER BY f.id DESC")
    fun getWishlistProductsForUser(userId: Long): Flow<List<com.example.data.local.entity.ProductEntity>>
}
