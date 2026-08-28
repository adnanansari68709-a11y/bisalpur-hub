package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shopId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val category: String,
    val gender: String, // Men, Women, Kids, Unisex
    val sizes: List<String>,
    val colors: List<String>,
    val stock: Int,
    val imageUrls: List<String>, // We'll just use the same string list converter
    val createdAt: Long = System.currentTimeMillis()
)
