package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlist.R
import com.example.smartlist.model.Item
import com.example.smartlist.model.PurchaseList
import com.example.smartlist.navigation.Screen
import java.util.UUID

@Composable
fun DetailedPurchaseListScreen(
    listId: String,
    purchaseViewModel: PurchaseViewModel,
    modifier: Modifier = Modifier
){
    purchaseViewModel.getItemsOfPurchaseList(listId = UUID.fromString(listId))
    val state: PurchaseItemUiState = purchaseViewModel.purchaseItemUiState


    when(state){
        is PurchaseItemUiState.Loading ->{}
        is PurchaseItemUiState.Error ->{}
        is PurchaseItemUiState.Success ->{
            ResultItemScreen(itemsOfList = state.items)
        }
    }
}


@Composable
fun ResultItemScreen(
    itemsOfList: List<Item>
){
    LazyColumn(){
        items(itemsOfList.size){
            ItemCard(itemsOfList[it])
        }
    }
}


@Composable
fun ItemCard(
    item: Item,
    onClick: (Int) -> Unit = {},
    onEdit: (Int) -> Unit = {},
    onDelete: (Int) -> Unit = {},
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
                modifier = Modifier.weight(6f).padding(top = 4.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 20.sp,
                    color = Color.Black
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween){
                    Text(
                        text = item.weight.toString(),
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
                    IconButton(onClick = { Toast.makeText(context,"pressed on delete button", Toast.LENGTH_SHORT).show() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete current list")
                    }
                }
            }
        }
    }
}