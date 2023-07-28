package com.example.smartlist.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun SettingsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
){
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val state by homeViewModel.voiceToTextParser.state.collectAsState()

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    //Navigation attributes
    val navigationMessage = stringResource(id = R.string.navigation_message)
    val navigationTransition = stringResource(id = R.string.navigation_transition)
    val unknownVoiceCommandMessage = stringResource(id = R.string.unknown_command)


    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    val isDarkTheme by remember { mutableStateOf(homeViewModel.isDarkThemeEnabled()) }

    val toggleTheme: (Boolean) -> Unit = {homeViewModel.setDarkThemeEnabled(it)}

    val theme = remember { mutableStateOf(false) }

    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    voiceCommand?.let { command->
        homeViewModel.processNavigationCommand(
            command = command,
            currentScreen = context.getString(R.string.settings_screen),
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
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.HomeScreen.route)
                        }
                        "settings"->{
                            Toast.makeText(context,navigationMessage, Toast.LENGTH_SHORT).show()
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



            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = "Settings")

                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = {
                        toggleTheme(!isDarkTheme)
                        homeViewModel.themeTest = !homeViewModel.themeTest
                    }
                )

            }


        }
    }
}