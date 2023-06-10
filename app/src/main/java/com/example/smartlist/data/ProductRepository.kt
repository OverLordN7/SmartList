package com.example.smartlist.data

import com.example.smartlist.model.Product

interface ProductRepository {
    suspend fun getAllProducts(): List<Product>
}

class DefaultProductRepository(
    private val productDao: ProductDao
): ProductRepository{

    override suspend fun getAllProducts(): List<Product> {
        return productDao.getAllProducts()
    }

}