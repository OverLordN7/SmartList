package com.example.smartlist.ui.screens


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.extend_functions.bitmapToUri
import com.example.smartlist.extend_functions.capitalizeFirstChar
import com.example.smartlist.extend_functions.saveImageToInternalStorage
import com.example.smartlist.model.Item
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.ui.common_composables.ErrorScreen
import com.example.smartlist.ui.common_composables.LoadingScreen
import com.example.smartlist.ui.menu.CustomBottomAppBar
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import com.example.smartlist.ui.swipe.SwipeAction
import com.example.smartlist.ui.swipe.SwipeableActionsBox
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.lang.Math.round
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

private const val TAG = "DetailedPurchaseListScreen"
@Composable
fun DetailedPurchaseListScreen(
    purchaseViewModel: PurchaseViewModel,
    homeViewModel: HomeViewModel,
    navController: NavController,
    onSubmit: (Item) -> Unit,
    onRefresh: (Long)->Unit,
    onDelete: (UUID) -> Unit,
    onEdit: (Item) -> Unit,
    onRestore: (List<Item>) -> Unit,
    onItemBoughtChanged: (Item,Boolean)-> Unit,
    modifier: Modifier = Modifier
){
    val showDialog = remember { mutableStateOf(false) }
    val state: PurchaseItemUiState = purchaseViewModel.purchaseItemUiState
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    //Voice attributes
    val voiceState by homeViewModel.voiceToTextParser.state.collectAsState()
    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    //Navigation attributes
    val unknownVoiceCommandMessage = stringResource(id = R.string.unknown_command)

    if (showDialog.value){
        NewPurchaseListItemDialog(
            listId = purchaseViewModel.currentListId,
            setShowDialog = {showDialog.value = it},
            onConfirm = {item->
                //Submit newly created item to DB using callback of ViewModel
                onSubmit(item)
            }
        )
    }

    //When switching to different screen clean the content of command
    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    voiceCommand?.let { command->

        val parts = command.text.split(" ")
        if (parts.size>=3 && parts[0] == "создай" && parts[1] == "новый" && parts[2] == "предмет"){

            try {

                val newItemName:String  = parts[3]
                val newItemWeight: Float = parts[5].replace(',', '.').toFloat()
                val newItemType: String = parts[7]
                val newItemPrice: Float = parts.subList(9,parts.size).joinToString("").replace(',', '.').toFloat()

                val newItem = Item(
                    name = newItemName.capitalizeFirstChar(),
                    weight = newItemWeight,
                    weightType = newItemType,
                    price = newItemPrice,
                    total = newItemPrice * newItemWeight,
                    listId = purchaseViewModel.currentListId,
                )

                onSubmit(newItem)

            }catch (e : Exception){
                Toast.makeText(context,unknownVoiceCommandMessage, Toast.LENGTH_SHORT).show()
                Log.d(TAG,"error is $e")
            }

            homeViewModel.clearVoiceCommand()
        }
        else{
            homeViewModel.processNavigationCommand(
                command = command,
                currentScreen = context.getString(R.string.detailed_purchase_screen),
                navController = navController,
                context = context,
            )
        }
    }



    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                name = purchaseViewModel.currentName,
                state = voiceState,
                onNavigationIconClick = { scope.launch { scaffoldState.drawerState.open() } },
                retryAction = {onRefresh(800)},
                onMicrophoneOn = {
                    if(it){ homeViewModel.startListening() }
                    else{ homeViewModel.stopListening() }
                },
                isRetryActionEnabled = true,
                isShareActionEnabled = true,
                onShareAction = {
                    val messageHeader = context.getString(R.string.messageHeader)
                    val currency = context.getString(R.string.currency_title)
                    purchaseViewModel.sharePurchaseList(messageHeader,currency,context)
                }
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
            FloatingActionButton(
                onClick = { showDialog.value = true },
                modifier = Modifier
            ){
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add_purchase_item),
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
                        currentScreen = "detailedPurchaseScreen",
                        context = context,
                        navController = navController,
                    )
                }
            )
        }
    ) {
        Surface(modifier = modifier.padding(it)) {

            when(state){
                is PurchaseItemUiState.Loading -> LoadingScreen()
                is PurchaseItemUiState.Error -> ErrorScreen(errorMessage = state.errorMessage)
                is PurchaseItemUiState.Success ->{
                    if ( state.items.isEmpty()){
                        EmptyCard(
                            listId = purchaseViewModel.currentListId,
                            onSubmit = onSubmit,
                            )
                    } else{
                        ActiveListCard(
                            itemsOfList =  state.items,
                            listId = purchaseViewModel.currentListId,
                            onSubmit = onSubmit,
                            onItemBoughtChanged = onItemBoughtChanged,
                            onDelete = {itemId-> onDelete(itemId)},
                            onEdit = onEdit,
                            onRestore = onRestore,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCard(
    listId: UUID,
    onSubmit: (Item) -> Unit,
    modifier: Modifier = Modifier
){
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value){
        NewPurchaseListItemDialog(
            listId =  listId,
            setShowDialog = {showDialog.value = it},
            onConfirm = {item->
                //Submit newly created item to DB using callback of ViewModel
                onSubmit(item)
            }
        )
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.empty_card_message_1), color = Color.Black)
            //Text(text = stringResource(id = R.string.empty_card_message_2), color = Color.Black)

            val stroke = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f,10f),0f)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(start = 8.dp, end = 8.dp, bottom = 4.dp, top = 16.dp)
                    .drawBehind { drawRect(color = Color.DarkGray, style = stroke) }
                    .clickable { showDialog.value = true }
            ) {
                Text(
                    text = stringResource(id = R.string.add_purchase_item),
                    textAlign = TextAlign.Center
                )
            }

        }
    }
}

@Composable
fun ActiveListCard(
    itemsOfList: List<Item>,
    listId: UUID,
    onSubmit: (Item) -> Unit,
    onItemBoughtChanged: (Item,Boolean)-> Unit,
    onEdit: (Item) -> Unit,
    onDelete: (UUID) -> Unit,
    onRestore: (List<Item>) -> Unit,
    modifier: Modifier = Modifier
){

    var isActiveExpanded by remember { mutableStateOf(true) }
    var isNotActiveExpanded by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value){
        NewPurchaseListItemDialog(
            listId =  listId,
            setShowDialog = {showDialog.value = it},
            onConfirm = {item->
                //Submit newly created item to DB using callback of ViewModel
                onSubmit(item)
            }
        )
    }
    val notBoughtList: MutableList<Item> = emptyList<Item>().toMutableList()
    val boughtList: MutableList<Item> = emptyList<Item>().toMutableList()

    itemsOfList.forEach { item ->
        if (item.isBought){
            boughtList += item
        } else{
            notBoughtList += item
        }
    }

    Card(
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        LazyColumn{
            item{
                ListInfoCard(itemsOfList)
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = stringResource(R.string.active), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { isActiveExpanded = !isActiveExpanded}) {
                        Icon(
                            imageVector = if(isActiveExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
            }

            if (isActiveExpanded){

                item {
                    val stroke = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f,10f),0f)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(start = 8.dp, end = 8.dp, bottom = 4.dp, top = 16.dp)
                            .drawBehind { drawRect(color = Color.DarkGray, style = stroke) }
                            .clickable { showDialog.value = true }
                    ) {
                        Text(
                            text = stringResource(id = R.string.add_purchase_item),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                items(notBoughtList.size){
                    SwipeWrapperItemCard(
                        item = notBoughtList[it],
                        onClick = onItemBoughtChanged,
                        onDelete = {id-> onDelete(id) },
                        onEdit = onEdit
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = stringResource(R.string.offline), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {onRestore(itemsOfList)}) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                    }
                    IconButton(onClick = { isNotActiveExpanded = !isNotActiveExpanded}) {
                        Icon(
                            imageVector = if(isNotActiveExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
            }

            if (isNotActiveExpanded){
                items(boughtList.size){
                    SwipeWrapperItemCard(
                        item = boughtList[it],
                        onClick = onItemBoughtChanged,
                        onDelete = {id-> onDelete(id) },
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeWrapperItemCard(
    item: Item,
    onClick: (Item, Boolean) -> Unit,
    onEdit: (Item) -> Unit,
    onDelete: (UUID) -> Unit,
    modifier: Modifier = Modifier
){
    val deleteAction = SwipeAction(
        icon = {rememberVectorPainter(image = Icons.TwoTone.Delete)},
        background = Color.Red,
        onSwipe = { onDelete(item.id) },
    )

    SwipeableActionsBox(
        modifier = modifier,
        startActions = emptyList(),
        endActions = listOf(deleteAction),
        swipeThreshold = 40.dp,
        backgroundUntilSwipeThreshold = MaterialTheme.colors.surface
    ) {
        ItemCard(
            item = item,
            onClick = onClick,
            onDelete = onDelete,
            onEdit = onEdit
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    item: Item,
    modifier: Modifier = Modifier,
    onClick: (Item,Boolean) -> Unit,
    onEdit: (Item) -> Unit = {},
    onDelete: (UUID) -> Unit = {},
){
    val context = LocalContext.current
    val currency = stringResource(id = R.string.currency_title)
    val isExpanded = remember { mutableStateOf(false) }

    var isBought: Boolean = item.isBought

    val drawablesList = listOf(
        R.drawable.veg,
        R.drawable.veg2,
        R.drawable.veg3,
    )

    //Attributes for photo
    val bitmap = remember{ mutableStateOf<Bitmap?>(null)}

    val bitmapCorrupted = remember { mutableStateOf(false) }

    val showImageDialog = remember { mutableStateOf(false) }

    if (showImageDialog.value){
        ShowItemImage(
            item = item,
            setShowDialog = {showImageDialog.value = it},
        )
    }


    Card(
        elevation = 4.dp,
        backgroundColor = if(isBought) Color.LightGray else Color.White,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    isBought = !isBought
                    onClick(item, isBought)
                },
                onLongClick = { showImageDialog.value = true }
            )

    ) {
        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(4.dp)
            ) {

                if (!item.photoPath.isNullOrEmpty()){
                    try {
                        val source = ImageDecoder.createSource(context.contentResolver,
                            item.photoPath!!.toUri())
                        bitmap.value = ImageDecoder.decodeBitmap(source)
                        bitmapCorrupted.value = false

                    }
                    catch (e: Exception){
                        e.printStackTrace()
                        //Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show()
                        bitmapCorrupted.value = true
                    }

                    Image(
                        bitmap = if(bitmapCorrupted.value) getBitmapFromImage(R.drawable.circle) else bitmap.value!!.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.purchase_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            //.weight(2f)
                            .size(48.dp)
                            //.padding(start = 4.dp, end = 4.dp)
                            .border(2.dp, Color.Gray, CircleShape)
                            .clip(CircleShape)
                    )

                } else {
                    Image(
                        painter = painterResource(id = drawablesList[item.drawableId]),
                        contentDescription = stringResource(id = R.string.purchase_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            //.weight(2f)
                            .size(48.dp)
                            //.padding(start = 4.dp, end = 4.dp)
                            .border(2.dp, Color.Gray, CircleShape)
                            .clip(CircleShape)
                    )
                }

                Column(modifier = Modifier
                    .weight(8f)
                    .padding(top = 4.dp, start = 4.dp)) {

                    Text(
                        text = item.name,
                        fontSize = 20.sp,
                        color = Color.Black,
                    )

                    Row(horizontalArrangement = Arrangement.SpaceAround){
                        Text(
                            text = context.getString(R.string.purchase_weight,item.weight,item.weightType),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = modifier
                                .weight(3f)
                                .padding(4.dp)
                        )

                        Text(
                            //text = context.getString(R.string.purchase_total,item.total.toInt(),currency),
                            text = formatTotal(item.total,currency),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = modifier
                                .weight(3f)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = modifier.weight(1f))

                Column(modifier = Modifier.weight(4f)) {

                    Row {
                        IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(id = R.string.edit_current_list)
                            )
                        }

                        IconButton(onClick = { onDelete(item.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_current_list)
                            )
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(4.dp)
            ) {
                if (isExpanded.value){
                    EditScreen(item = item, isExpanded = isExpanded, onSubmit = onEdit)
                }
            }
        }
    }
}
@Composable
fun ListInfoCard(items: List<Item>, modifier: Modifier = Modifier){

    val context = LocalContext.current
    val currency = stringResource(id = R.string.currency_title)

    var total = 0

    items.forEach {
        total += it.total.toInt()
    }

    var left: Int = total

    items.forEach {
        if (it.isBought){
            left-=it.total.toInt()
        }
    }
    val convertedTotal: String = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(total)
    val convertedLeft = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(left)


    Card(
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.primary,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {

            Row(modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)) {
                Text(
                    text = context.getString(R.string.total,convertedTotal,currency),
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }

            Row(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.getString(R.string.left,convertedLeft,currency),
                    fontSize = 16.sp,
                    color = Color.White,
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditScreen(
    item: Item,
    isExpanded: MutableState<Boolean>,
    onSubmit: (Item)-> Unit,
    modifier: Modifier = Modifier
){
    var fieldValue by remember{ mutableStateOf(TextFieldValue(item.name)) }
    var weight by remember { mutableStateOf(TextFieldValue(item.weight.toString())) }
    var price by remember { mutableStateOf(TextFieldValue(item.price.toString())) }
    var errorMessage by remember { mutableStateOf(false) }

    //values for DropDownMenu
    val options = listOf(
        stringResource(id = R.string.kgs),
        stringResource(id = R.string.lbs),
        stringResource(id = R.string.pcs),
        stringResource(id = R.string.pkg)
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }


    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = fieldValue,
                onValueChange = {fieldValue = it},
                placeholder = {Text(text = stringResource(id = R.string.new_purchase_list_name_hint))},
                label = {
                    Text(
                        text = stringResource(id = R.string.new_purchase_list_name_title),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .padding(4.dp)
                    .weight(0.6f),
            )

            OutlinedTextField(
                value = weight,
                onValueChange = {weight = it},
                placeholder = {Text(text = stringResource(id = R.string.weight_hint))},
                label = {
                    Text(
                        text = stringResource(id = R.string.weight_title),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .padding(4.dp)
                    .weight(0.5f),
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded},
                modifier = Modifier.weight(0.5f)
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedOptionText,
                    onValueChange = { },
                    label = { Text(stringResource(id = R.string.unit), color = Color.Black)},
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach{ selectionOption ->
                        DropdownMenuItem(
                            onClick = {
                                selectedOptionText = selectionOption
                                expanded = false
                            }
                        ) {
                            Text(text = selectionOption)
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.Center) {
            OutlinedTextField(
                value = price,
                onValueChange = {price = it},
                placeholder = {Text(text = stringResource(id = R.string.price_hint))},
                label = {
                    Text(
                        text = stringResource(id = R.string.price_title),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f),
            )

            //Plug for good view
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.End) {

            //Plug
            Spacer(modifier = Modifier.weight(5f))

            IconButton(
                onClick = {
                    if(fieldValue.text.isBlank() || weight.text.isBlank() || price.text.isBlank()){
                        errorMessage = true
                    }else{
                        val temp = Item(
                            id = item.id,
                            name = fieldValue.text,
                            weight = weight.text.toFloat(),
                            weightType = selectedOptionText,
                            price = price.text.toFloat(),
                            total = weight.text.toFloat() * price.text.toFloat(),
                            listId = item.listId
                        )
                        isExpanded.value = false
                        onSubmit(temp)
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.button_confirm)
                )
            }
            IconButton(onClick = { isExpanded.value = false }, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = stringResource(id = R.string.button_cancel)
                )
            }
        }
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun NewPurchaseListItemDialog(
    listId: UUID,
    setShowDialog: (Boolean) -> Unit,
    onConfirm: (Item) -> Unit,
    modifier: Modifier = Modifier,
){
    var errorFieldStatus by remember { mutableStateOf(false) }
    var fieldValue by remember{ mutableStateOf(TextFieldValue("")) }
    var weight by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var totalPrice : Float

    //values for DropDownMenu
    val options = listOf(
        stringResource(id = R.string.kgs),
        stringResource(id = R.string.lbs),
        stringResource(id = R.string.pcs),
        stringResource(id = R.string.pkg)
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    //Attributes for photo capture
    var imageUri by remember { mutableStateOf<Uri?>(null)}
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()){
            result->
        if (result.resultCode == Activity.RESULT_OK){
            imageUri = result.data?.data

            Log.d(TAG,"the uri in launcher: $imageUri")

            if (imageUri != null){
                imageUri = saveImageToInternalStorage(context, imageUri!!)
            }
        }
    }

    //Adding camera support
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer  = LifecycleEventObserver { _, event ->
                if(event == Lifecycle.Event.ON_RESUME){
                    cameraPermissionState.launchPermissionRequest()
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()){
        bitmap ->
        if (bitmap != null){
            imageUri = bitmapToUri(context, bitmap)
        }

    }


    Dialog(onDismissRequest = {setShowDialog(false)}) {

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {

            LazyColumn(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(8.dp)
            ) {

                //Header of dialog
                item{
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.new_purchase), color = Color.Black, fontSize = 28.sp)
                    }
                }

                //Primary fields
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.name_title),
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fieldValue,
                            onValueChange = {fieldValue = it},
                            placeholder = {Text(text = stringResource(id = R.string.new_purchase_hint))},
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrect = true,
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(2f),
                        )
                    }
                }

                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.weight_title),
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weight,
                            onValueChange = {weight = it},
                            placeholder = {Text(text = stringResource(id = R.string.weight_hint))},
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(2f),
                        )
                    }
                }

                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.unit),
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded},
                            modifier = Modifier.weight(2f)
                        ) {
                            OutlinedTextField(
                                readOnly = true,
                                value = selectedOptionText,
                                onValueChange = { },
                                label = { Text(stringResource(id = R.string.unit), color = Color.Black)},
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                            )

                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                options.forEach{ selectionOption ->
                                    DropdownMenuItem(
                                        onClick = {
                                            selectedOptionText = selectionOption
                                            expanded = false
                                        }
                                    ) {
                                        Text(text = selectionOption)
                                    }
                                }
                            }
                        }
                    }
                }

                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.price_title),
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = {price = it},
                            placeholder = {Text(text = stringResource(id = R.string.price_hint))},
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.weight(2f),
                        )
                    }
                }

                //Photo capture
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.padding(end = 4.dp),
                    ){

                        Text(
                            text = stringResource(id = R.string.photo_button),
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )

                        Row(modifier = Modifier.weight(2f)){
                            IconButton(
                                onClick = {
                                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                    launcher.launch(galleryIntent)
                                },
                                modifier = Modifier
                                    .size(60.dp)
                                    .weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Photo, contentDescription = null)
                            }

                            IconButton(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .size(60.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                            }
                        }
                    }
                }

                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        if (errorFieldStatus){
                            Text(
                                text = stringResource(id = R.string.error_message),
                                color = Color.Red,
                                modifier = Modifier.padding(start = 12.dp)
                            )

                        } else{
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }

                item{
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
                                if (fieldValue.text.isBlank() || weight.text.isBlank() || price.text.isBlank()){
                                    errorFieldStatus = true
                                }
                                else{
                                    //Check is OK, continue..

                                    Log.d(TAG,"the uri before creating new purchase: $imageUri")

                                    totalPrice = weight.text.toFloat() * price.text.toFloat()
                                    val tempItem = Item(
                                        name = fieldValue.text.capitalizeFirstChar(),
                                        weight = weight.text.toFloat(),
                                        weightType = selectedOptionText,
                                        price = price.text.toFloat(),
                                        total = totalPrice,
                                        listId = listId,
                                        photoPath = if(imageUri.toString() == "null") null else imageUri.toString()
                                    )
                                    onConfirm(tempItem)
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
}

@Composable
fun ShowItemImage(
    item: Item,
    setShowDialog: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    ){

    val context = LocalContext.current
    val bitmapFromDrawable = getBitmapFromImage(drawable = R.drawable.veg)

    var height = context.resources.configuration.screenHeightDp
    var width = context.resources.configuration.screenWidthDp

    //make a square
    if (height > width){
        height = width
    } else{
       width = height
    }

    //Attributes for photo
    val bitmap = remember{ mutableStateOf<Bitmap?>(null)}

    val bitmapCorrupted = remember { mutableStateOf(false) }

    try {
        val source = ImageDecoder.createSource(context.contentResolver,
            item.photoPath!!.toUri())
        bitmap.value = ImageDecoder.decodeBitmap(source)
        bitmapCorrupted.value = false

    }
    catch (e: Exception){
        e.printStackTrace()
        //Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show()
        bitmapCorrupted.value = true
    }



    Dialog(
        onDismissRequest = { setShowDialog(false) },
    ) {
        Surface(modifier = modifier.size(width.dp,height.dp)) {
            Card(modifier = Modifier.clickable {
                setShowDialog(false)
            }){
                Image(
                    bitmap = if (bitmapCorrupted.value) bitmapFromDrawable else bitmap.value!!.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    )
            }
        }
    }
}

fun formatTotal(total: Float, currency: String): String {
    return when {
        total >= 1000000 -> {
            val formattedTotal = (total / 1000000)
            var temp = String.format("%.2f", formattedTotal)
            temp += "M $currency"
            temp
        }
        total >= 100000 -> {
            val formattedTotal = round((total / 1000)).toInt()
            "${formattedTotal}K $currency"
        }
        else -> {
            String.format("%.0f %s", total, currency)
        }
    }
}
