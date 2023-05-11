package com.example.smartlist.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartlist.R

@Composable
fun DetailedDishesScreen(
    dishViewModel: DishViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
){
    Scaffold(
        topBar = { Text(text = stringResource(id = R.string.app_name))}
    ) {
        Surface(modifier.padding(it)) {

            //Main content
            LazyColumn(){
                item{
                    SearchCard()
                }
                item{
                    RecipeCard()
                }
                item{
                    RecipeCard()
                }
                item{
                    RecipeCard()
                }
                item{
                    RecipeCard()
                }
                item{
                    RecipeCard()
                }

            }
        }
    }
}

@Composable
fun SearchCard(modifier: Modifier = Modifier){

    var searchText by remember { mutableStateOf("")}

    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                value = searchText,
                onValueChange = {searchText = it},
                placeholder = { Text(text = "Search..") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeCard(modifier: Modifier = Modifier){

    //variable section
    var isExpanded = remember { mutableStateOf(false) }

    var name = remember { mutableStateOf("Pasta Pepeproni por fovor") }

    var portions = remember { mutableStateOf(5) }


    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column() {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(){
                    Image(
                        painter = painterResource(id = R.drawable.pasta1),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .border(2.dp, Color.Gray, CircleShape)
                    )

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(text = name.value)
                        Text(text = "Portions: ${portions.value}")
                    }

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "")
                    }
                }
            }

            if(isExpanded.value){
                RecipeCardEditScreen(
                    isExpanded = isExpanded,
                    name = name,
                    portions = portions,
                )
            }
        }
    }
}

@Composable
fun RecipeCardEditScreen(
    isExpanded: MutableState<Boolean>,
    name: MutableState<String>,
    portions: MutableState<Int>,
    modifier: Modifier = Modifier
){
    var name by remember { mutableStateOf(TextFieldValue(name.value)) }
    var portions by remember { mutableStateOf(portions.value) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        //Name
        Row() {
            OutlinedTextField(
                value = name,
                onValueChange = {name = it},
                placeholder = {Text(text = "ex Peperoni")},
                modifier = Modifier.padding(4.dp),
                label = {
                    Text(
                        text = "Enter new name: ",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            //Portions
            Row(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { if (portions != 0) portions-- },
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(text = "-")
                }

                Text(
                    text = "$portions",
                    modifier = Modifier.padding(8.dp)
                )

                OutlinedButton(
                    onClick = { portions++ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(text = "+")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.weight(1f)) {
                IconButton(onClick = { isExpanded.value = false }) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Submit changes")
                }
                IconButton(onClick = { isExpanded.value = false }) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel")
                }
            }
        }
    }
}