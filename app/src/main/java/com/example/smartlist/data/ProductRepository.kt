package com.example.smartlist.data

import com.example.smartlist.model.Product

interface ProductRepository {
    suspend fun getAllProducts(): List<Product>

    suspend fun insertProduct(product: Product)

    suspend fun updateProduct(product: Product)

    suspend fun deleteProduct(product: Product)
}

class DefaultProductRepository(
    private val productDao: ProductDao
): ProductRepository{

    override suspend fun getAllProducts(): List<Product> {
        return productDao.getAllProducts()
    }

    override suspend fun insertProduct(product: Product) {
        productDao.addProduct(product)
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(
            id = product.id,
            name = product.name,
            carb = product.carb,
            fat = product.fat,
            protein = product.protein,
            cal = product.cal
        )
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.id)
    }

}