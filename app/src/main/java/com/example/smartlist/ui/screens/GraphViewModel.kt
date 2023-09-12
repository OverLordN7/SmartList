package com.example.smartlist.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.R
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
    data class Success(
        var purchaseMap: Map<Float,PurchaseList>,
        var monthDataList: List<MonthData>,
    ): GraphUiState

    object Error: GraphUiState

    object Loading: GraphUiState
}

data class MonthData(
    val month: String,
    val monthShort: String,
    var data: Int = 0,
)


class GraphViewModel(
    val purchaseRepository: PurchaseRepository,
    val dishRepository: DishRepository,
    application: SmartListApplication
): AndroidViewModel(application) {

    var graphUiState: GraphUiState by mutableStateOf(GraphUiState.Loading)
    var context: Context by mutableStateOf(application.applicationContext)



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

    private suspend fun getPurchaseLists(context: Context): List<MonthData>{

        var resultList: List<MonthData> = listOf(
            MonthData(
                month = context.getString(R.string.month_jan),
                monthShort = context.getString(R.string.month_jan_short)
            ),
            MonthData(
                month = context.getString(R.string.month_feb),
                monthShort = context.getString(R.string.month_feb_short)
            ),
            MonthData(
                month = context.getString(R.string.month_mar),
                monthShort = context.getString(R.string.month_mar_short)
            ),
            MonthData(
                month = context.getString(R.string.month_apr),
                monthShort = context.getString(R.string.month_apr_short)
            ),
            MonthData(
                month = context.getString(R.string.month_may),
                monthShort = context.getString(R.string.month_may_short)
            ),
            MonthData(
                month = context.getString(R.string.month_jun),
                monthShort = context.getString(R.string.month_jun_short)
            ),
            MonthData(
                month = context.getString(R.string.month_jul),
                monthShort = context.getString(R.string.month_jul_short)
            ),
            MonthData(
                month = context.getString(R.string.month_aug),
                monthShort = context.getString(R.string.month_aug_short)
            ),
            MonthData(
                month = context.getString(R.string.month_sep),
                monthShort = context.getString(R.string.month_sep_short)
            ),
            MonthData(
                month = context.getString(R.string.month_oct),
                monthShort = context.getString(R.string.month_oct_short)
            ),
            MonthData(
                month = context.getString(R.string.month_nov),
                monthShort = context.getString(R.string.month_nov_short)
            ),
            MonthData(
                month = context.getString(R.string.month_dec),
                monthShort = context.getString(R.string.month_dec_short)
            ),

        )

        //Get a full list of purchase lists
        val purchaseLists: List<PurchaseList> = withContext(Dispatchers.IO){
            purchaseRepository.getAllLists()
        }

        //Calculate entries in each month
        purchaseLists.forEach { purchaseList ->
            resultList.forEach { monthData ->
                if (purchaseList.month == monthData.month)
                    monthData.data++
            }
        }

//        resultList.forEach {
//            Log.d(TAG,"month: ${it.month} and data: ${it.data}")
//        }

        return resultList
    }

    fun getPurchaseListsForGraph(context: Context){
        viewModelScope.launch {
            getPurchaseLists(context)
        }
    }


    fun getPurchaseListsMap(){
        viewModelScope.launch {
            graphUiState = GraphUiState.Loading
            delay(600) // for Users to fill impact of refresh button
            graphUiState = try{
                GraphUiState.Success(
                    purchaseMap = getExpensivePurchaseLists(),
                    monthDataList = getPurchaseLists(context),
                    )
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
                    application = application,
                )
            }
        }
    }
}