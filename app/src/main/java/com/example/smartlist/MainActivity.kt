package com.example.smartlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartlist.ui.SmartListApp
import com.example.smartlist.ui.screens.HomeViewModel
import com.example.smartlist.ui.theme.SmartListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

            WrapperSmartListApp(homeViewModel = homeViewModel)
        }
    }
}

@Composable
fun WrapperSmartListApp(homeViewModel: HomeViewModel){
    val isDarkTheme by homeViewModel.isDarkThemeEnabled.collectAsState()
    SmartListTheme(darkTheme = isDarkTheme) {
        SmartListApp(homeViewModel)
    }
}