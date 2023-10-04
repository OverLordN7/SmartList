package com.example.smartlist.ui.screens

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.ProductRepository
import com.example.smartlist.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


sealed interface ProductUIState{
    data class Success(var productList: List<Product>): ProductUIState
    object Error: ProductUIState
    object Loading:ProductUIState
}


private const val TAG = "ProductViewModel"
class ProductViewModel(private val productRepository: ProductRepository): ViewModel(){

    var productUIState: ProductUIState by mutableStateOf(ProductUIState.Loading)

    init {
        getProducts()
    }

    private suspend fun getAllProducts(): List<Product>{
        return withContext(Dispatchers.IO){
            productRepository.getAllProducts().sortedBy {
                Log.d(TAG,"id: ${it.id} ${it.name} in [vm]")
                it.name
            }
        }
    }

    fun getProducts(){
        viewModelScope.launch {
            productUIState = ProductUIState.Loading
            delay(600) // for Users to fill impact of refresh button
            productUIState = try{
                ProductUIState.Success(getAllProducts())
            }catch (e: Exception){
                ProductUIState.Error
            }
        }
    }

    fun insertProduct(product: Product){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                Log.d(TAG,"id: ${product.id} ${product.name} [in vm]")
                productRepository.insertProduct(product)
            }
            //Refresh view
            getProducts()
        }
    }

    fun deleteProduct(product: Product){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                Log.d(TAG,"deleting id: ${product.id} ${product.name} [in vm]")
                productRepository.deleteProduct(product)
            }
            //Refresh view
            getProducts()
        }
    }

    fun updateProduct(product: Product){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                productRepository.updateProduct(product)
            }
            //Refresh view
            getProducts()
        }
    }


    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as SmartListApplication)
                val productRepository = application.container.productRepository
                ProductViewModel(productRepository)
            }
        }
    }
}