package com.example.smartlist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
){
    Scaffold() {
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