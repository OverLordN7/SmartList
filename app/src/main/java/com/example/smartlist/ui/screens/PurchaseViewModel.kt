package com.example.smartlist.ui.screens


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
import java.time.LocalDate

class PurchaseViewModel(private val purchaseRepository: PurchaseRepository): ViewModel() {

    val date = LocalDate.now()
    val list = PurchaseList(1,"List 1",0,date.year,date.month.name,date.dayOfMonth)
    val listId = list.id

    val item1 = Item(1,"Potato",10f,1500f,10f*1500,listId)
    val item2 = Item(2,"Onion",2f,800f,2f*800,listId)

    init {
        viewModelScope.launch {
            //insertPurchaseList(list)
            //insertItem(item1)
            //insertItem(item2)
            getItemsForPurchaseList(listId)
        }
    }

    suspend fun getAllLists():List<PurchaseList>{
        return purchaseRepository.getAllLists()
    }

    suspend fun getItemsForPurchaseList(listId: Int): List<Item>{
        var itemList: List<Item>

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