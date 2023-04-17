package com.example.smartlist.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.screens.*
import kotlinx.coroutines.launch
import java.time.LocalDate


private const val TAG = "SmartListApp"
@Composable
fun SmartListApp(){
    val navController = rememberNavController()

    val purchaseViewModel: PurchaseViewModel = viewModel(factory = PurchaseViewModel.Factory)


//    LaunchedEffect(key1 = "test"){
//        coroutineScope.launch {
//            val db = MyDatabase.getInstance(context = context)
//            val purchaseListDao = db.purchaseListDao()
//            val itemDao = db.itemDao()
//
//            val date = LocalDate.now()
//            val list = PurchaseList(1,"List 1",0,date.year,date.month.name,date.dayOfMonth)
//
//            purchaseListDao.insertPurchaseList(list)
//            val listId = list.id
//
//            val item1 = Item(1,"Potato",10f,1500f,10f*1500,listId)
//            val item2 = Item(2,"Onion",2f,800f,2f*800,listId)
//
//            itemDao.insertItem(item1)
//            itemDao.insertItem(item2)
//
//            val lists = purchaseListDao.getAllLists()
//
//            lists.forEach {
//                Log.d(TAG,"${it.name}")
//            }
//        }
//    }







    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){

        //Navigate to HomeScreen screen
        composable(route = Screen.HomeScreen.route){
            HomeScreen(navController)
        }

        //Navigate to PurchaseScreen screen
        composable(route = Screen.PurchasesScreen.route){
            PurchasesScreen(navController)
        }

        //Navigate to DishesScreen screen
        composable(route = Screen.DishesScreen.route){
            DishesScreen()
        }

        //Navigate to GraphScreen screen
        composable(route = Screen.GraphScreen.route){
            GraphScreen()
        }

        //Navigate to DetailedPurchaseListScreen Screen
        composable(
            route = Screen.DetailedPurchaseListScreen.route +"/{list_id}",
            arguments = listOf(
                navArgument("list_id"){
                    type = NavType.StringType
                    nullable = false
                },
            )
        ){
            DetailedPurchaseListScreen(
                listId = it.arguments?.getString("list_id")!!,
            )
        }


    }
}