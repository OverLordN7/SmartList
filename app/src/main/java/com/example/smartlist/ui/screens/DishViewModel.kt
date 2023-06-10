package com.example.smartlist.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.DishRepository
import com.example.smartlist.data.ProductRepository
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
import java.time.LocalDate
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
    private val productRepository: ProductRepository,
    ): ViewModel(){

    var dishUiState: DishUiState by mutableStateOf(DishUiState.Loading)

    var recipeUiState: RecipeUiState by mutableStateOf(RecipeUiState.Loading)

    var currentListId: UUID by mutableStateOf(UUID.randomUUID())

    var currentName: String by mutableStateOf("List unknown")

    var currentListSize: Int by mutableStateOf(0)


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

    fun convertDishListToPurchaseList(exportName: String){
        //Create an empty PurchaseList

        //Prepare data of DishList
            // 1 adapt DishComponent to recipe portions
                //1.1 get a list of recipes
                //1.2 get DishesComponent associated with current recipe
                //1.3 modify weight and price
            //2 Add dc components to exportList

        viewModelScope.launch {
            val date = LocalDate.now()
            val exportPurchaseList = PurchaseList(
                id = UUID.randomUUID(),
                name = exportName,
                listSize = 0,
                year = date.year,
                month = date.month.name,
                day = date.dayOfMonth
            )
            var listSize = 0
            val exportItemList: ArrayList<Item> = arrayListOf()


            var tempRecipeList: List<Recipe> = emptyList()
            withContext(Dispatchers.IO){
                tempRecipeList = getRecipeListFromDb()
            }

            tempRecipeList.forEach {
                var tempDCList: List<DishComponent> = emptyList()

                withContext(Dispatchers.IO){
                    tempDCList = dishRepository.getDishComponents(it.id)
                }

                tempDCList.forEach { dc->
                    dc.weight = dc.weight * it.portions
                    dc.total = dc.weight * dc.price

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
                        listSize++ // increment listSize of new Purchase list
                    }
                }
            }

            withContext(Dispatchers.IO){
                purchaseRepository.insertPurchaseList(exportPurchaseList)
                purchaseRepository.updateListSize(listSize,exportPurchaseList.id)
            }

            exportItemList.forEach {
                withContext(Dispatchers.IO){
                    purchaseRepository.insertItem(it)
                }
            }
        }
    }

    fun getListName(id: UUID){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                currentName = dishRepository.getListName(id)
            }
        }
    }

    fun getListSize(id: UUID){
        viewModelScope.launch {
            currentListSize = parseListSize(id)
        }
    }

    private suspend fun parseListSize(listId: UUID):Int{
        return withContext(Dispatchers.IO){
            dishRepository.getListSize(listId)
        }
    }

    private fun updateListSize(value: Int, listId: UUID){
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                dishRepository.updateListSize(value, listId)
            }
        }
    }

    //Recipe functions
    fun insertRecipe(recipe: Recipe){
        viewModelScope.launch {

            //Get listSize from DB
            val listSize = parseListSize(currentListId)

            //Increase value of listSize in DB
            updateListSize(listSize+1, currentListId)

            //Insert a Recipe to DB
            withContext(Dispatchers.IO){
                dishRepository.insertRecipe(recipe)
            }

            //Refresh Recipe list
            getRecipesList()
        }


    }

    fun deleteRecipe(recipe: Recipe){
        viewModelScope.launch {
            //Get listSize from DB
            val listSize = parseListSize(currentListId)

            //Decrease value of listSize in DB
            updateListSize(listSize-1, currentListId)

            //Delete a Recipe from DB
            withContext(Dispatchers.IO){
                dishRepository.deleteRecipe(recipe)
            }

            //Refresh Recipe list
            getRecipesList()
        }
    }

    fun updateRecipe(recipe: Recipe){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                dishRepository.updateRecipe(recipe)
            }

            //Refresh Recipe list
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
                val productRepository = application.container.productRepository
                DishViewModel(dishRepository, purchaseRepository, productRepository)
            }
        }
    }
}