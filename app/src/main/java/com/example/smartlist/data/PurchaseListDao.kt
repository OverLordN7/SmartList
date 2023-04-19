package com.example.smartlist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList

@Dao
interface PurchaseListDao {
    @Query("SELECT * FROM list_table")
    fun getAllLists(): List<PurchaseList>

    @Insert
    fun insertPurchaseList(list: PurchaseList)

    @Query("DELETE FROM list_table")
    fun deleteAllLists()

    //add other CRUD functions
}

@Dao
interface ItemDao{
    @Query("SELECT * FROM item_table WHERE listId=:listId")
    fun getItemsForPurchaseList(listId:Int): List<Item>

    @Insert
    fun insertItem(item:Item)

    //add other CRUD functions
}