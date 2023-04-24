package com.example.smartlist.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.navigation.Screen
import java.time.LocalDate
import java.util.*


private const val TAG = "PurchasesScreen"
@Composable
fun PurchasesScreen(
    navController: NavController,
    purchaseViewModel: PurchaseViewModel,
    onSubmit: (PurchaseList) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val state: PurchaseUiState = purchaseViewModel.purchaseUiState

    if (showDialog.value){
        NewPurchaseListDialog(
            setShowDialog = {showDialog.value = it},
            onConfirm = {newPurchaseList ->
                onSubmit(newPurchaseList)
            }
        )
    }

    Scaffold(
        topBar = {AppBar(onRefresh)},
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true}) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add new List")
            }
        }
    ) { it ->
        Surface(
            modifier = modifier
                .padding(it)
        ) {

            Log.d(TAG,"state is: $state")

            when(state){
                is PurchaseUiState.Loading ->{}
                is PurchaseUiState.Error ->{}
                is PurchaseUiState.Success ->{
                    ResultScreen(
                        lists = state.purchaseLists,
                        onClick = {
                            Log.d(TAG,"Try to navigate")
                            purchaseViewModel.currentListId = it
                            purchaseViewModel.getItemsOfPurchaseList()
                            purchaseViewModel.getListSize(it)
                            purchaseViewModel.getListNameFromDb(it)
                            navController
                                .navigate(
                                    Screen.DetailedPurchaseListScreen.withArgs(
                                        it.toString()
                                    )
                                )
                        }
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
){
    LazyColumn(){
        items(lists.size){index ->
            ListCard(
                list = lists[index],
                onClick = { onClick(lists[index].id)},
                onEdit = {},
                onDelete = {},
            )
        }
    }
}


@Composable
fun ListCard(
    list: PurchaseList,
    onClick: (UUID) -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    Card(
        elevation = 4.dp,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                onClick(list.id)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(4.dp)
        ) {
            Column(
                modifier = Modifier.weight(3f)
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

            Spacer(modifier = modifier.weight(5f))

            Column(modifier = Modifier.weight(3f)) {
                Row {
                    IconButton(onClick = { Toast.makeText(context,"pressed on edit button",Toast.LENGTH_SHORT).show() }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit current list")
                    }
                    IconButton(onClick = { Toast.makeText(context,"pressed on delete button",Toast.LENGTH_SHORT).show() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete current list")
                    }
                }
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
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = {fieldValue = it},
                    placeholder = {Text(text = "ex Market list 1")},
                    label = {
                        Text(
                            text = "Enter new list name: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val date = LocalDate.now()
                            val list = PurchaseList(
                                name = fieldValue.text,
                                listSize = 0,
                                year = date.year,
                                month = date.month.name,
                                day = date.dayOfMonth
                            )
                            onConfirm(list)
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
fun AppBar(retryAction: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = retryAction) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    )
}
