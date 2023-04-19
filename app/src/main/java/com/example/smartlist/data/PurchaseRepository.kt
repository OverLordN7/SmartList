package com.example.smartlist.data

import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import java.util.*

interface PurchaseRepository{

    suspend fun getItems(listId: UUID): List<Item>

    suspend fun insertItem(item: Item)

    suspend fun getAllLists(): List<PurchaseList>

    fun insertPurchaseList(list: PurchaseList)

    fun deletePurchaseLists()
}

class DefaultPurchaseRepository(
    private val itemDao: ItemDao,
    private val purchaseListDao: PurchaseListDao,
): PurchaseRepository{

    override suspend fun getItems(listId: UUID): List<Item> {
        return itemDao.getItemsForPurchaseList(listId)
    }

    override suspend fun insertItem(item: Item) {
        itemDao.insertItem(item)
    }

    override suspend fun getAllLists(): List<PurchaseList> {
        return purchaseListDao.getAllLists()
    }

    override fun insertPurchaseList(list: PurchaseList) {
        purchaseListDao.insertPurchaseList(list)
    }

    override fun deletePurchaseLists() {
        purchaseListDao.deleteAllLists()
    }
}