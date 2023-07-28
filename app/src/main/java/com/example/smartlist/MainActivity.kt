package com.example.smartlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartlist.ui.SmartListApp
import com.example.smartlist.ui.screens.HomeViewModel
import com.example.smartlist.ui.theme.SmartListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

            val isDarkTheme by remember { mutableStateOf(homeViewModel.isDarkThemeEnabled()) }

            val toggleTheme: (Boolean) -> Unit = {homeViewModel.setDarkThemeEnabled(it)}



            SmartListTheme(darkTheme = isDarkTheme) {
                SmartListApp(homeViewModel)
                
            }
        }
    }
}