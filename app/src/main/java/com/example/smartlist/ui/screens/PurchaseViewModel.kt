package com.example.smartlist.ui.screens


import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
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

    init {
        getPurchaseLists()
    }

    private suspend fun getAllLists():List<PurchaseList>{
        var purchaseList: List<PurchaseList>
        withContext(Dispatchers.IO){
            purchaseList =  purchaseRepository.getAllLists()
        }
        return  purchaseList
    }

    suspend fun getItemsForPurchaseList(listId: UUID): List<Item>{
        var itemList: List<Item> = emptyList()

        withContext(Dispatchers.IO){
            itemList = purchaseRepository.getItems(listId = listId)
        }

        return itemList
    }

    private suspend fun insertPurchaseList(list: PurchaseList){
        withContext(Dispatchers.IO){
            purchaseRepository.insertPurchaseList(list)
        }
    }

    fun insertNewPurchaseList(list: PurchaseList){
        viewModelScope.launch {
            insertPurchaseList(list)
        }
    }

    suspend fun insertItem(item: Item){
        withContext(Dispatchers.IO){
            purchaseRepository.insertItem(item)
        }
    }

    suspend fun deleteItem(id: UUID){
        withContext(Dispatchers.IO){
            purchaseRepository.deleteItem(id)
        }
    }

    fun deleteItemFromDb(id: UUID){
        viewModelScope.launch {
            deleteItem(id)
        }
    }

    suspend fun parseListSize(listId: UUID):Int{
        return withContext(Dispatchers.IO){
            purchaseRepository.getListSize(listId)
        }
    }

    fun getListSize(id: UUID){
        viewModelScope.launch {
            currentListSize = parseListSize(id)
        }
    }

    private suspend fun updateListSize(value: Int, listId: UUID){
        withContext(Dispatchers.IO){
            purchaseRepository.updateListSize(value, listId)
        }
    }

    private fun updateListSizeFromDb(value: Int, listId: UUID){
        viewModelScope.launch {
            updateListSize(value, listId)
        }
    }

    fun updateItemInfo(item: Item, id: UUID){
        insertItemToDb(item)
        updateListSizeFromDb(currentListSize+1,id)
    }

    fun getPurchaseLists(){
        viewModelScope.launch {
            purchaseUiState = PurchaseUiState.Loading
            Log.d(TAG,"State in getPurchasesList() is $purchaseUiState")
            purchaseUiState = try{
                PurchaseUiState.Success(getAllLists())
            }catch (e: Exception){
                Log.d(TAG,"State out of getPurchasesList() is $purchaseUiState with exception $e")
                PurchaseUiState.Error
            }
        }
    }

    fun getItemsOfPurchaseList(listId: UUID){
        viewModelScope.launch {
            purchaseItemUiState = PurchaseItemUiState.Loading
            Log.d(TAG,"State in getItemsOfPurchaseList is $purchaseUiState")
            purchaseItemUiState = try{
                PurchaseItemUiState.Success(getItemsForPurchaseList(listId))
            }catch (e: Exception){
                Log.d(TAG,"State in getItemsOfPurchaseList is $purchaseUiState")
                PurchaseItemUiState.Error
            }
            delay(1500)
        }
    }

    fun insertItemToDb(item: Item){
        viewModelScope.launch {
            insertItem(item)
        }
    }

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