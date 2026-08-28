package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productImageUrl: String,
    val price: Double,
    val quantity: Int,
    val selectedSize: String,
    val selectedColor: String
)
