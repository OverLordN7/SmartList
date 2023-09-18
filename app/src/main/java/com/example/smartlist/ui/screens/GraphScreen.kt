package com.example.smartlist.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
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
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.ui.charts.bar_graph.BarGraph
import com.example.smartlist.ui.charts.DonutChart
import com.example.smartlist.ui.charts.bar_graph.BarType
import com.example.smartlist.ui.common_composables.LoadingScreen
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import com.example.smartlist.ui.theme.Carb200
import com.example.smartlist.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GraphScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    graphViewModel: GraphViewModel,
    onRetryAction: ()-> Unit,
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

    val state: GraphUiState = graphViewModel.graphUiState

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
                },
                isRetryActionEnabled = true,
                retryAction = onRetryAction

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
        Surface(modifier = modifier.padding(it)) {
            when(state){
                is GraphUiState.Loading -> LoadingScreen()
                is GraphUiState.Error -> {}
                is GraphUiState.Success ->{
                    Column {
                        if (state.purchaseMap.isEmpty() || state.monthDataList.isEmpty()){
                            EmptyScreen()
                        } else{
                            DonutGraphCard(state.purchaseMap)
                            BarGraphCard(state.monthDataList)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScreen(modifier: Modifier = Modifier){
    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
            .padding(4.dp)
            .fillMaxSize()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = stringResource(R.string.empty_message_1))
                Text(text = stringResource(R.string.empty_message_2))
            }
        }
    }
}

@Composable
fun DonutGraphCard(data: Map<Float, PurchaseList>, modifier: Modifier = Modifier){

    var colors = listOf(
        Carb200,
        Fat200,
        Protein200,
        Cal200
    )

    val totalValueList = data.keys.toList()
    val totalValueListName = data.values.toList()

    if (colors.size > totalValueList.size-1){
        colors = colors.subList(0,totalValueList.size)
    }

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
                Text(text = stringResource(R.string.the_most_expensive_lists), fontSize = 20.sp, fontWeight = FontWeight.Bold )
            }

            //Body of Donut graph
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart(
                    colors = colors,
                    inputValues = totalValueList,
                    textColor = Color.Black,
                    modifier = Modifier
                        .size(150.dp)
                        .padding(4.dp)
                        .weight(0.6f)
                )

                LazyColumn(modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)){
                    items(totalValueList.size){
                        DonutGraphCardItem(
                            color = colors[it],
                            item = totalValueList[it],
                            name = totalValueListName[it].name,
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun DonutGraphCardItem(
    color: Color,
    item: Float,
    name:String,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(30.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .size(30.dp)
                .background(color)
                .weight(1f)
                .padding(start = 4.dp))
            //Spacer(modifier = Modifier.weight(0.5f))
            Text(text = name, modifier = Modifier
                .weight(3f)
                .padding(start = 4.dp))
            Text(text = normalizeTotalValue(item,context), modifier = Modifier.weight(4f))
        }
    }
}

@Composable
fun BarGraphCard(monthDataList: List<MonthData>,modifier: Modifier = Modifier){
    Card(
        elevation = 4.dp,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    ){
        val yAxisValuesList: ArrayList<Int> = arrayListOf()
        val graphBarValues = mutableListOf<Float>()

        // fill x-Axis coordinates with values
        val xAxisValuesListAlternative = listOf(
            stringResource(id = R.string.month_jan_short),
            stringResource(id = R.string.month_feb_short),
            stringResource(id = R.string.month_mar_short),
            stringResource(id = R.string.month_apr_short),
            stringResource(id = R.string.month_may_short),
            stringResource(id = R.string.month_jun_short),
            stringResource(id = R.string.month_jul_short),
            stringResource(id = R.string.month_aug_short),
            stringResource(id = R.string.month_sep_short),
            stringResource(id = R.string.month_oct_short),
            stringResource(id = R.string.month_nov_short),
            stringResource(id = R.string.month_dec_short),

        ).toMutableList()

        monthDataList.forEach { monthData ->
            yAxisValuesList.add((monthData.data)) // fill y-Axis coordinates with values
        }

        yAxisValuesList.forEachIndexed { index,value ->
            graphBarValues.add(index = index, element = value.toFloat()/yAxisValuesList.max().toFloat())
        }


        Column(modifier = Modifier.padding(4.dp)) {
            Text(text = stringResource(R.string.graph_bar_title), fontSize = 20.sp, fontWeight = FontWeight.Bold )
            BarGraph(
                graphBarData = graphBarValues,
                xAxisScaleData = xAxisValuesListAlternative,
                barDataValue = yAxisValuesList,
                height = 300.dp,
                roundType = BarType.TOP_CURVED,
                barWidth = 20.dp,
                barColor = MaterialTheme.colors.primary,
                barArrangement = Arrangement.SpaceEvenly
            )
        }
    }
}

fun normalizeTotalValue(value: Float, context: Context): String{

    return when(value.toInt()){
        //Range between 100K and 1M
        in 99999..999999 ->{
            val tempResult: Int = (value/1000).roundToInt() // 300K
            var tempReminder = value/1000 - tempResult //0.250K
            tempReminder = ((tempReminder * 1000).roundToInt() / 1000.0).toFloat() //round to 3 digits
            tempReminder = ((tempReminder * 100.0).roundToInt() / 100.0).toFloat() //round to 2 digits
            val result: Float = tempResult + tempReminder

            "${result}K " + context.getString(R.string.currency_title)
        }
        //Range between 1M and 1B
        in 999999..1999999999 -> {
            val tempResult: Int = (value/1000000).roundToInt() // 300K
            var tempReminder = value/1000000 - tempResult //0.250K
            tempReminder = ((tempReminder * 1000).roundToInt() / 1000.0).toFloat() //round to 3 digits
            tempReminder = ((tempReminder * 100.0).roundToInt() / 100.0).toFloat() //round to 2 digits
            val result: Float = tempResult + tempReminder

            "${result}M " + context.getString(R.string.currency_title)
        }
        //Range between 0 and 100K
        else -> {
            "${value.toInt()} " + context.getString(R.string.currency_title)
        }
    }

}