package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sellerId: Long,
    val name: String,
    val description: String = "",
    val address: String = "Bisalpur Market",
    val city: String = "Bisalpur",
    val pincode: String = "262201",
    val logoUrl: String? = null
)
