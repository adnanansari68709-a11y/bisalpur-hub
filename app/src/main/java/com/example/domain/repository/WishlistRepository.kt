package com.example.domain.repository

import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WishlistRepository(
    private val favoriteDao: FavoriteDao,
    private val productDao: ProductDao
) {
    fun getWishlistProducts(userId: Long): Flow<List<ProductEntity>> {
        return favoriteDao.getWishlistProductsForUser(userId)
    }

    fun isWishlisted(userId: Long, productId: Long): Flow<Boolean> {
        return favoriteDao.isFavorite(userId, productId)
    }

    fun getWishlistedProductIds(userId: Long): Flow<List<Long>> {
        return favoriteDao.getFavoriteProductIds(userId)
    }

    suspend fun toggleWishlist(userId: Long, productId: Long): Boolean {
        val currentlyWishlisted = favoriteDao.isFavorite(userId, productId).first()
        return if (currentlyWishlisted) {
            favoriteDao.removeFavorite(userId, productId)
            false
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(userId = userId, productId = productId))
            true
        }
    }

    suspend fun addToWishlist(userId: Long, productId: Long) {
        favoriteDao.insertFavorite(FavoriteEntity(userId = userId, productId = productId))
    }

    suspend fun removeFromWishlist(userId: Long, productId: Long) {
        favoriteDao.removeFavorite(userId, productId)
    }
}
