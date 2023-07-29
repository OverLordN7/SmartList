package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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


    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    val isDarkTheme by homeViewModel.isDarkThemeEnabled.collectAsState()

    val toggleTheme: (Boolean) -> Unit = {homeViewModel.setDarkThemeEnabled(it)}


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
                name = stringResource(id = R.string.settings),
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

            Column {
                DarkModeCard(isDarkTheme = isDarkTheme, toggleTheme = toggleTheme)
                VoiceCommandListCard()
            }
        }
    }
}

@Composable
fun DarkModeCard(isDarkTheme: Boolean, toggleTheme: (Boolean)->Unit, modifier: Modifier = Modifier){
    Card(
        elevation = 4.dp,
        modifier = modifier.padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = stringResource(id = R.string.appearance),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.dark_theme_mode),
                    modifier = Modifier.weight(2f)
                )

                Spacer(modifier = Modifier.weight(2f))

                Switch(
                    checked = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = {
                        toggleTheme(!isDarkTheme)
                    }
                )

            }

        }

    }
}

@Composable
fun VoiceCommandListCard(modifier: Modifier = Modifier){

    val isExpanded = remember { mutableStateOf(false) }

    Card(
        elevation = 4.dp,
        modifier = modifier.padding(8.dp)
    ) {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        text = stringResource(id = R.string.voice_commands),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(4f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(onClick = { isExpanded.value = !isExpanded.value}) {
                        Icon(
                            imageVector = if (isExpanded.value )Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = ""
                        )
                    }
                }
            }
            item { 
                if (isExpanded.value){
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = stringResource(R.string.command_hint),
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            text = stringResource(R.string.navigation),
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                        )
                        Text(
                            text = stringResource(R.string.break_line),
                            fontSize = 16.sp,
                        )
                        Text(text = " - "+stringResource(id = R.string.home_screen))
                        Text(text = " - "+stringResource(id = R.string.purchase_screen))
                        Text(text = " - "+stringResource(id = R.string.dish_screen))
                        Text(text = " - "+stringResource(id = R.string.graph_screen))
                        Text(text = " - "+stringResource(id = R.string.settings_screen))

                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            text = stringResource(R.string.creation),
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                        )
                        Text(
                            text = stringResource(R.string.break_line),
                            fontSize = 16.sp,
                        )

                        Text(
                            text = " - "
                                    + stringResource(id = R.string.create_command)
                                    + " "
                                    + stringResource(id = R.string.create_command_parameter_new)
                                    + " "
                                    + stringResource(id = R.string.create_command_object)
                        )
                        Text(text = stringResource(R.string.command_in)
                                + stringResource(id = R.string.purchase_screen)
                                + ","
                                + stringResource(id = R.string.dish_screen),
                            fontWeight = FontWeight.Bold
                        )

                    }
                }
            }
        }
    }
}