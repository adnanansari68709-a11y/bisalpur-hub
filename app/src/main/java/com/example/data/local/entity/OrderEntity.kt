package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val userId: Long,
    val shopId: Long, // Simple model: one shop per order. If cart has multiple shops, split into multiple orders.
    val totalAmount: Double,
    val deliveryAddress: String,
    val status: String, // Placed, Confirmed, Preparing, Ready for Delivery, Out for Delivery, Delivered, Cancelled
    val paymentMethod: String = "Cash on Delivery", // Cash on Delivery, Online UPI
    val createdAt: Long = System.currentTimeMillis(),
    val cancelReason: String? = null,
    val cancelledAt: Long? = null,
    val refundStatus: String? = null // e.g. "Refund Pending", "Refunded"
)

