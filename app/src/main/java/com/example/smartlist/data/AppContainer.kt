package com.example.smartlist.data


import android.content.Context
import androidx.room.Room


interface AppContainer{
    val purchaseRepository: PurchaseRepository
}

class DefaultAppContainer(private val applicationContext: Context): AppContainer{

    private val database by lazy{
        Room.databaseBuilder(
            applicationContext,
            MyDatabase::class.java,
            "my-database"
        ).build()
    }

    private val purchaseListDao by lazy { database.purchaseListDao()}

    private val itemDao by lazy { database.itemDao() }

    override val purchaseRepository: PurchaseRepository by lazy{
        DefaultPurchaseRepository(itemDao,purchaseListDao)
    }
}