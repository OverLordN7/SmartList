package com.example.smartlist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.DishList
import com.example.smartlist.model.Recipe
import kotlinx.coroutines.flow.Flow
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
    @Query("SELECT * FROM dish_component_table WHERE recipeId=:recipeId")
    fun getDishComponentForDishList(recipeId:UUID): Flow<List<DishComponent>>

    @Query("SELECT * FROM dish_component_table WHERE recipeId=:recipeId")
    fun getDishComponents(recipeId:UUID): List<DishComponent>

    @Query("SELECT * FROM dish_component_table WHERE id = CAST(:id AS BLOB)")
    fun getDishComponentById(id: UUID): DishComponent

    @Insert
    fun insertDishComponent(component: DishComponent)

    @Query("DELETE FROM dish_component_table WHERE id = CAST(:id AS BLOB)")
    fun deleteDishComponent(id: UUID)

    @Query("DELETE FROM dish_component_table WHERE id = CAST(:recipeId AS BLOB)")
    fun deleteDishComponentsAssociatedWithDishList(recipeId: UUID)

    @Query("UPDATE dish_component_table SET name =:name,weight=:weight, weightType=:weightType, price=:price, total=:total, carbs=:carbs, fat=:fat, protein=:protein, cal=:cal, drawableId =:drawableId, photoPath=:photoPath, recipeId = CAST(:recipeId AS BLOB) WHERE id = CAST(:id AS BLOB)")
    fun updateDishComponent(id: UUID,name: String, weight: Float, weightType: String, price:Float, total:Float, carbs: Float, fat:Float, protein:Float, cal:Float, recipeId: UUID, drawableId: Int, photoPath: String?)
}

@Dao
interface RecipeDao{
    @Insert
    fun insertRecipe(recipe: Recipe)

    @Delete
    fun deleteRecipe(recipe: Recipe)

     @Query("SELECT * FROM recipe_table WHERE listId=:listId")
    fun getRecipeForDishList(listId: UUID): List<Recipe>

    @Query("UPDATE recipe_table SET name = :name, portions= :portions, photoPath = :photoPath, description= :description WHERE id = CAST(:id AS BLOB)")
    fun updateRecipe(id: UUID, name: String, portions: Int, photoPath: String?, description: String?)
}