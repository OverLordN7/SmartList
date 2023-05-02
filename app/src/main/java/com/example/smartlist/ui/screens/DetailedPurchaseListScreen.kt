package com.example.smartlist.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smartlist.R
import com.example.smartlist.model.Item
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

private const val TAG = "DetailedPurchaseListScreen"
@Composable
fun DetailedPurchaseListScreen(
    listId: String,
    purchaseViewModel: PurchaseViewModel,
    onSubmit: (Item) -> Unit,
    onRefresh: ()->Unit,
    onDelete: (UUID) -> Unit,
    onEdit: (Item) -> Unit,
    onItemBoughtChanged: (Item,Boolean)-> Unit,
    modifier: Modifier = Modifier
){
    val showDialog = remember { mutableStateOf(false) }
    val state: PurchaseItemUiState = purchaseViewModel.purchaseItemUiState

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

    Scaffold(
        topBar = {
            AppBarItem(
                purchaseViewModel.currentName,
                retryAction = { onRefresh() }
            )
                 },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true}) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add new Item")
            }
        }
    ) {
        Surface(modifier = modifier.padding(it)) {
            when(state){
                is PurchaseItemUiState.Loading ->{}
                is PurchaseItemUiState.Error ->{}
                is PurchaseItemUiState.Success ->{
                    ResultItemScreen(
                        itemsOfList = state.items,
                        onItemBoughtChanged = onItemBoughtChanged,
                        onDelete = {itemId-> onDelete(itemId)},
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}


@Composable
fun ResultItemScreen(
    itemsOfList: List<Item>,
    onItemBoughtChanged: (Item,Boolean)-> Unit,
    onEdit: (Item) -> Unit,
    onDelete: (UUID) -> Unit
){

    //If no Item received but call ended with Success
    if (itemsOfList.isEmpty()) {
        EmptyCard()
        return Unit
    }
    LazyColumn{
        item{
            ListInfoCard(itemsOfList)
        }
        items(itemsOfList.size){
            ItemCard(
                item = itemsOfList[it],
                onClick = onItemBoughtChanged,
                onDelete = {id-> onDelete(id) },
                onEdit = onEdit
            )
        }

    }
}

@Composable
fun EmptyCard(modifier: Modifier = Modifier){
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        Column {
            Text(text = "No items to display", color = Color.Black)
            Text(text = "Try to use + button", color = Color.Black)
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    modifier: Modifier = Modifier,
    onClick: (Item,Boolean) -> Unit,
    onEdit: (Item) -> Unit = {},
    onDelete: (UUID) -> Unit = {},
){
    val context = LocalContext.current
    val isExpanded = remember { mutableStateOf(false) }

    var isBought by remember { mutableStateOf(item.isBought) }



    Card(
        elevation = 4.dp,
        backgroundColor = if(isBought) Color.LightGray else Color.White,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                isBought = !isBought
                onClick(item, isBought)
            }

    ) {
        Column() {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(4.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.circle),
                    contentDescription = "product picture",
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 4.dp, end = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(8f)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = item.name,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Row(horizontalArrangement = Arrangement.SpaceAround){
                        Text(
                            text = "${item.weight} ${item.weightType}",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = modifier
                                .weight(2f)
                                .padding(4.dp)
                        )

                        Text(
                            text = "${item.price.toInt()} UZS",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = modifier
                                .weight(3f)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = modifier.weight(2f))

                Column(modifier = Modifier
                    .weight(3f)
                    .padding(end = 4.dp)) {
                    Row {
                        IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit current list")
                        }
                        IconButton(onClick = {
                            Toast.makeText(context,"Deleting item...", Toast.LENGTH_SHORT).show()
                            onDelete(item.id)
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete current list")
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
                    EditScreen(
                        item = item,
                        isExpanded = isExpanded,
                        onSubmit = onEdit
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
    var totalPrice : Float = 0.0f

    //values for DropDownMenu
    val options = listOf("kgs","lbs","pcs","pkg")
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    //keyboard focus
    val focusManager = LocalFocusManager.current


    Dialog(onDismissRequest = {setShowDialog(false)}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .padding(8.dp)
            ) {
                //Header of dialog
                Text(text = "New Item", color = Color.Black, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = {fieldValue = it},
                    placeholder = {Text(text = "ex Potato")},
                    modifier = Modifier.padding(top = 4.dp),
                    singleLine = true,
                    label = {
                        Text(
                            text = "Enter new Item name: ",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = {weight = it},
                        placeholder = {Text(text = "ex 10.0")},
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .weight(0.8f),
                        label = {
                            Text(
                                text = "Weight: ",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                    )

                    Spacer(modifier = Modifier.weight(0.2f))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded},
                        modifier = Modifier.weight(0.8f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedOptionText,
                            onValueChange = { },
                            label = { Text("Unit", color = Color.Black)},
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            //colors =  ExposedDropdownMenuDefaults.textFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
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
                OutlinedTextField(
                    value = price,
                    onValueChange = {price = it},
                    placeholder = {Text(text = "ex 10000")},
                    modifier = Modifier.padding(top = 4.dp),
                    label = {
                        Text(
                            text = "Price: ",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )

                if (errorFieldStatus){
                    Text(
                        text = "*Sure that you fill all fields, if message still remains, check symbols",
                        color = Color.Red,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                } else{
                    Spacer(modifier = Modifier.height(20.dp))
                }


                Spacer(modifier = Modifier.height(20.dp))

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
                        Text(text = "Cancel")
                    }

                    //Spacer(modifier = Modifier.weight(1f))

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        onClick = {

                            //Check if all fields are not null
                            if (fieldValue.text.isBlank() || weight.text.isBlank() || price.text.isBlank()){
                                errorFieldStatus = true
                            }
                            else{
                                //Check is OK, continue..
                                totalPrice = weight.text.toFloat() * price.text.toFloat()
                                val tempItem = Item(
                                    name = fieldValue.text,
                                    weight = weight.text.toFloat(),
                                    weightType = selectedOptionText,
                                    price = price.text.toFloat(),
                                    total = totalPrice,
                                    listId = listId
                                )
                                onConfirm(tempItem)
                                setShowDialog(false)
                            }
                        }
                    ) { Text(text = "Confirm") }
                }
            }
        }
    }
}


@Composable
fun ListInfoCard(items: List<Item>, modifier: Modifier = Modifier){
    var total = 0
    var left = 0
    items.forEach {
        total += it.total.toInt()
    }

    left = total

    items.forEach {
        if (it.isBought){
            left-=it.total.toInt()
        }
    }

    val convertedTotal = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(total)
    val convertedLeft = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)).format(left)

    Card(
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                "Total: $convertedTotal UZS",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f).padding(4.dp)
            )
            Text(
                "Left: $convertedLeft UZS",
                fontSize = 18.sp,
                color = Color.Gray,
                modifier = Modifier.weight(1f).padding(4.dp)
            )
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
    var totalPrice : Float = item.weight * item.price
    var errorMessage by remember { mutableStateOf(false) }


    //values for DropDownMenu
    val options = listOf("kgs","lbs","pcs","pkg")
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
                placeholder = {Text(text = "ex Potato")},
                modifier = Modifier
                    .padding(4.dp)
                    .weight(0.6f),
                label = {
                    Text(
                        text = "Enter new Item name: ",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )

            OutlinedTextField(
                value = weight,
                onValueChange = {weight = it},
                placeholder = {Text(text = "ex 10.0")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .padding(4.dp)
                    .weight(0.5f),
                label = {
                    Text(
                        text = "Weight: ",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
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
                    label = { Text("Unit", color = Color.Black)},
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    //colors =  ExposedDropdownMenuDefaults.textFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
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
                placeholder = {Text(text = "ex 10000")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f),
                label = {
                    Text(
                        text = "Price: ",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )

            //Plug for good view
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.End) {

            //Plug
            Spacer(modifier = Modifier.weight(5f))

            IconButton(
                modifier = Modifier.weight(1f),
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
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "check")
            }
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = { isExpanded.value = false }
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription = "cancel")
            }
        }
        if (errorMessage){
            Text(
                text = "*Sure that you fill all fields, if message still remains, check symbols",
                color = Color.Red,
                modifier = Modifier.padding(start = 12.dp)
            )
        } else{
            Spacer(modifier = Modifier.height(20.dp))
        }
    }


}

@Composable
fun AppBarItem(name: String, retryAction: () -> Unit) {
    val title = stringResource(id = R.string.app_name)
    TopAppBar(
        title = { Text(text = "$title > $name") },
        actions = {
            IconButton(onClick = retryAction) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    )
}