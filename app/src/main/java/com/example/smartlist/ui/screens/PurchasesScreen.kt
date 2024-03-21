package com.example.smartlist.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.extend_functions.capitalizeFirstChar
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.common_composables.ErrorScreen
import com.example.smartlist.ui.common_composables.LoadingScreen
import com.example.smartlist.ui.menu.CustomBottomAppBar
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID


@Composable
fun PurchasesScreen(
    navController: NavController,
    purchaseViewModel: PurchaseViewModel,
    homeViewModel: HomeViewModel,
    onSubmit: (PurchaseList) -> Unit,
    onRefresh: () -> Unit,
    onEdit: (PurchaseList) -> Unit,
    onDelete: (UUID) -> Unit,
    modifier: Modifier = Modifier,
){
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val state: PurchaseUiState = purchaseViewModel.purchaseUiState

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    //Voice attributes
    val voiceState by homeViewModel.voiceToTextParser.state.collectAsState()
    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    if (showDialog.value){
        NewPurchaseListDialog(
            setShowDialog = {showDialog.value = it},
            onConfirm = {newPurchaseList ->
                onSubmit(newPurchaseList)
            }
        )
    }

    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    val themeMode = homeViewModel.isDarkThemeEnabled.collectAsState()

    voiceCommand?.let { command->

        val parts = command.text.split(" ")

        if (parts.size>=3 && parts[0] == "создай" && parts[1] == "новый" && parts[2] == "список"){
            val newPurchaseListName:String  = parts.subList(3,parts.size).joinToString("")
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("LLLL", Locale.getDefault())
            val systemMonth = currentDate.format(formatter)
            val newPurchaseList = PurchaseList(
                name = newPurchaseListName.capitalizeFirstChar(),
                listSize = 0,
                year = currentDate.year,
                month = systemMonth.capitalizeFirstChar(),
                monthValue = currentDate.monthValue,
                day = currentDate.dayOfMonth
            )
            onSubmit(newPurchaseList)
            homeViewModel.clearVoiceCommand()
        }
        else{
            homeViewModel.processNavigationCommand(
                command = command,
                currentScreen = context.getString(R.string.purchase_screen),
                navController = navController,
                context = context,
            )
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                state = voiceState,
                onNavigationIconClick = { scope.launch { scaffoldState.drawerState.open() } },
                retryAction = onRefresh,
                onMicrophoneOn = {
                    if(it){ homeViewModel.startListening() }

                    else{ homeViewModel.stopListening() }
                },
                isRetryActionEnabled = true,
            )
        },
        bottomBar = {
            CustomBottomAppBar(
                navController = navController,
                isFabExist = true,
                context = context
            )
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true}) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add_new_purchase_list)
                )
            }
        },
        drawerContent = {
            DrawerHeader()
            DrawerBody(
                items = myItems,
                onItemClick = {
                    scope.launch { scaffoldState.drawerState.close() }
                    homeViewModel.processDrawerBodyCommand(
                        item = it,
                        currentScreen = "purchaseList",
                        context = context,
                        navController = navController,
                    )
                }
            )
        }
    ) { it ->
        Surface(modifier = modifier.padding(it)) {
            when(state){
                is PurchaseUiState.Loading -> LoadingScreen()
                is PurchaseUiState.Error -> ErrorScreen(state.errorMessage)
                is PurchaseUiState.Success ->{
                    ResultScreen(
                        lists = state.purchaseLists,
                        themeMode = themeMode.value,
                        onClick = {
                            purchaseViewModel.currentListId = it
                            purchaseViewModel.getItemsOfPurchaseList()
                            purchaseViewModel.getListSize(it)
                            purchaseViewModel.getListName(it)
                            navController.navigate(Screen.DetailedPurchaseListScreen.withArgs(it.toString()))
                        },
                        onEdit = onEdit,
                        onDelete = onDelete,
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
fun ResultScreen(
    lists: List<PurchaseList>,
    themeMode: Boolean,
    onClick: (UUID) -> Unit,
    onEdit: (PurchaseList) -> Unit,
    onDelete: (UUID) -> Unit,
){
    //If no Item received but call ended with Success
    if (lists.isEmpty()) {
        EmptyListCard()
        return
    }

    LazyColumn{
        items(lists.size){ index ->
            NewListCard(
                list = lists[index],
                themeMode = themeMode,
                onClick = { onClick(lists[index].id)},
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
fun NewListCard(
    list: PurchaseList,
    themeMode: Boolean,
    onClick: (UUID) -> Unit,
    onEdit: (PurchaseList) -> Unit,
    onDelete: (UUID) -> Unit,
    modifier: Modifier = Modifier
){

    val isChangeMode = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imgAssets = listOf(
        R.drawable.img_1,
        R.drawable.img_2,
        R.drawable.img_3,
        R.drawable.img_4,
        R.drawable.img_5,
    )

    val imgAsset = when(list.drawableId){
        1 -> imgAssets[1]
        2 -> imgAssets[2]
        3 -> imgAssets[3]
        4 -> imgAssets[4]
        else->{
            imgAssets[0]
        }
    }

    val customShape = GenericShape{ size, _ ->
        val width = size.width
        val height = size.height
        // construct shape from position where start is up left corner 0,0
        // and right bottom corner is width,height
        lineTo(width*0.3.toFloat(),0f)
        lineTo(width*0.6.toFloat(),height)
        lineTo(0f,height)
        close()
    }

    val dateCustomShape = GenericShape{size,_ ->
        val width = size.width
        val height = size.height
        lineTo(width*0.75.toFloat(),0f)
        //lineTo(width,height*0.75.toFloat())
        lineTo(width*0.9.toFloat(),height)
        lineTo(0f,height)
        close()
    }

    if (!isChangeMode.value){
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp)
                .clickable {
                    onClick(list.id)
                },
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(id = imgAsset),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.graphicsLayer {
                        clip = true
                        shape = customShape
                    }
                )

                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp
                    ) {
                        Column {
                            Row(modifier = Modifier.weight(1f)){
                                Text(
                                    text = list.name,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(3f)
                                )
                                Spacer(modifier = Modifier.weight(2f))
                                IconButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { isChangeMode.value = !isChangeMode.value }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(id = R.string.edit_current_list)
                                    )
                                }

                                IconButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onDelete(list.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.delete_current_list)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(3f))
                            Row(modifier = Modifier.weight(1f)){
                                Box(
                                    modifier = Modifier
                                        .weight(3f)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            shape = dateCustomShape
                                        )
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = list.month +" "+ list.year,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Light,
                                        color = Color.White,
                                        //modifier = Modifier.weight(3f)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(3f))
                                Box(
                                    modifier = Modifier.weight(2f).padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 4.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Text(
                                        text = context.getString(
                                            R.string.card_items,
                                            list.listSize
                                        ),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                        color = if (themeMode) Color.White else Color.Black,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp),
            elevation = 4.dp
        ){
            NewEditScreen(list = list, isExpanded = isChangeMode, onSubmit = onEdit)
        }
    }
}

@Composable
fun EmptyListCard(modifier: Modifier = Modifier){
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = stringResource(id = R.string.empty_card_message_1),
                color = Color.Black
            )
            Text(
                text = stringResource(id = R.string.empty_card_message_2),
                color = Color.Black
            )
        }
    }
}

@Composable
fun NewEditScreen(
    list: PurchaseList,
    isExpanded: MutableState<Boolean>,
    onSubmit: (PurchaseList) -> Unit,
    modifier: Modifier = Modifier
){
    var name by remember { mutableStateOf(TextFieldValue(list.name)) }
    var errorMessage by remember { mutableStateOf(false) }


    Log.d("Purchase", "hash code of ${list.name} is ${list.name.hashCode()}")

    Card(
        modifier = modifier.padding(8.dp),
        elevation = 0.dp
    ) {
        Column{
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(3f)
            ) {
                Text(
                    text = stringResource(id = R.string.purchaselist_name_title),
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.weight(0.5f))

                OutlinedTextField(
                    value = name,
                    onValueChange = {name = it},
                    placeholder = {Text(text = stringResource(id = R.string.new_purchase_list_name_hint))},
                    modifier = Modifier.weight(2f),
                )
            }
            Spacer(modifier = Modifier.weight(2.5f))

            //Error field
            Row {
                if (errorMessage){
                    Text(
                        text = stringResource(id = R.string.error_message),
                        color = Color.Red,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                } else{
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Row(
                modifier = Modifier.weight(3f)
            ) {
                Spacer(modifier = Modifier.weight(3f))

                Button(
                    onClick = { isExpanded.value = false },
                    modifier = Modifier
                        .weight(2f)
                        .padding(4.dp),
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text(text = stringResource(id = R.string.button_cancel))
                }

                Button(
                    onClick = {
                        //Check if all fields are not null
                        if (name.text.isBlank()){
                            errorMessage = true
                        }
                        else{
                            val newList = PurchaseList(
                                id = list.id,
                                name = name.text,
                                listSize = list.listSize,
                                year = list.year,
                                month = list.month,
                                monthValue = list.monthValue,
                                day = list.day
                            )
                            onSubmit(newList)
                            isExpanded.value = false
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .padding(4.dp),
                    colors = ButtonDefaults.buttonColors(Color.Green)
                ) {
                    Text(text = stringResource(id = R.string.button_confirm))
                }

            }
        }
    }
}

@Composable
fun EditScreen(
    list: PurchaseList,
    isExpanded: MutableState<Boolean>,
    onSubmit: (PurchaseList) -> Unit,
    modifier: Modifier = Modifier
){
    var name by remember { mutableStateOf(TextFieldValue(list.name)) }
    var errorMessage by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(4.dp)) {

        //Name field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = stringResource(id = R.string.purchaselist_name_title),
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {name = it},
                placeholder = {Text(text = stringResource(id = R.string.new_purchase_list_name_hint))},
                modifier = Modifier.weight(2f),
            )
        }

        //Error field
        Row {
            if (errorMessage){
                Text(
                    text = stringResource(id = R.string.error_message),
                    color = Color.Red,
                    modifier = Modifier.padding(start = 12.dp)
                )
            } else{
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        //Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Button(onClick = { isExpanded.value = false }, modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.button_cancel))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    //Check if all fields are not null
                    if (name.text.isBlank()){
                        errorMessage = true
                    }
                    else{
                        val newList = PurchaseList(
                            id = list.id,
                            name = name.text,
                            listSize = list.listSize,
                            year = list.year,
                            month = list.month,
                            monthValue = list.monthValue,
                            day = list.day
                        )
                        onSubmit(newList)
                        isExpanded.value = false
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(id = R.string.button_confirm))
            }
        }
    }
}
@Composable
fun NewPurchaseListDialog(
    setShowDialog: (Boolean) -> Unit,
    onConfirm: (PurchaseList) -> Unit,
    modifier: Modifier = Modifier,
){
    var fieldValue by remember{ mutableStateOf(TextFieldValue("")) }
    var errorFieldStatus by remember { mutableStateOf(false) }


    Dialog(onDismissRequest = {setShowDialog(false)}) {

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(8.dp)
            ) {

                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = {fieldValue = it},
                    placeholder = {Text(text = stringResource(id = R.string.new_purchase_list_name_hint))},
                    label = {
                        Text(
                            text = stringResource(id = R.string.new_purchase_list_name_title),
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )

                if (errorFieldStatus){
                    Text(
                        text = stringResource(id = R.string.error_message),
                        color = Color.Red,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .height(40.dp)
                    )
                }
                else{ Spacer(modifier = Modifier.height(40.dp)) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ){

                    Button(onClick = { setShowDialog(false)}, modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)) {
                        Text(text = stringResource(id = R.string.button_cancel))
                    }

                    Button(
                        onClick = {
                            //Check if all fields are not null
                            if (fieldValue.text.isBlank()){ errorFieldStatus = true }
                            else{
                                val date = LocalDate.now()

                                val formatter = DateTimeFormatter.ofPattern("LLLL", Locale.getDefault())
                                val systemMonth = date.format(formatter)


                                val list = PurchaseList(
                                    name = fieldValue.text.capitalizeFirstChar(),
                                    listSize = 0,
                                    year = date.year,
                                    month = systemMonth.capitalizeFirstChar(),
                                    monthValue = date.monthValue,
                                    day = date.dayOfMonth
                                )
                                onConfirm(list)
                                setShowDialog(false)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                    ) {
                        Text(text = stringResource(id = R.string.button_confirm))
                    }
                }
            }
        }
    }
}

