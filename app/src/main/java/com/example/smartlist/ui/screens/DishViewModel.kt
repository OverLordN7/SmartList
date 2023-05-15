package com.example.smartlist.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.DishRepository
import com.example.smartlist.model.DishList
import com.example.smartlist.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


private const val TAG = "DishViewModel"
sealed interface DishUiState{
    data class Success(var dishList: List<DishList>): DishUiState
    object Error: DishUiState
    object Loading: DishUiState
}

sealed interface RecipeUiState{
    data class Success(var recipeList: List<Recipe>): RecipeUiState
    object Error: RecipeUiState
    object Loading: RecipeUiState
}



class DishViewModel (private val dishRepository: DishRepository): ViewModel(){

    var dishUiState: DishUiState by mutableStateOf(DishUiState.Loading)

    var recipeUiState: RecipeUiState by mutableStateOf(RecipeUiState.Loading)

    var currentListId: UUID by mutableStateOf(UUID.randomUUID())


    //DishList Functions

    init{
        getDishLists()
    }

    private suspend fun getAllLists(): List<DishList>{
        var dishList: List<DishList>
        withContext(Dispatchers.IO){
            dishList = dishRepository.getAllLists()
        }
        return dishList
    }

    fun getDishLists(){
        viewModelScope.launch {
            dishUiState = DishUiState.Loading
            dishUiState = try{
                DishUiState.Success(getAllLists())
            } catch (e: Exception){
                DishUiState.Error
            }
        }
    }

    fun insertDishList(list: DishList){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.insertDishList(list)
            }

            //Refresh DishList
            getDishLists()
        }
    }

    fun updateDishList(list: DishList){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.updateList(list)
            }

            //Refresh List
            getDishLists()
        }
    }

    fun deleteDishList(listId: UUID){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.deleteDishComponentsAssociatedWithList(listId)
                dishRepository.deleteList(listId)
            }

            //Refresh List
            getDishLists()
        }
    }

    //Recipe functions
    fun insertRecipe(recipe: Recipe){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.insertRecipe(recipe)
            }

            getRecipesList()
        }


    }

    fun deleteRecipe(recipe: Recipe){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.deleteRecipe(recipe)
            }

            getRecipesList()
        }
    }

    fun updateRecipe(recipe: Recipe){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.updateRecipe(recipe)
            }


            Log.d(TAG, "update is called")
            Log.d(TAG,"current status UIstate is $recipeUiState")

            getRecipesList()

            Log.d(TAG,"current status UIstate is $recipeUiState after refresh")
        }
    }

    private suspend fun getRecipeList(): List<Recipe>{
        var recipeList: List<Recipe> = emptyList()
        withContext(Dispatchers.IO){
            recipeList = dishRepository.getRecipes(listId = currentListId)
        }
        return recipeList
    }

    fun getRecipesList(){
        viewModelScope.launch {
            recipeUiState = RecipeUiState.Loading

            recipeUiState = try {
                RecipeUiState.Success(getRecipeList())
            } catch (e: Exception){
                RecipeUiState.Error
            }
        }
    }



    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartListApplication)
                val dishRepository = application.container.dishRepository
                DishViewModel(dishRepository)
            }
        }
    }
}