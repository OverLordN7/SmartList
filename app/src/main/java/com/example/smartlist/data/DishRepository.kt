package com.example.smartlist.data

import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.DishList
import java.util.UUID

interface DishRepository {
    suspend fun getDishComponent(listId: UUID): List<DishComponent>

    suspend fun insertDishComponent(component: DishComponent)

    suspend fun getAllLists(): List<DishList>

    fun insertDishList(list: DishList)

    fun updateListSize(value: Int,listId: UUID)

    suspend fun getListSize(listId: UUID):Int

    suspend fun deleteList(listId: UUID)

    suspend fun updateList(list: DishList)

    suspend fun deleteDishComponent(id: UUID)

    suspend fun getListName(id: UUID): String

    suspend fun updateDishComponent(component: DishComponent)

    suspend fun deleteDishComponentsAssociatedWithList(listId: UUID)

}

class DefaultDishRepository(
    private val dishComponentDao: DishComponentDao,
    private val dishListDao: DishListDao
): DishRepository {
    override suspend fun getDishComponent(listId: UUID): List<DishComponent> {
        return dishComponentDao.getDishComponentForDishList(listId)
    }

    override suspend fun insertDishComponent(component: DishComponent) {
        dishComponentDao.insertDishComponent(component)
    }

    override suspend fun getAllLists(): List<DishList> {
        return dishListDao.getAllLists()
    }

    override fun insertDishList(list: DishList) {
        dishListDao.insertDishList(list)
    }

    override fun updateListSize(value: Int, listId: UUID) {
        dishListDao.updateListSize(value, listId)
    }

    override suspend fun getListSize(listId: UUID): Int {
        return dishListDao.getListSize(listId)
    }

    override suspend fun deleteList(listId: UUID) {
        dishListDao.deleteList(listId)
    }

    override suspend fun updateList(list: DishList) {
        dishListDao.updateList(
            name = list.name,
            listId = list.id
        )
    }

    override suspend fun deleteDishComponent(id: UUID) {
        dishComponentDao.deleteDishComponent(id)
    }

    override suspend fun getListName(id: UUID): String {
        return dishListDao.getListName(id)
    }

    override suspend fun updateDishComponent(component: DishComponent) {
        dishComponentDao.updateDishComponent(
            id = component.id,
            name = component.name,
            weight = component.weight,
            weightType = component.weightType,
            listId = component.listId
        )
    }

    override suspend fun deleteDishComponentsAssociatedWithList(listId: UUID) {
        dishComponentDao.deleteDishComponentsAssociatedWithDishList(listId)
    }
}