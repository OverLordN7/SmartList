package com.example.smartlist.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartlist.SmartListApplication
import com.example.smartlist.data.DishRepository

class DishViewModel (private val dishRepository: DishRepository): ViewModel(){



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