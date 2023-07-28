package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
){
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val state by homeViewModel.voiceToTextParser.state.collectAsState()

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    //Navigation attributes
    val navigationMessage = stringResource(id = R.string.navigation_message)


    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    voiceCommand?.let { command->
        homeViewModel.processNavigationCommand(
            command = command,
            currentScreen = context.getString(R.string.home_screen),
            navController = navController,
            context = context
        )
    }



    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                state = state,
                onNavigationIconClick = { scope.launch { scaffoldState.drawerState.open() } },
                retryAction = {},
                onMicrophoneOn = {
                    if(it){ homeViewModel.startListening() }

                    else { homeViewModel.stopListening() }
                }
            )
        },
        drawerContent = {
            DrawerHeader()
            DrawerBody(
                items = myItems,
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
                            Toast.makeText(context,navigationMessage, Toast.LENGTH_SHORT).show()
                        }
                        "settings"->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.SettingScreen.route)
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
        Surface(modifier = modifier.padding(it)) {

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
        modifier = modifier.fillMaxWidth().padding(8.dp)
    ) {

        Card(elevation = 4.dp, modifier = Modifier.weight(1f).height(120.dp).padding(8.dp)) {
            Button(onClick = onPurchaseScreenButton) {
                Text(text = stringResource(id = R.string.purchases_screen_button))
            }
        }

        Card(elevation = 4.dp, modifier = Modifier.weight(1f).height(120.dp).padding(8.dp)) {
            Button(onClick = onDishesScreenButton) {
                Text(text = stringResource(id = R.string.dishes_screen_button))
            }
        }

        Card(elevation = 4.dp, modifier = Modifier.weight(1f).height(120.dp).padding(8.dp)) {
            Button(onClick = onGraphScreenButton) {
                Text(text = stringResource(id = R.string.graph_screen_button))
            }
        }
    }
}