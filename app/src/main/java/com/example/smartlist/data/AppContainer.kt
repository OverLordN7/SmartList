package com.example.smartlist.data


import android.content.Context
import androidx.room.Room


interface AppContainer{
    val purchaseRepository: PurchaseRepository
    val dishRepository: DishRepository
    val productRepository: ProductRepository
}

class DefaultAppContainer(private val applicationContext: Context): AppContainer{

    private val database by lazy{
        // Temporary comment such implementation of db
//        Room.databaseBuilder(
//            applicationContext,
//            MyDatabase::class.java,
//            "my-database"
//        ).build()
        MyDatabase.getInstance(applicationContext)
    }

    private val purchaseListDao by lazy { database.purchaseListDao()}

    private val itemDao by lazy { database.itemDao() }

    private val dishListDao by lazy { database.dishListDao() }

    private val dishComponentDao by lazy { database.dishComponentDao() }

    private val recipeDao by lazy { database.recipeDao()}

    private val productDao by lazy { database.productDao() }

    override val purchaseRepository: PurchaseRepository by lazy{
        DefaultPurchaseRepository(itemDao,purchaseListDao)
    }

    override val dishRepository: DishRepository by lazy{
        DefaultDishRepository(dishComponentDao,dishListDao,recipeDao)
    }

    override val productRepository: ProductRepository by lazy {
        DefaultProductRepository(productDao)
    }
}