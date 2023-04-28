package com.example.smartlist.data

import androidx.compose.ui.Modifier
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import java.util.*

interface PurchaseRepository{

    suspend fun getItems(listId: UUID): List<Item>

    suspend fun insertItem(item: Item)

    suspend fun getAllLists(): List<PurchaseList>

    fun insertPurchaseList(list: PurchaseList)

    fun updateListSize(value: Int,listId: UUID)

    suspend fun getListSize(listId: UUID):Int

    suspend fun deleteList(listId: UUID)

    suspend fun updateList(list: PurchaseList)

    suspend fun deleteItem(id: UUID)

    suspend fun getListName(id: UUID): String

    suspend fun updateItem(item: Item)

    suspend fun deleteItemsAssociatedWithList(listId: UUID)
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

    override fun updateListSize(value: Int, listId: UUID) {
        purchaseListDao.updateListSize(value, listId)
    }

    override suspend fun getListSize(listId: UUID):Int {
        return purchaseListDao.getListSize(listId)
    }

    override suspend fun deleteList(listId: UUID) {
        return purchaseListDao.deleteList(listId)
    }

    override suspend fun updateList(list: PurchaseList) {
        return purchaseListDao.updateList(
            name = list.name,
            listId = list.id
        )
    }

    override suspend fun deleteItem(id: UUID) {
        itemDao.deleteItem(id)
    }

    override suspend fun getListName(id: UUID): String {
        return purchaseListDao.getListName(id)
    }

    override suspend fun updateItem(item: Item) {
        return itemDao.updateItem(
            id = item.id,
            name = item.name,
            weight = item.weight,
            weightType = item.weightType,
            price = item.price,
            total = item.total,
            listId = item.listId
        )
    }

    override suspend fun deleteItemsAssociatedWithList(listId: UUID) {
        itemDao.deleteItemsAssociatedWithList(listId)
    }


}