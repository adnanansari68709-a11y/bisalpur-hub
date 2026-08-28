package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val userId: Long,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)
