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
import com.example.smartlist.data.PurchaseRepository
import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.DishList
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.model.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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



class DishViewModel (
    private val dishRepository: DishRepository,
    private val purchaseRepository: PurchaseRepository,
    ): ViewModel(){

    var dishUiState: DishUiState by mutableStateOf(DishUiState.Loading)

    var recipeUiState: RecipeUiState by mutableStateOf(RecipeUiState.Loading)

    var currentListId: UUID by mutableStateOf(UUID.randomUUID())


    private val _dishComponents = MutableStateFlow<List<DishComponent>>(emptyList())
    val dishComponents: StateFlow<List<DishComponent>> get() = _dishComponents.asStateFlow()



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

    fun convertDishListToPurchaseList(){
        //Create an empty PurchaseList

        //Prepare data of DishList
            // 1 adapt DishComponent to recipe portions
                //1.1 get a list of recipes
                //1.2 get DishesComponent associated with current recipe
                //1.3 modify weight and price
            //2 Add dc components to exportList

        viewModelScope.launch {
            var exportPurchaseList = PurchaseList(
                id = UUID.randomUUID(),
                name = "Test",
                listSize = 0,
                year = 2023,
                month = "JUNE",
                day = 2
            )

            var exportItemList: ArrayList<Item> = arrayListOf()


            var tempRecipeList: List<Recipe> = emptyList()
            withContext(Dispatchers.IO){
                tempRecipeList = getRecipeListFromDb()
            }

            tempRecipeList.forEach {
                //Log.d(TAG,"templist recipe name: ${it.name} with portions: ${it.portions}")

                var tempDCList: List<DishComponent> = emptyList()

                withContext(Dispatchers.IO){
                    tempDCList = dishRepository.getDishComponents(it.id)
                }

                tempDCList.forEach { dc->
                    dc.weight = dc.weight * it.portions
                    dc.total = dc.weight * dc.price
                    //Log.d(TAG,"DC name: ${dc.name} weight: ${dc.weight} total: ${dc.total}")

                    val isDishComponentPresent = exportItemList.find { item-> item.name == dc.name && item.price == dc.price}

                    if (isDishComponentPresent != null ){
                        isDishComponentPresent.weight += dc.weight
                    }
                    else{
                        exportItemList.add(
                            Item(
                                id = UUID.randomUUID(),
                                name = dc.name,
                                weight = dc.weight,
                                weightType = dc.weightType,
                                price = dc.price,
                                total = dc.total,
                                isBought = false,
                                listId = exportPurchaseList.id
                            )
                        )
                    }
                }
                //Log.d(TAG,"-----------------")

            }

            withContext(Dispatchers.IO){
                purchaseRepository.insertPurchaseList(exportPurchaseList)
            }

            exportItemList.forEach {
                Log.d(TAG,"exportItemList item name: ${it.name} weight: ${it.weight} total: ${it.total}")
                withContext(Dispatchers.IO){
                    purchaseRepository.insertItem(it)
                }
            }







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

            getRecipesList()
        }
    }

    private suspend fun getRecipeListFromDb(): List<Recipe>{
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
                RecipeUiState.Success(getRecipeListFromDb())
            } catch (e: Exception){
                RecipeUiState.Error
            }
        }
    }


    //Dish Component functions

    fun insertDishComponent(component: DishComponent){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.insertDishComponent(component)
            }
        }
    }



    fun loadDishComponents(recipe: Recipe) {
        viewModelScope.launch {
            dishRepository.getDishComponent(recipe.id).collect { dishComponents ->
                dishComponents.forEach {
                    it.weight = it.weight * recipe.portions
                    it.total = it.weight * it.price
                }
                _dishComponents.value = dishComponents
            }
        }
    }

    fun deleteDishComponent(id: UUID){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.deleteDishComponent(id)
            }
        }
    }

    fun updateDishComponent(dishComponent: DishComponent){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.updateDishComponent(dishComponent)
            }
        }
    }





    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartListApplication)
                val dishRepository = application.container.dishRepository
                val purchaseRepository = application.container.purchaseRepository
                DishViewModel(dishRepository, purchaseRepository)
            }
        }
    }
}