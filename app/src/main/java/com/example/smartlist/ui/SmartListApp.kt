package com.example.smartlist.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.screens.*


private const val TAG = "SmartListApp"
@Composable
fun SmartListApp(){
    val navController = rememberNavController()

    val purchaseViewModel: PurchaseViewModel = viewModel(factory = PurchaseViewModel.Factory)

    val dishViewModel: DishViewModel = viewModel(factory = DishViewModel.Factory)


    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){

        //Navigate to HomeScreen screen
        composable(route = Screen.HomeScreen.route){
            HomeScreen(navController)
        }

        //Navigate to PurchaseScreen screen
        composable(route = Screen.PurchasesScreen.route){
            PurchasesScreen(
                navController = navController,
                purchaseViewModel = purchaseViewModel,
                onSubmit = purchaseViewModel::insertPurchaseList,
                onRefresh = purchaseViewModel::getPurchaseLists,
                onEdit = purchaseViewModel::updateList,
                onDelete = purchaseViewModel::deletePurchaseList,
            )
        }

        //Navigate to DishesScreen screen
        composable(route = Screen.DishesScreen.route){
            DishesScreen(
                navController = navController,
                dishViewModel = dishViewModel,
                onSubmit = dishViewModel::insertDishList,
                onEdit = dishViewModel::updateDishList,
                onDelete = dishViewModel::deleteDishList
            )
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
                purchaseViewModel = purchaseViewModel,
                onSubmit = purchaseViewModel::insertItem,
                onRefresh = purchaseViewModel::getItemsOfPurchaseList,
                onDelete = purchaseViewModel::deleteItem,
                onEdit = purchaseViewModel::updateItemInDb,
                onItemBoughtChanged = purchaseViewModel::updateItemBoughtAttribute
            )
        }

        //Navigate to DetailedPurchaseListScreen Screen
        composable(
            route = Screen.DetailedDishesScreen.route
        ){
            DetailedDishesScreen(
                dishViewModel = dishViewModel,
                navController = navController,
                onDelete = dishViewModel::deleteRecipe,
                onSubmit = dishViewModel::updateRecipe,
                onRefresh = dishViewModel::getRecipesList,
                addNewRecipe = dishViewModel::insertRecipe,
                insertNewDishComponent = dishViewModel::insertDishComponent,
                loadDishComponent = dishViewModel::loadDishComponents
            )
        }



    }
}