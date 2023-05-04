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
import com.example.smartlist.model.DishList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface DishUiState{
    data class Success(var dishList: List<DishList>): DishUiState
    object Error: DishUiState
    object Loading: DishUiState
}



class DishViewModel (private val dishRepository: DishRepository): ViewModel(){

    var dishUiState: DishUiState by mutableStateOf(DishUiState.Loading)


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



    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartListApplication)
                val dishRepository = application.container.dishRepository
                DishViewModel(dishRepository)
            }
        }
    }
}