package com.example.smartlist.ui.screens

import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.Month
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.navigation.Screen

@Composable
fun PurchasesScreen(navController: NavController, modifier: Modifier = Modifier){
    Scaffold(
        topBar = {},
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add new List")
            }
        }
    ) { it ->
        Surface(
            modifier = modifier
                .padding(it)
        ) {

            val lists1 = listOf<PurchaseList>(
                PurchaseList(
                    1,
                    "List 1",
                    0,
                    2023,
                    "APRIL",
                    20
                ),
                PurchaseList(
                    2,
                    "List 2",
                    2,
                    2023,
                    "APRIL",
                    26
                ),
                PurchaseList(
                    3,
                    "List 2",
                    3,
                    2023,
                    "MAY",
                    28
                ),
                PurchaseList(
                    4,
                    "List 4",
                    2,
                    2023,
                    "APRIL",
                    27
                ),
            )

            val context = LocalContext.current

            LazyColumn(){
                items(lists1.size){index ->
                    ListCard(
                        list = lists1[index],
                        onClick = {listId->
                            navController.navigate(Screen.DetailedPurchaseListScreen.withArgs(listId.toString()))},
                        onEdit = {},
                        onDelete = {},
                    )
                }
            }

        }
    }
}

@Composable
fun MonthScreen(lists: List<PurchaseList>,modifier: Modifier = Modifier){
    LazyColumn(){
        items(1){
            MonthCard(month = Month.JANUARY.name, lists = filterLists(lists,Month.JANUARY.name))
            MonthCard(month = Month.FEBRUARY.name, lists = filterLists(lists,Month.FEBRUARY.name))
            MonthCard(month = Month.MARCH.name, lists = filterLists(lists,Month.MARCH.name))
            MonthCard(month = Month.APRIL.name, lists = filterLists(lists,Month.APRIL.name))
            MonthCard(month = Month.MAY.name, lists = filterLists(lists,Month.MAY.name))
            MonthCard(month = Month.JUNE.name, lists = filterLists(lists,Month.JUNE.name))
            MonthCard(month = Month.JULY.name, lists = filterLists(lists,Month.JULY.name))
            MonthCard(month = Month.AUGUST.name, lists = filterLists(lists,Month.AUGUST.name))
            MonthCard(month = Month.SEPTEMBER.name, lists = filterLists(lists,Month.SEPTEMBER.name))
            MonthCard(month = Month.OCTOBER.name, lists = filterLists(lists,Month.OCTOBER.name))
            MonthCard(month = Month.NOVEMBER.name, lists = filterLists(lists,Month.NOVEMBER.name))
            MonthCard(month = Month.DECEMBER.name, lists = filterLists(lists,Month.DECEMBER.name))
        }
    }
}

@Composable
fun ListCard(
    list: PurchaseList,
    onClick: (Int) -> Unit,
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
            .clickable {onClick(list.id)}
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
fun MonthCard(
    month: String,
    lists: List<PurchaseList>,
    modifier: Modifier = Modifier)
{
    var isExpanded by remember { mutableStateOf(false) } //if section contain lists
    var expandHeight by remember { mutableStateOf(50) }
    val context = LocalContext.current
    Card(
        backgroundColor = Color.Gray,
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(expandHeight.dp)
    ) {
        Column() {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = month,
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(3.5f)
                )
                Spacer(modifier = Modifier.weight(4.5f))

                IconButton(onClick = {
                    if (!isExpanded){
                        expandHeight = 200
                        isExpanded = true
                    } else{
                        expandHeight = 50
                        isExpanded = false
                    }
                }) {
                    MonthCardExpandButton(lists, isExpanded)
                }

            }

            if (isExpanded){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.background(Color.White)
                    ) {
                        LazyColumn(){
                            items(lists.size){index->
                                ListCard(
                                    list = lists[index],
                                    onClick = {},
                                    onEdit = {},
                                    onDelete = {},
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthCardExpandButton(lists: List<PurchaseList>, isExpanded: Boolean){
    return if (lists.isEmpty()){
        Spacer(modifier = Modifier.size(20.dp))
    }else{
        Icon(
            imageVector = if(!isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
            contentDescription = if(!isExpanded) "Expand more" else "Expand less"
        )
    }
}

fun filterLists(lists: List<PurchaseList>, filterValue: String): List<PurchaseList>{
    var temp = emptyList<PurchaseList>()
    lists.forEach {
        if (it.month == filterValue){
            temp = temp + it
        }
    }
    return temp
}