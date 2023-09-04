package com.example.smartlist.ui.screens

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
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "GraphViewModel"
sealed interface GraphUiState{
    data class Success(var purchaseMap: Map<Float,PurchaseList>): GraphUiState

    object Error: GraphUiState

    object Loading: GraphUiState
}


class GraphViewModel(
    val purchaseRepository: PurchaseRepository,
    val dishRepository: DishRepository
): ViewModel() {

    var graphUiState: GraphUiState by mutableStateOf(GraphUiState.Loading)

    init {
        getPurchaseListsMap()
    }

    private suspend fun getExpensivePurchaseLists(): Map<Float,PurchaseList> {

        var resultMap: MutableMap<Float,PurchaseList> = emptyMap<Float,PurchaseList>().toMutableMap()

        val purchaseLists: List<PurchaseList> = withContext(Dispatchers.IO){
            purchaseRepository.getAllLists()
        }

        // Algorithm which sort and extract from List<PurchaseList> 4 top most expensive lists.
        // with values.

        purchaseLists.forEach {purchaseList ->
            var itemList: List<Item>
            var total = 0.0f
            withContext(Dispatchers.IO){
                //Get all items associated with list
                itemList = purchaseRepository.getItems(purchaseList.id)
                // Calculate all item total sum
                itemList.forEach { item->
                    total+=item.total
                }
            }
            resultMap[total] = purchaseList
        }

        //Sort map in reverse order to get 4 top most expensive lists
        resultMap = resultMap.toSortedMap(Comparator.reverseOrder())


        //if size of map bigger than 4, remove all unnecessary entries
        if (resultMap.size > 4){
            while (resultMap.size > 4){
                resultMap.remove(resultMap.keys.last())
            }
        }

        return resultMap.toMap()
    }

    fun getPurchaseListsMap(){
        viewModelScope.launch {
            graphUiState = GraphUiState.Loading
            delay(600) // for Users to fill impact of refresh button
            graphUiState = try{
                GraphUiState.Success(getExpensivePurchaseLists())
            } catch (e: Exception){
                GraphUiState.Error
            }
        }
    }



    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartListApplication)
                val purchaseRepository = application.container.purchaseRepository
                val dishRepository = application.container.dishRepository
                GraphViewModel(
                    purchaseRepository = purchaseRepository,
                    dishRepository = dishRepository,
                )
            }
        }
    }
}