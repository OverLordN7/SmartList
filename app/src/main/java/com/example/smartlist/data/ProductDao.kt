package com.example.smartlist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartlist.model.Product
import java.util.UUID

@Dao
interface ProductDao {
    @Query("SELECT * FROM product_table")
    fun getAllProducts(): List<Product>

    @Insert
    fun addProduct(product: Product)

    @Query("UPDATE product_table SET name = :name, carb = :carb, fat = :fat, protein = :protein, cal = :cal WHERE id = CAST(:id AS BLOB) ")
    fun updateProduct(id: UUID, name: String, carb: Float, fat: Float, protein: Float, cal: Float)

    @Query("DELETE FROM product_table WHERE id = CAST(:productID AS BLOB)")
    fun deleteProduct(productID: UUID)
}