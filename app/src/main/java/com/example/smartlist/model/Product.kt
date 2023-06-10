package com.example.smartlist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val carb: Float = 0.0f,
    val fat: Float = 0.0f,
    val protein: Float = 0.0f,
    val cal: Float = 0.0f,
)