package com.example.domain.repository

import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.ShopDao
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

class ShopRepository(
    private val shopDao: ShopDao,
    private val productDao: ProductDao
) {
    suspend fun createShop(shop: ShopEntity): Long {
        return shopDao.insertShop(shop)
    }

    suspend fun createShopForSeller(
        sellerId: Long,
        shopName: String,
        shopDescription: String,
        address: String = "Bisalpur Market",
        city: String = "Bisalpur",
        pincode: String = "262201"
    ): Long {
        val existing = shopDao.getShopBySellerId(sellerId)
        if (existing != null) return existing.id
        return shopDao.insertShop(
            ShopEntity(
                sellerId = sellerId,
                name = shopName.ifBlank { "My Bisalpur Store" },
                description = shopDescription.ifBlank { "Local Store in Bisalpur" },
                address = address.ifBlank { "Bisalpur Market" },
                city = city.ifBlank { "Bisalpur" },
                pincode = pincode.ifBlank { "262201" }
            )
        )
    }

    suspend fun getShopBySellerId(sellerId: Long): ShopEntity? {
        return shopDao.getShopBySellerId(sellerId)
    }
    
    fun getShopBySellerIdFlow(sellerId: Long): Flow<ShopEntity?> {
        return shopDao.getShopBySellerIdFlow(sellerId)
    }

    suspend fun addProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }
    
    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    fun getProductsByShop(shopId: Long): Flow<List<ProductEntity>> {
        return productDao.getProductsByShopId(shopId)
    }

    fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }
    
    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }
    
    suspend fun getProductById(id: Long): ProductEntity? {
        return productDao.getProductById(id)
    }
}
