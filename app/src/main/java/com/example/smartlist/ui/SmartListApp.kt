package com.example.smartlist.ui

import android.os.LocaleList
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartlist.R
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.screens.DetailedDishesScreen
import com.example.smartlist.ui.screens.DetailedPurchaseListScreen
import com.example.smartlist.ui.screens.DishViewModel
import com.example.smartlist.ui.screens.DishesScreen
import com.example.smartlist.ui.screens.GraphScreen
import com.example.smartlist.ui.screens.GraphViewModel
import com.example.smartlist.ui.screens.HomeScreen
import com.example.smartlist.ui.screens.HomeViewModel
import com.example.smartlist.ui.screens.ProductScreen
import com.example.smartlist.ui.screens.ProductViewModel
import com.example.smartlist.ui.screens.PurchaseViewModel
import com.example.smartlist.ui.screens.PurchasesScreen
import com.example.smartlist.ui.screens.SettingsScreen
import java.util.Locale


private const val TAG = "SmartListApp"
@Composable
fun SmartListApp(
    homeViewModel: HomeViewModel
){
    val navController = rememberNavController()

    //Change language configuration of the app
    val context = LocalContext.current
    val resources = context.resources

    val currentLanguage = homeViewModel.currentLanguage.collectAsState()

    val configuration = resources.configuration
    configuration.setLocale(Locale(currentLanguage.value))

    val localeList = LocaleList(Locale(currentLanguage.value))
    configuration.setLocales(localeList)

    val localeListCompat = LocaleListCompat.create(Locale(currentLanguage.value))

    ConfigurationCompat.setLocales(configuration,localeListCompat)

    val contextWithUpdatedConfig = ContextThemeWrapper(context, R.style.Theme_SmartList)
    contextWithUpdatedConfig.resources.updateConfiguration(configuration, resources.displayMetrics)


    //ViewModel setup
    val purchaseViewModel: PurchaseViewModel = viewModel(factory = PurchaseViewModel.Factory)
    val dishViewModel: DishViewModel = viewModel(factory = DishViewModel.Factory)
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory)
    val graphViewModel: GraphViewModel = viewModel(factory = GraphViewModel.Factory)

    NavHost(navController = navController, startDestination = Screen.HomeScreen.route){

        //Navigate to HomeScreen screen
        composable(route = Screen.HomeScreen.route){
            HomeScreen(
                navController = navController,
                homeViewModel = homeViewModel,
            )
        }

        //Navigate to PurchaseScreen screen
        composable(route = Screen.PurchasesScreen.route){
            PurchasesScreen(
                navController = navController,
                purchaseViewModel = purchaseViewModel,
                homeViewModel = homeViewModel,
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
                homeViewModel = homeViewModel,
                onSubmit = dishViewModel::insertDishList,
                onEdit = dishViewModel::updateDishList,
                onDelete = dishViewModel::deleteDishList,
                onRefresh = dishViewModel::getDishLists,
            )
        }

        //Navigate to GraphScreen screen
        composable(route = Screen.GraphScreen.route){
            GraphScreen(
                navController = navController,
                homeViewModel = homeViewModel,
                graphViewModel = graphViewModel,
                onRetryAction = graphViewModel::getPurchaseListsMap
            )
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
                purchaseViewModel = purchaseViewModel,
                homeViewModel = homeViewModel,
                navController = navController,
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
                homeViewModel = homeViewModel,
                navController = navController,
                onDelete = dishViewModel::deleteRecipe,
                onSubmit = dishViewModel::updateRecipe,
                onRefresh = dishViewModel::getRecipesList,
                addNewRecipe = dishViewModel::insertRecipe,
                insertNewDishComponent = dishViewModel::insertDishComponent,
                loadDishComponent = dishViewModel::loadDishComponents,
                deleteDishComponent = dishViewModel::deleteDishComponent,
                onEdit = dishViewModel::updateDishComponent,
                onExport = dishViewModel::convertDishListToPurchaseList
            )
        }

        //Navigate to Settings Screen
        composable(
            route = Screen.SettingScreen.route
        ){
            SettingsScreen(
                navController = navController,
                homeViewModel = homeViewModel,
            )
        }

        //Navigate to ProductScreen
        composable(
            route = Screen.ProductScreen.route
        ){
            ProductScreen(
                homeViewModel = homeViewModel,
                productViewModel = productViewModel,
                navController = navController,
                onConfirm = productViewModel::insertProduct,
                onDelete = productViewModel::deleteProduct,
                onUpdate = productViewModel::updateProduct,
                onRefresh = productViewModel::getProducts,
            )
        }


    }
}