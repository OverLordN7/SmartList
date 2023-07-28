package com.example.smartlist.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.navigation.Screen
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

    //Navigation attributes
    val navigationMessage = stringResource(id = R.string.navigation_message)


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

    voiceCommand?.let { command->

        val parts = command.text.split(" ")

        if (parts.size>=3 && parts[0] == "создай" && parts[1] == "новый" && parts[2] == "список"){
            val newPurchaseListName:String  = parts.subList(3,parts.size).joinToString("")
            val currentDate = LocalDate.now()
            val newPurchaseList = PurchaseList(
                name = newPurchaseListName,
                listSize = 0,
                year = currentDate.year,
                month = currentDate.month.name,
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
                            Toast.makeText(context,navigationMessage, Toast.LENGTH_SHORT).show()
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
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.SettingScreen.route)
                        }
                        else -> {
                            val message = context.getString(R.string.menu_item_toast_default,it.title)
                            Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true}) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add_new_purchase_list)
                )
            }
        }
    ) { it ->
        Surface(modifier = modifier.padding(it)) {
            when(state){
                is PurchaseUiState.Loading ->{}
                is PurchaseUiState.Error ->{}
                is PurchaseUiState.Success ->{
                    ResultScreen(
                        lists = state.purchaseLists,
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
            }
        }
    }
}

@Composable
fun ResultScreen(
    lists: List<PurchaseList>,
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
            ListCard(
                list = lists[index],
                onClick = { onClick(lists[index].id)},
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}


@Composable
fun ListCard(
    list: PurchaseList,
    onClick: (UUID) -> Unit,
    onEdit: (PurchaseList) -> Unit,
    onDelete: (UUID) -> Unit,
    modifier: Modifier = Modifier
){
    val isExpanded = remember { mutableStateOf(false) }

    Card(
        elevation = 4.dp,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                onClick(list.id)
            }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(4.dp)
            ) {

                Column(modifier = Modifier.weight(5f)) {

                    Text(
                        text = list.name,
                        fontSize = 20.sp,
                        color = Color.Black
                    )

                    Text(
                        text = list.month +" "+ list.year,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = modifier.weight(3f))

                Column(modifier = Modifier.weight(3f)) {

                    Row {

                        IconButton(onClick = { isExpanded.value = !isExpanded.value}) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(id = R.string.edit_current_list)
                            )
                        }

                        IconButton(onClick = { onDelete(list.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_current_list)
                            )
                        }
                    }
                }
            }
            Row{
                if(isExpanded.value){
                    EditScreen(list = list, isExpanded = isExpanded, onSubmit = onEdit)
                }
            }
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
fun EditScreen(
    list: PurchaseList,
    isExpanded: MutableState<Boolean>,
    onSubmit: (PurchaseList) -> Unit,
    modifier: Modifier = Modifier
){
    var name by remember { mutableStateOf(TextFieldValue(list.name)) }
    var errorMessage by remember { mutableStateOf(false) }

    Column {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp)
        ){

            OutlinedTextField(
                value = name,
                onValueChange = {name = it},
                placeholder = {Text(text = stringResource(id = R.string.new_purchase_list_name_hint))},
                label = {
                    Text(
                        text = stringResource(id = R.string.new_purchase_list_name_title),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                modifier = Modifier
                    .padding(4.dp)
                    .weight(5f),
            )

            Spacer(modifier = Modifier.weight(2f))

            IconButton(
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
                            day = list.day
                        )
                        onSubmit(newList)
                        isExpanded.value = false
                    }
                },
                modifier = Modifier.weight(1.5f),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.button_confirm)
                )
            }

            IconButton(onClick = { isExpanded.value = false }, modifier = Modifier.weight(1.5f)) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = stringResource(id = R.string.button_cancel)
                )
            }

        }

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
                                    name = fieldValue.text,
                                    listSize = 0,
                                    year = date.year,
                                    month = systemMonth,
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

