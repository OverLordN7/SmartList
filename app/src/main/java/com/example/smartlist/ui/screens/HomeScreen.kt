package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.MenuItem
import com.example.smartlist.model.items
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.menu.DishAppBar
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            DishAppBar(
                onNavigationIconClick = {
                    scope.launch { scaffoldState.drawerState.open()
                    } },
                retryAction = {}
            )
        },
        drawerContent = {
            DrawerHeader()
            DrawerBody(
                items = items,
                onItemClick = {
                    when(it.id){
                        "dishList" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.DishesScreen.route)
                        }
                        "purchaseList" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.PurchasesScreen.route)
                        }
                        "graphs" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.GraphScreen.route)
                        }
                        "home" ->{
                            Toast.makeText(context,"You are already on this screen", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val message = context.getString(R.string.menu_item_toast_default,it.title)
                            Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        },
    ) {
        Surface(
            modifier = modifier
                .padding(it)
        ) {
            ScreensButtonsMenu(
                onPurchaseScreenButton = {navController.navigate(Screen.PurchasesScreen.route)},
                onDishesScreenButton = {navController.navigate(Screen.DishesScreen.route)},
                onGraphScreenButton = {navController.navigate(Screen.GraphScreen.route)}
            )
        }
    }
}

@Composable
private fun ScreensButtonsMenu(
    onPurchaseScreenButton : ()->Unit,
    onDishesScreenButton: ()->Unit,
    onGraphScreenButton: ()->Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Card(
            elevation = 4.dp,
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
        ) {
            Button(onClick = {onPurchaseScreenButton() }) {
                Text(text = stringResource(id = R.string.purchases_screen_button))
            }
        }
        Card(
            elevation = 4.dp,
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
        ) {
            Button(onClick = { onDishesScreenButton() }) {
                Text(text = stringResource(id = R.string.dishes_screen_button))
            }
        }
        Card(
            elevation = 4.dp,
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
        ) {
            Button(onClick = { onGraphScreenButton() }) {
                Text(text = stringResource(id = R.string.graph_screen_button))
            }
        }
    }
}