package com.example.smartlist.ui.screens

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.extend_functions.capitalizeFirstChar
import com.example.smartlist.model.DishList
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

    //Voice attributes
    val voiceState by homeViewModel.voiceToTextParser.state.collectAsState()
    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    val themeMode = homeViewModel.isDarkThemeEnabled.collectAsState()

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
            val formatter = DateTimeFormatter.ofPattern("LLLL", Locale.getDefault())
            val systemMonth = currentDate.format(formatter)
            val newDishList = DishList(
                name = newDishListName.capitalizeFirstChar(),
                listSize = 0,
                year = currentDate.year,
                month = systemMonth.capitalizeFirstChar(),
                day = currentDate.dayOfMonth
            )
            onSubmit(newDishList)
            homeViewModel.clearVoiceCommand()
        }

        else{
            homeViewModel.processNavigationCommand(
                command = command,
                currentScreen = context.getString(R.string.dish_screen),
                navController = navController,
                context = context,
            )
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
                    contentDescription = stringResource(id = R.string.add_new_dish)
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
                        currentScreen = "dishList",
                        context = context,
                        navController = navController,
                    )
                }
            )
        },
    ) {
        Surface(modifier = modifier
            .fillMaxSize()
            .padding(it)) {

            when(state){
                is DishUiState.Loading -> LoadingScreen()
                is DishUiState.Error -> ErrorScreen(errorMessage = state.errorMessage)
                is DishUiState.Success ->{
                    ResultScreen(
                        lists = state.dishList,
                        themeMode = themeMode.value,
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
    themeMode: Boolean,
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
                themeMode = themeMode,
                onClick = { onClick(lists[index].id)},
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
fun DishListCard1(
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
fun DishListCard(
    list: DishList,
    themeMode: Boolean,
    onClick: (UUID) -> Unit,
    onEdit: (DishList) -> Unit,
    onDelete: (UUID) -> Unit,
    modifier: Modifier = Modifier
){

    val isChangeMode = remember { mutableStateOf(false) }
    val context = LocalContext.current

    var spacerWeight by remember { mutableFloatStateOf(3f) }
    var itemWeight by remember { mutableFloatStateOf(2f) }

    if( list.listSize > 9 ){
        spacerWeight = 4f
        itemWeight = 2.5f
    }else{
        spacerWeight = 3f
        itemWeight = 2f
    }

    val imgAssets = listOf(
        R.drawable.food_1,
        R.drawable.food_2,
        R.drawable.food_3,
        R.drawable.food_4,
        R.drawable.food_5,
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
                            .fillMaxSize(),
                        backgroundColor = Color.Transparent,
                        elevation = 0.dp
                    ) {
                        Column() {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(MaterialTheme.colors.primary)
                                    .padding(start = 4.dp, end = 4.dp)
                            ){
                                Box( modifier = Modifier.weight(3f)) {
                                    Text(
                                        text = list.name,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.weight(2f))
                                IconButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { isChangeMode.value = !isChangeMode.value }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(id = R.string.edit_current_list),
                                        tint = MaterialTheme.colors.onPrimary
                                    )
                                }

                                IconButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = { onDelete(list.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.delete_current_list),
                                        tint = MaterialTheme.colors.onPrimary
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
                                Spacer(modifier = Modifier.weight(spacerWeight))
                                Box(
                                    modifier = Modifier
                                        .weight(itemWeight)
                                        .padding(
                                            start = 4.dp,
                                            top = 8.dp,
                                            end = 4.dp,
                                            bottom = 4.dp
                                        ),
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
            DishEditScreen1(list = list, isExpanded = isChangeMode, onSubmit = onEdit)
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

    Column(modifier = modifier.padding(4.dp)) {

        //Name field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = stringResource(id = R.string.dishlist_name_title),
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = name,
                onValueChange = {name = it},
                placeholder = {Text(text = stringResource(id = R.string.new_dish_list_name_hint))},
                modifier = Modifier.weight(2f)
            )
        }

        //Error field
        Row() {
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

            Button(
                modifier = Modifier.weight(1f),
                onClick = { isExpanded.value = false }
            ) {
                Text(text = stringResource(id = R.string.button_cancel))
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.weight(1f),
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
            ) {
                Text(text = stringResource(id = R.string.button_confirm))
            }
        }
    }
}

@Composable
fun DishEditScreen1(
    list: DishList,
    isExpanded: MutableState<Boolean>,
    onSubmit: (DishList) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(TextFieldValue(list.name)) }
    var errorMessage by remember { mutableStateOf(false) }

    Card {
        Column {
            Box(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.primary)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.purchaselist_name_title),
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.new_purchase_list_name_hint),
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 0.dp)
            )

            if (errorMessage) {
                Text(
                    text = stringResource(id = R.string.error_message),
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.height(32.dp).padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 0.dp)
                )
            } else{
                Spacer(modifier = Modifier.height(32.dp).padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 0.dp))
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 0.dp)
            ) {
                Button(
                    onClick = { isExpanded.value = false },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text(
                        text = stringResource(id = R.string.button_cancel),
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        if (name.text.isBlank()) {
                            errorMessage = true
                        } else {
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
                    modifier = Modifier
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                ) {
                    Text(
                        text = stringResource(id = R.string.button_confirm),
                        color = Color.White
                    )
                }
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
                                val formatter = DateTimeFormatter.ofPattern("LLLL", Locale.getDefault())
                                val systemMonth = date.format(formatter)

                                val list = DishList(
                                    name = fieldValue.text.capitalizeFirstChar(),
                                    listSize = 0,
                                    year = date.year,
                                    month = systemMonth.capitalizeFirstChar(),
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