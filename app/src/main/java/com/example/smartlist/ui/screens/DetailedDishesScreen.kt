package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.Recipe
import java.util.UUID

@Composable
fun DetailedDishesScreen(
    dishViewModel: DishViewModel,
    navController: NavController,
    onDelete: (Recipe) -> Unit,
    onSubmit: (Recipe) -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: ()->Unit = {/*TODO add refresh action in DishViewModel*/},
){
    val state: RecipeUiState = dishViewModel.recipeUiState


    Scaffold(
        topBar = { DishAppBar {onRefresh()}},
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                /*TODO make a new dialog where user can add his custom recipe*/
                dishViewModel.insertRecipe(Recipe(
                    id = UUID.randomUUID(),
                    listId = dishViewModel.currentListId,
                    name = "Peperoni yetit",
                    portions = 1,
                ))
            }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "add new recipe")
            }
        }
    ) {
        Surface(modifier.padding(it)) {


            when(state){
                is RecipeUiState.Loading ->{}
                is RecipeUiState.Error ->{}
                is RecipeUiState.Success ->{
                    //Main content
                    ResultScreen(
                        list = state.recipeList,
                        onDelete = onDelete,
                        onSubmit = onSubmit,
                    )
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    list: List<Recipe>,
    onDelete: (Recipe) -> Unit,
    onSubmit: (Recipe) -> Unit,
){
    LazyColumn {
        item{
            SearchCard()
        }
        items(list.size){id->
            RecipeCard(
                recipe = list[id],
                onDelete = onDelete,
                onSubmit = onSubmit
            )
        }
    }

}

@Composable
fun SearchCard(modifier: Modifier = Modifier){

    var searchText by remember { mutableStateOf("")}

    Card(
        elevation = 4.dp,
        modifier = modifier
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


@Composable
fun RecipeCard(
    recipe: Recipe,
    onDelete: (Recipe)->Unit,
    onSubmit: (Recipe) -> Unit,
    modifier: Modifier = Modifier
){

    //variable section
    val isExpanded = remember { mutableStateOf(false) }

    val name = remember { mutableStateOf(recipe.name) }

    val portions = remember { mutableStateOf(recipe.portions) }

    val context = LocalContext.current


    Card(
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
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
                    IconButton(onClick = {
                        Toast.makeText(context,"Delete recipe", Toast.LENGTH_SHORT).show()
                        onDelete(recipe) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete recipe")
                    }
                }
            }

            if(isExpanded.value){
                RecipeCardEditScreen(
                    recipe = recipe,
                    isExpanded = isExpanded,
                    name = name,
                    portions = portions,
                    onSubmit = onSubmit,
                )
            }
        }
    }
}

@Composable
fun RecipeCardEditScreen(
    recipe: Recipe,
    isExpanded: MutableState<Boolean>,
    name: MutableState<String>,
    portions: MutableState<Int>,
    onSubmit: (Recipe)->Unit,
    modifier: Modifier = Modifier
){
    var nameField by remember { mutableStateOf(TextFieldValue(name.value)) }
    var portionsField by remember { mutableStateOf(portions.value) }

    var errorMessage by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        //Name
        Row {
            OutlinedTextField(
                value = nameField,
                onValueChange = {nameField = it},
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
                    onClick = { if (portionsField != 0) portionsField-- },
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(text = "-")
                }

                Text(
                    text = "$portionsField",
                    modifier = Modifier.padding(8.dp)
                )

                OutlinedButton(
                    onClick = { portionsField++ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(text = "+")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.weight(1f)) {
                IconButton(onClick = {
                    if(nameField.text.isBlank() || portionsField <=0){
                        errorMessage = true
                    }
                    else{
                        val newRecipe = Recipe(
                            id = recipe.id,
                            name = nameField.text,
                            listId = recipe.listId,
                            portions = portionsField
                        )

                        onSubmit(newRecipe)
                        isExpanded.value = false
                    }
                }) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Submit changes")
                }
                IconButton(onClick = { isExpanded.value = false }) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel")
                }
            }
        }

        Row {
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
}