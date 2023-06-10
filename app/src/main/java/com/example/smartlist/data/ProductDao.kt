package com.example.smartlist.data

import androidx.room.Dao
import androidx.room.Query
import com.example.smartlist.model.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM product_table")
    fun getAllProducts(): List<Product>
}