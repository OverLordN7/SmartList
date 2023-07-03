package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.items
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import kotlinx.coroutines.launch

@Composable
fun GraphScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier,
){

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    //Voice attributes
    val voiceState by homeViewModel.voiceToTextParser.state.collectAsState()
    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    //When switch to different screen, refresh command
    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    //Process command
    voiceCommand?.let { command->
        when(command.text){
            "список покупок"->{ navController.navigate(Screen.PurchasesScreen.route)}
            "список блюд"->{navController.navigate(Screen.DishesScreen.route)}
            "графики"->{Toast.makeText(context,"Already here", Toast.LENGTH_SHORT).show()}
            "домашняя страница"->{navController.navigate(Screen.HomeScreen.route)}
            else->{Toast.makeText(context,"Unknown command", Toast.LENGTH_SHORT).show()}
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                state = voiceState,
                onNavigationIconClick = {
                    scope.launch { scaffoldState.drawerState.open()
                    } },
                retryAction = {},
                onMicrophoneOn = {
                    if(it){
                        homeViewModel.startListening()
                        //Log.d("HomeScreen","textFromSpeech inside startListen: ${homeViewModel.textFromSpeech}")

                    }else{
                        homeViewModel.stopListening()
                    }
                }
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
                            Toast.makeText(context,"You are already on this screen", Toast.LENGTH_SHORT).show()
                        }
                        "home" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.HomeScreen.route)
                        }
                        else -> {
                            val message = context.getString(R.string.menu_item_toast_default,it.title)
                            Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Text(text = stringResource(id = R.string.graph_screen_button))
        }
    }
}