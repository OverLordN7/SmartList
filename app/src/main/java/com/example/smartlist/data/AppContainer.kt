package com.example.smartlist.data


import android.content.Context


interface AppContainer{
    val purchaseRepository: PurchaseRepository
}

class DefaultAppContainer(context: Context): AppContainer{


    private val db = MyDatabase.getInstance(context = context)

    private val itemDao = db.itemDao()
    private val purchaseListDao = db.purchaseListDao()

    override val purchaseRepository: PurchaseRepository by lazy{
        DefaultPurchaseRepository(itemDao,purchaseListDao)
    }
}