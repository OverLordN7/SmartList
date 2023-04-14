package com.example.smartlist.ui.screens


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.PurchaseRepository
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList

class PurchaseViewModel(private val purchaseRepository: PurchaseRepository): ViewModel() {

    suspend fun getAllLists():List<PurchaseList>{
        return purchaseRepository.getAllLists()
    }

    suspend fun getItemsForPurchaseList(listId: Int): List<Item>{
        return  purchaseRepository.getItems(listId = listId)
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