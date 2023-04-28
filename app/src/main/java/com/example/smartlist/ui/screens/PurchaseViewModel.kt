package com.example.smartlist.ui.screens


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.PurchaseRepository
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val TAG = "PurchaseViewModel"

sealed interface PurchaseUiState{
    data class Success(var purchaseLists: List<PurchaseList>): PurchaseUiState
    object Error: PurchaseUiState
    object Loading: PurchaseUiState
}

sealed interface PurchaseItemUiState{
    data class Success(var items: List<Item>): PurchaseItemUiState

    object Error: PurchaseItemUiState

    object Loading: PurchaseItemUiState
}
class PurchaseViewModel(private val purchaseRepository: PurchaseRepository): ViewModel() {

    var purchaseUiState: PurchaseUiState by mutableStateOf(PurchaseUiState.Loading)

    var purchaseItemUiState: PurchaseItemUiState by mutableStateOf(PurchaseItemUiState.Loading)

    var currentListId: UUID by mutableStateOf(UUID.randomUUID())

    var currentListSize: Int by mutableStateOf(0)

    var currentName: String by mutableStateOf("List unknown")

    init {
        getPurchaseLists()
    }

    //Purchase List functions

    private suspend fun getAllLists():List<PurchaseList>{
        var purchaseList: List<PurchaseList>
        withContext(Dispatchers.IO){
            purchaseList =  purchaseRepository.getAllLists()
        }
        return  purchaseList
    }

    fun getPurchaseLists(){
        viewModelScope.launch {
            purchaseUiState = PurchaseUiState.Loading
            purchaseUiState = try{
                PurchaseUiState.Success(getAllLists())
            }catch (e: Exception){
                PurchaseUiState.Error
            }
        }
    }

    private suspend fun getItemsForPurchaseList(): List<Item>{
        var itemList: List<Item> = emptyList()

        withContext(Dispatchers.IO){
            itemList = purchaseRepository.getItems(listId = currentListId)
        }

        return itemList
    }

    fun getItemsOfPurchaseList(){
        viewModelScope.launch {
            purchaseItemUiState = PurchaseItemUiState.Loading
            purchaseItemUiState = try{
                PurchaseItemUiState.Success(getItemsForPurchaseList())
            }catch (e: Exception){
                PurchaseItemUiState.Error
            }

        }
    }

    fun insertPurchaseList(list: PurchaseList){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                purchaseRepository.insertPurchaseList(list)
            }

            //Refresh List
            getPurchaseLists()
        }
    }

    fun deletePurchaseList(listId: UUID){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                purchaseRepository.deleteItemsAssociatedWithList(listId)
                purchaseRepository.deleteList(listId)
            }

            //Refresh List
            getPurchaseLists()
        }
    }

    fun updateList(list: PurchaseList){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                purchaseRepository.updateList(list)
            }

            //Refresh List
            getPurchaseLists()
        }
    }


    fun getListName(id: UUID){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                currentName = purchaseRepository.getListName(id)
            }
        }
    }

    private suspend fun parseListSize(listId: UUID):Int{
        return withContext(Dispatchers.IO){
            purchaseRepository.getListSize(listId)
        }
    }

    fun getListSize(id: UUID){
        viewModelScope.launch {
            currentListSize = parseListSize(id)
        }
    }


    private fun updateListSize(value: Int, listId: UUID){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                purchaseRepository.updateListSize(value, listId)
            }
        }
    }

    //End of Purchase List functions

    //Item functions

    fun deleteItem(itemId: UUID){
        viewModelScope.launch {
            //Get listSize from DB
            val listSize = parseListSize(currentListId)

            //Decrease value of listSize in DB
            updateListSize(listSize-1,currentListId)

            //Delete an Item from DB
            withContext(Dispatchers.IO){
                purchaseRepository.deleteItem(itemId)
            }

            //Refresh Item List
            getItemsOfPurchaseList()
        }
    }


    fun insertItem(item: Item){
        viewModelScope.launch {
            //Get listSize from DB
            val listSize = parseListSize(currentListId)

            //Increase value of listSize in DB
            updateListSize(listSize+1,currentListId)

            //Insert an Item from DB
            withContext(Dispatchers.IO){
                purchaseRepository.insertItem(item)
            }

            //Refresh Item List
            getItemsOfPurchaseList()
        }
    }

    fun updateItemInDb(item: Item){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                purchaseRepository.updateItem(item)
            }

            //Refresh Item List
            getItemsOfPurchaseList()
        }
    }
    //End of Item functions

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SmartListApplication)
                val purchaseRepository = application.container.purchaseRepository
                PurchaseViewModel(purchaseRepository)
            }
        }
    }
}