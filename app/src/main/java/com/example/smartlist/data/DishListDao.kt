package com.example.smartlist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.DishList
import com.example.smartlist.model.Item
import java.util.UUID

@Dao
interface DishListDao {
    @Query("SELECT * FROM dished_list")
    fun getAllLists(): List<DishList>

    @Insert
    fun insertDishList(list: DishList)

    @Query("UPDATE dished_list SET listSize=:value WHERE id=CAST(:listId AS BLOB)")
    fun updateListSize(value: Int,listId: UUID)

    @Query("SELECT listSize FROM dished_list WHERE id=CAST(:listId AS BLOB)")
    fun getListSize(listId: UUID): Int

    @Query("SELECT name FROM dished_list WHERE id=CAST(:listId AS BLOB)")
    fun getListName(listId: UUID): String

    @Query("DELETE FROM dished_list WHERE id=CAST(:listId AS BLOB)")
    fun deleteList(listId: UUID)

    @Query("UPDATE dished_list SET name=:name WHERE id=CAST(:listId AS BLOB)")
    fun updateList(name: String, listId: UUID)
}

@Dao
interface DishComponentDao{
    @Query("SELECT * FROM dish_component_table WHERE listId=:listId")
    fun getDishComponentForDishList(listId:UUID): List<DishComponent>

    @Insert
    fun insertDishComponent(component: DishComponent)

    @Query("DELETE FROM dish_component_table WHERE id = CAST(:id AS BLOB)")
    fun deleteDishComponent(id: UUID)

    @Query("DELETE FROM dish_component_table WHERE id = CAST(:listId AS BLOB)")
    fun deleteDishComponentsAssociatedWithDishList(listId: UUID)

    @Query("UPDATE dish_component_table SET name =:name,weight=:weight, weightType=:weightType, listId = CAST(:listId AS BLOB) WHERE id = CAST(:id AS BLOB)")
    fun updateDishComponent(id: UUID,name: String, weight: Float,weightType: String,listId: UUID)
}