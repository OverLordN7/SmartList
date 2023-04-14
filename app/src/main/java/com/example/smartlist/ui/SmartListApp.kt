package com.example.smartlist.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.screens.DishesScreen
import com.example.smartlist.ui.screens.GraphScreen
import com.example.smartlist.ui.screens.HomeScreen
import com.example.smartlist.ui.screens.PurchasesScreen
import java.time.LocalDate


private const val TAG = "SmartListApp"
@Composable
fun SmartListApp(){
    val navController = rememberNavController()


    val date = LocalDate.now()
    Log.d(TAG,"the month is ${date.month.name} and ${date.month.value} and ${date.year}")


    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){

        //Navigate to HomeScreen screen
        composable(route = Screen.HomeScreen.route){
            HomeScreen(navController)
        }

        //Navigate to PurchaseScreen screen
        composable(route = Screen.PurchasesScreen.route){
            PurchasesScreen()
        }

        //Navigate to DishesScreen screen
        composable(route = Screen.DishesScreen.route){
            DishesScreen()
        }

        //Navigate to GraphScreen screen
        composable(route = Screen.GraphScreen.route){
            GraphScreen()
        }


    }
}