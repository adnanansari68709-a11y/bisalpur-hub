package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity): Long

    @Query("SELECT * FROM shops WHERE sellerId = :sellerId LIMIT 1")
    suspend fun getShopBySellerId(sellerId: Long): ShopEntity?
    
    @Query("SELECT * FROM shops WHERE sellerId = :sellerId LIMIT 1")
    fun getShopBySellerIdFlow(sellerId: Long): Flow<ShopEntity?>

    @Query("SELECT * FROM shops WHERE id = :shopId LIMIT 1")
    suspend fun getShopById(shopId: Long): ShopEntity?

    @Query("SELECT * FROM shops")
    suspend fun getAllShops(): List<ShopEntity>
}
