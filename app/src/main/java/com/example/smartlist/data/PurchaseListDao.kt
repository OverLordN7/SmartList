package com.example.smartlist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import java.util.UUID

@Dao
interface PurchaseListDao {
    @Query("SELECT * FROM list_table")
    fun getAllLists(): List<PurchaseList>

    @Insert
    fun insertPurchaseList(list: PurchaseList)

    @Query("DELETE FROM list_table")
    fun deleteAllLists()

    @Query("UPDATE list_table SET listSize=:value WHERE id=CAST(:listId AS BLOB)")
    fun updateListSize(value: Int,listId: UUID)

    @Query("SELECT listSize FROM list_table WHERE id=CAST(:listId AS BLOB)")
    fun getListSize(listId: UUID): Int

    @Query("SELECT name FROM list_table WHERE id=CAST(:listId AS BLOB)")
    fun getListName(listId: UUID): String

    //add other CRUD functions
}

@Dao
interface ItemDao{
    @Query("SELECT * FROM item_table WHERE listId=:listId")
    fun getItemsForPurchaseList(listId:UUID): List<Item>

    @Insert
    fun insertItem(item:Item)

    @Query("DELETE FROM item_table WHERE id = CAST(:id AS BLOB)")
    fun deleteItem(id: UUID)

    @Query("UPDATE item_table SET name =:name,weight=:weight,price=:price, total =:total, listId = CAST(:listId AS BLOB) WHERE id = CAST(:id AS BLOB)")
    fun updateItem(id: UUID,name: String, weight: Float, price: Float, total: Float,listId: UUID)

    //add other CRUD functions
}