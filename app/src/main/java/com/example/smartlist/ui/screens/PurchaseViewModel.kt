package com.example.smartlist.ui.screens


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
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
import java.io.File
import java.util.UUID

private const val TAG = "PurchaseViewModel"
sealed interface PurchaseUiState{
    data class Success(var purchaseLists: List<PurchaseList>): PurchaseUiState
    data class Error(var errorMessage: Exception): PurchaseUiState
    object Loading: PurchaseUiState
}

sealed interface PurchaseItemUiState{
    data class Success(var items: List<Item>): PurchaseItemUiState
    data class Error(var errorMessage: Exception): PurchaseItemUiState
    object Loading: PurchaseItemUiState
}
class PurchaseViewModel(private val purchaseRepository: PurchaseRepository): ViewModel() {

    var purchaseUiState: PurchaseUiState by mutableStateOf(PurchaseUiState.Loading)

    var purchaseItemUiState: PurchaseItemUiState by mutableStateOf(PurchaseItemUiState.Loading)

    var currentListId: UUID by mutableStateOf(UUID.randomUUID())

    private var currentListSize: Int by mutableIntStateOf(0)

    var currentName: String by mutableStateOf("List unknown")

    private var sharedMessage: String by mutableStateOf("none")

    init {
        getPurchaseLists()
    }

    //Purchase List functions

    private suspend fun getAllLists():List<PurchaseList>{
        return withContext(Dispatchers.IO){
            purchaseRepository.getAllLists()
        }
    }

    fun getPurchaseLists(){
        viewModelScope.launch {
            purchaseUiState = PurchaseUiState.Loading
            delay(600) // for Users to fill impact of refresh button
            purchaseUiState = try{
                PurchaseUiState.Success(getAllLists())
            }catch (e: Exception){
                PurchaseUiState.Error(e)
            }
        }
    }

    private suspend fun getItemsForPurchaseList(): List<Item>{
        return withContext(Dispatchers.IO){
            purchaseRepository.getItems(listId = currentListId).sortedBy { it.name }
        }
    }

    fun getItemsOfPurchaseList(delayValue: Long = 0){
        viewModelScope.launch {
            purchaseItemUiState = PurchaseItemUiState.Loading
            delay(delayValue)
            purchaseItemUiState = try{
                PurchaseItemUiState.Success(getItemsForPurchaseList())
            }catch (e: Exception){
                PurchaseItemUiState.Error(e)
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

    fun sharePurchaseList(messageHeader: String,currency: String,context: Context){
        viewModelScope.launch {
            val itemList: List<Item> = getItemsForPurchaseList()

            sharedMessage = "$messageHeader\n"
            itemList.forEach {item->
                sharedMessage += "${item.name} - ${item.weight} ${item.weightType}  - ${item.total} $currency\n"
            }
            Log.d(TAG,sharedMessage)

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT,"share")
            intent.putExtra(Intent.EXTRA_TEXT, sharedMessage)
            context.startActivity(Intent.createChooser(intent,"test?"))
        }
    }

    //Item functions

    fun deleteItem(itemId: UUID){
        viewModelScope.launch {
            //Get listSize from DB
            val listSize = parseListSize(currentListId)

            //Decrease value of listSize in DB
            updateListSize(listSize-1,currentListId)

            //Delete an Item from DB
            withContext(Dispatchers.IO){

                //get a copy of Item before deleting
                val item = purchaseRepository.getItemById(itemId)

                var imageUri: Uri? = null

                //convert photoPath of Item into Uri
                if (item.photoPath != null){
                    imageUri = item.photoPath!!.toUri()
                }


                purchaseRepository.deleteItem(itemId)

                //delete image from device
                if (imageUri != null) {
                    val file = File(imageUri.path!!)
                    if (file.exists()){
                        file.delete()
                    }
                }
            }

            //Refresh Item List
            getItemsOfPurchaseList()

            //Update purchaseList state
            getPurchaseLists()
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

            //Update purchaseList state
            getPurchaseLists()
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

    fun updateItemBoughtAttribute(item: Item, isBought: Boolean){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                purchaseRepository.updateItemBoughtAttribute(item,isBought)
            }

            //Refresh Item List
            getItemsOfPurchaseList()
        }
    }

    fun restoreAllItems(list: List<Item>){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                list.forEach { item ->
                    if (item.isBought){
                        purchaseRepository.updateItemBoughtAttribute(item,false)
                    }
                }
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