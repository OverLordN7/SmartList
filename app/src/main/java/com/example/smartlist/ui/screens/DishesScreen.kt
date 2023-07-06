package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.DishList
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.model.items
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@Composable
fun DishesScreen(
    navController: NavController,
    dishViewModel: DishViewModel,
    homeViewModel: HomeViewModel,
    onSubmit: (DishList) -> Unit,
    onEdit: (DishList) -> Unit,
    onDelete: (UUID) -> Unit,
    onRefresh: ()->Unit,
    modifier: Modifier = Modifier,
){
    val showDialog = remember { mutableStateOf(false) }
    val state: DishUiState = dishViewModel.dishUiState

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    //Navigation attributes
    val navigationMessage = stringResource(id = R.string.navigation_message)
    val navigationTransition = stringResource(id = R.string.navigation_transition)
    val unknownVoiceCommandMessage = stringResource(id = R.string.unknown_command)

    //Voice attributes
    val voiceState by homeViewModel.voiceToTextParser.state.collectAsState()
    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    //When switch to different screen, refresh command
    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    //Process voice command
    voiceCommand?.let { command->
        val parts = command.text.split(" ")

        if (parts.size>=3 && parts[0] == "создай" && parts[1] == "новый" && parts[2] == "список"){
            val newDishListName:String  = parts.subList(3,parts.size).joinToString("")
            val currentDate = LocalDate.now()
            val newDishList = DishList(
                name = newDishListName,
                listSize = 0,
                year = currentDate.year,
                month = currentDate.month.name,
                day = currentDate.dayOfMonth
            )
            onSubmit(newDishList)
            homeViewModel.clearVoiceCommand()
        }

        else{
            when(command.text){
                "список блюд"->{ Toast.makeText(context,navigationTransition, Toast.LENGTH_SHORT).show()}
                "список покупок"->{ navController.navigate(Screen.PurchasesScreen.route)}
                "графики"->{navController.navigate(Screen.GraphScreen.route)}
                "домашняя страница"->{navController.navigate(Screen.HomeScreen.route)}
                else->{Toast.makeText(context,unknownVoiceCommandMessage, Toast.LENGTH_SHORT).show()}
            }
        }
    }

    if (showDialog.value){
        NewDishListDialog(setShowDialog = {showDialog.value = it}, onConfirm = onSubmit)
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
                            Toast.makeText(context,navigationMessage, Toast.LENGTH_SHORT).show()
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
                    contentDescription = stringResource(id = R.string.add_new_dish)
                )
            }
        },
    ) {
        Surface(modifier = modifier
            .fillMaxSize()
            .padding(it)) {

            when(state){
                is DishUiState.Loading ->{}
                is DishUiState.Error ->{}
                is DishUiState.Success ->{
                    ResultScreen(
                        lists = state.dishList,
                        onClick = { listId ->
                            dishViewModel.currentListId = listId
                            dishViewModel.getRecipesList()
                            dishViewModel.getListName(listId)
                            dishViewModel.getListSize(listId)
                            navController.navigate(Screen.DetailedDishesScreen.route)
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
    lists: List<DishList>,
    onClick: (UUID) -> Unit,
    onEdit: (DishList) -> Unit,
    onDelete: (UUID) -> Unit,
){
    //If no Item received but call ended with Success
    if (lists.isEmpty()) {
        EmptyListCard()
        return
    }

    LazyColumn{
        items(lists.size){index ->
            DishListCard(
                list = lists[index],
                onClick = { onClick(lists[index].id)},
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
fun DishListCard(
    list: DishList,
    onClick: (UUID) -> Unit,
    onEdit: (DishList) -> Unit,
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

                Column(
                    modifier = Modifier.weight(5f)
                ) {

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
                    DishEditScreen(list = list, isExpanded = isExpanded, onSubmit = onEdit)
                }
            }
        }

    }
}

@Composable
fun DishEditScreen(
    list: DishList,
    isExpanded: MutableState<Boolean>,
    onSubmit: (DishList) -> Unit,
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
                placeholder = {Text(text = stringResource(id = R.string.new_dish_list_name_hint))},
                label = {
                    Text(
                        text = stringResource(id = R.string.new_dish_list_name_title),
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
                    if (name.text.isBlank()){ errorMessage = true }
                    else{
                        val newList = DishList(
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
fun NewDishListDialog(
    setShowDialog: (Boolean) -> Unit,
    onConfirm: (DishList) -> Unit,
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
                    placeholder = {Text(text = stringResource(id = R.string.new_dish_list_name_hint))},
                    label = {
                        Text(
                            text = stringResource(id = R.string.new_dish_list_name_title),
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
                } else{
                    Spacer(modifier = Modifier.height(40.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ){

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        onClick = { setShowDialog(false)}
                    ) {
                        Text(text = stringResource(id = R.string.button_cancel))
                    }

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        onClick = {
                            //Check if all fields are not null
                            if (fieldValue.text.isBlank()){
                                errorFieldStatus = true
                            }
                            else{
                                val date = LocalDate.now()
                                val list = DishList(
                                    name = fieldValue.text,
                                    listSize = 0,
                                    year = date.year,
                                    month = date.month.name,
                                    day = date.dayOfMonth
                                )
                                onConfirm(list)
                                setShowDialog(false)
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.button_confirm))
                    }
                }
            }
        }
    }
}