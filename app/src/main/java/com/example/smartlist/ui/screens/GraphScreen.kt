package com.example.smartlist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.ui.charts.DonutChart
import com.example.smartlist.ui.charts.PieChart
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import com.example.smartlist.ui.theme.Carb200
import com.example.smartlist.ui.theme.*
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

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    //Voice attributes
    val voiceState by homeViewModel.voiceToTextParser.state.collectAsState()
    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    //When switch to different screen, refresh command
    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    //Process command
    voiceCommand?.let { command->
        homeViewModel.processNavigationCommand(
            command = command,
            currentScreen = context.getString(R.string.graph_screen),
            navController = navController,
            context = context
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                state = voiceState,
                onNavigationIconClick = { scope.launch { scaffoldState.drawerState.open() } },
                onMicrophoneOn = {
                    if(it){ homeViewModel.startListening() }

                    else{ homeViewModel.stopListening() }
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
                        currentScreen = "graphs",
                        context = context,
                        navController = navController,
                    )
                }
            )
        }
    ) {
        Surface(modifier = modifier
            .fillMaxSize()
            .padding(it)) {
            Column {
                Text(text = stringResource(id = R.string.graph_screen_button))

                DonutGraphCard()

//                PieChart(
//                    data = mapOf(
//                        Pair("A",30),
//                        Pair("B",10),
//                        Pair("C",75),
//                        Pair("D",100),
//                        Pair("E",120),
//                    ),
//                    modifier = Modifier.size(200.dp)
//                )
            }
        }
    }
}

@Composable
fun DonutGraphCard(modifier: Modifier = Modifier){

    val colors = listOf(
        Carb200,
        Fat200,
        Protein200,
        Cal200
    )

    val dummy = listOf(
        60f,
        110f,
        20f,
        35f
    )


    Card(
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),

    ) {
        Column {

            // Title of Donut Graph
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = "The most expensive lists in purchase screen", fontSize = 20.sp, fontWeight = FontWeight.Bold )
            }

            //Body of Donut graph
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    colors = colors,
                    inputValues = dummy,
                    textColor = Color.Black,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(4.dp)
                        .weight(1f)
                )

                LazyColumn(modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)){
                    items(dummy.size){
                        DonutGraphCardItem(color = colors[it], data = dummy[it].toString())
                    }
                }
            }
        }

    }
}

@Composable
fun DonutGraphCardItem(color: Color, data: String,modifier: Modifier = Modifier){
    Card(modifier = modifier
        .padding(4.dp)
        .fillMaxWidth()) {
        Row {
            Box(modifier = Modifier
                .size(20.dp)
                .background(color)
                .weight(1f))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = data, modifier = Modifier.weight(3f))
        }
    }
}