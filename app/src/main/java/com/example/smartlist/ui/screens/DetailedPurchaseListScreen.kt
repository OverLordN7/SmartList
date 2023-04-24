package com.example.smartlist.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smartlist.R
import com.example.smartlist.model.Item
import java.util.UUID

private const val TAG = "DetailedPurchaseListScreen"
@Composable
fun DetailedPurchaseListScreen(
    listId: String,
    purchaseViewModel: PurchaseViewModel,
    onSubmit: (Item,UUID) -> Unit,
    onRefresh: (UUID)->Unit,
    onDelete: (UUID,UUID) -> Unit,
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
                onSubmit(item, UUID.fromString(listId))
            }
        )
    }

    Scaffold(
        topBar = {
            AppBarItem(
                purchaseViewModel.currentName,
                retryAction = {
                    val id = UUID.fromString(listId)
                    onRefresh(id)
                }
            )
                 },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true}) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add new Item")
            }
        }
    ) { it ->
        Surface(modifier = modifier.padding(it)) {
            when(state){
                is PurchaseItemUiState.Loading ->{}
                is PurchaseItemUiState.Error ->{}
                is PurchaseItemUiState.Success ->{
                    ResultItemScreen(
                        itemsOfList = state.items,
                        onDelete = {itemId->
                            onDelete(itemId,UUID.fromString(listId))
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ResultItemScreen(
    itemsOfList: List<Item>,
    onDelete: (UUID) -> Unit,
){
    if (itemsOfList.isEmpty()){
        EmptyCard()
    }else{
        LazyColumn(){
            items(itemsOfList.size){
                ItemCard(
                    item = itemsOfList[it],
                    onDelete = {id->
                        onDelete(id)
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyCard(modifier: Modifier= Modifier){
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            Text(
                text = "No items to display",
                color = Color.Black
            )
            Text(
                text = "Try to use + button",
                color = Color.Black
            )
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onClick: (Int) -> Unit = {},
    onEdit: (Int) -> Unit = {},
    onDelete: (UUID) -> Unit = {},
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    Card(
        elevation = 4.dp,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {/*TODO*/ }
    ) {
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
                    .weight(6f)
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween){
                    Text(
                        text = "${item.weight} Kg",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = modifier.weight(1f)
                    )

                    Spacer(modifier = modifier.weight(0.5f))
                    Text(
                        text = "${item.price} UZS",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = modifier.weight(2f)
                    )
                }
            }

            Spacer(modifier = modifier.weight(2f))

            Column(modifier = Modifier.weight(3f)) {
                Row {
                    IconButton(onClick = { Toast.makeText(context,"pressed on edit button", Toast.LENGTH_SHORT).show() }) {
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
    }
}

@Composable
fun NewPurchaseListItemDialog(
    listId: UUID,
    setShowDialog: (Boolean) -> Unit,
    onConfirm: (Item) -> Unit,
    modifier: Modifier = Modifier,
){
    var fieldValue by remember{ mutableStateOf(TextFieldValue("")) }
    var weight by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var totalPrice : Float = 0.0f


    Dialog(onDismissRequest = {setShowDialog(false)}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
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
                    label = {
                        Text(
                            text = "Enter new Item name: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )

                OutlinedTextField(
                    value = weight,
                    onValueChange = {weight = it},
                    placeholder = {Text(text = "ex 10.0")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.padding(top = 4.dp),
                    label = {
                        Text(
                            text = "Weight: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = {price = it},
                    placeholder = {Text(text = "ex 10000")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.padding(top = 4.dp),
                    label = {
                        Text(
                            text = "Price: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )

                //totalPrice = weight.text.toFloat() * price.text.toFloat()

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            totalPrice = weight.text.toFloat() * price.text.toFloat()
                            val tempItem = Item(
                                name = fieldValue.text,
                                weight = weight.text.toFloat(),
                                price = price.text.toFloat(),
                                total = totalPrice,
                                listId = listId
                            )
                            onConfirm(tempItem)
                            setShowDialog(false)
                        }
                    ) { Text(text = "Confirm") }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { setShowDialog(false)}
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun AppBarItem(name: String, retryAction: () -> Unit) {
    var title = stringResource(id = R.string.app_name)
    title = "$title > $name"
    TopAppBar(
        title = { Text(text = title) },
        actions = {
            IconButton(onClick = retryAction) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    )
}