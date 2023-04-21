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



    val date = LocalDate.now()
    val list = PurchaseList(
        name = "List 1",
        listSize = 0,
        year = date.year,
        month = date.month.name,
        day = date.dayOfMonth
    )
    val listId = list.id

    val item1 = Item(
        name = "Potato",
        weight = 10f,
        price = 1500f,
        total = 10f*1500,
        listId = listId
    )
    val item2 = Item(
        name = "Onion",
        weight = 2f,
        price = 800f,
        total = 2f*800,
        listId = listId
    )

    init {
//        viewModelScope.launch {
//            insertPurchaseList(list)
//            insertItem(item1)
//            insertItem(item2)
//            //getItemsForPurchaseList(listId)
//        }
        getPurchaseLists()

    }

    suspend fun getAllLists():List<PurchaseList>{
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

    suspend fun insertPurchaseList(list: PurchaseList){
        withContext(Dispatchers.IO){
            purchaseRepository.insertPurchaseList(list)
        }
    }

    suspend fun insertItem(item: Item){
        withContext(Dispatchers.IO){
            purchaseRepository.insertItem(item)
        }
    }

    fun getListSize(listId: UUID):Int{
        var listSize: Int = 0
        Log.d(TAG,"init value of $listSize")
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                listSize = purchaseRepository.getListSize(listId)
                Log.d(TAG,"temp value of $listSize")
            }
        }
        Log.d(TAG,"final value of $listSize")
        return listSize
    }

    suspend fun updateListSize(value: Int, listId: UUID){
        withContext(Dispatchers.IO){
            purchaseRepository.updateListSize(value, listId)
        }
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

    fun updateListSizeFromDb(value: Int,listId: UUID){
        viewModelScope.launch {
            updateListSizeFromDb(value, listId)
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