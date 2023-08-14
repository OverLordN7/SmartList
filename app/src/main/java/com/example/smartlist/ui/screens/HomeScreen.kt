package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.ui.unit.sp

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
                    scope.launch { scaffoldState.drawerState.close() }
                    homeViewModel.processDrawerBodyCommand(
                        item = it,
                        currentScreen = "home",
                        context = context,
                        navController = navController,
                    )
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
    val context = LocalContext.current

    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            Card(elevation = 4.dp, modifier = Modifier
                .weight(1f)
                .height(120.dp)
                .padding(8.dp)) {
                Button(onClick = onPurchaseScreenButton) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBasket,
                            contentDescription = "",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = stringResource(id = R.string.purchases_screen_button))
                    }
                }
            }

            Card(elevation = 4.dp, modifier = Modifier
                .weight(1f)
                .height(120.dp)
                .padding(8.dp)) {
                Button(onClick = onDishesScreenButton) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = "",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = stringResource(id = R.string.dishes_screen_button))
                    }
                }
            }

            Card(elevation = 4.dp, modifier = Modifier
                .weight(1f)
                .height(120.dp)
                .padding(8.dp)) {
                Button(onClick = onGraphScreenButton) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = stringResource(id = R.string.graph_screen_button))
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Card(elevation = 4.dp,
                backgroundColor = MaterialTheme.colors.primary,
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .padding(8.dp)
            ) {
                Button(onClick = { Toast.makeText(context,"Table will be here",Toast.LENGTH_SHORT).show()}) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Cookie,
                            contentDescription = "",
                            modifier = Modifier.size(40.dp)
                        )
                        Text(text = stringResource(id = R.string.cal_table_button), fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier
                .weight(1f)
                .height(120.dp)
                .padding(8.dp))
            Spacer(modifier = Modifier
                .weight(1f)
                .height(120.dp)
                .padding(8.dp))
        }
    }
}