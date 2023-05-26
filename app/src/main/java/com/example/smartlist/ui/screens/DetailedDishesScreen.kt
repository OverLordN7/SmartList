package com.example.smartlist.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.Item
import com.example.smartlist.model.Recipe
import java.util.UUID


private const val TAG = "DetailedDishesScreen"

@Composable
fun DetailedDishesScreen(
    dishViewModel: DishViewModel,
    navController: NavController,
    onDelete: (Recipe) -> Unit,
    onSubmit: (Recipe) -> Unit,
    addNewRecipe: (Recipe) -> Unit,
    insertNewDishComponent: (DishComponent) -> Unit,
    loadDishComponent: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: ()->Unit,
){
    val state: RecipeUiState = dishViewModel.recipeUiState
    val showDialog = remember { mutableStateOf(false) }

    val dishComponentList = dishViewModel.dishComponents.collectAsState(emptyList())

    if (showDialog.value){
        NewRecipeDialog(
            setShowDialog = {showDialog.value = it},
            currentListId = dishViewModel.currentListId,
            onConfirm = addNewRecipe,
        )
    }

    Scaffold(
        topBar = { DishAppBar {onRefresh()}},
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {showDialog.value = true} ) {
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
                        insertNewDishComponent = insertNewDishComponent,
                        loadDishComponent = loadDishComponent,
                        dishComponentList = dishComponentList.value
                    )
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    list: List<Recipe>,
    dishComponentList: List<DishComponent>,
    onDelete: (Recipe) -> Unit,
    onSubmit: (Recipe) -> Unit,
    insertNewDishComponent: (DishComponent) -> Unit,
    loadDishComponent: (UUID) -> Unit,
){
    LazyColumn {
        item{
            SearchCard()
        }

        items(list.size){id->
            RecipeCard(
                recipe = list[id],
                dishComponentList = dishComponentList,
                onDelete = onDelete,
                onSubmit = onSubmit,
                insertNewDishComponent = insertNewDishComponent,
                loadDishComponent = loadDishComponent

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
    dishComponentList: List<DishComponent>,
    onDelete: (Recipe)->Unit,
    onSubmit: (Recipe) -> Unit,
    insertNewDishComponent: (DishComponent) -> Unit,
    loadDishComponent: (UUID) -> Unit,
    modifier: Modifier = Modifier
){
    val isExpanded = remember { mutableStateOf(false) }
    val showDishComponents = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                showDishComponents.value = !showDishComponents.value
                loadDishComponent(recipe.id)
            }
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.pasta1),
                        contentDescription = "Image of recipe",
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
                        Text(text = recipe.name)
                        Text(text = "Portions: ${recipe.portions}")
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit recipe")
                    }
                    IconButton(onClick = {onDelete(recipe)} ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete recipe")
                    }
                }
            }

            if(isExpanded.value){
                RecipeCardEditScreen(recipe = recipe, isExpanded = isExpanded, onSubmit = onSubmit)
            }

            if(showDishComponents.value){
                RecipeCardList(
                    recipeId = recipe.id,
                    dishComponentList = dishComponentList,
                    insertNewDishComponent = insertNewDishComponent,
                )
            }
        }
    }
}

@Composable
fun RecipeCardList(
    recipeId: UUID,
    dishComponentList: List<DishComponent>,
    insertNewDishComponent: (DishComponent) -> Unit,
    modifier: Modifier = Modifier
){

    val height by remember { mutableStateOf(300) }

    val showDialog = remember { mutableStateOf(false) }

    val itemList: ArrayList<Item> = ArrayList()

    if (showDialog.value){
        NewDishComponentDialog(
            recipeId = recipeId,
            setShowDialog = {showDialog.value = it},
            onConfirm = {
                insertNewDishComponent(it)
            }
        )
    }

    dishComponentList.forEach {
        val newItem = Item(
            id = it.id,
            name = it.name,
            weight = it.weight,
            weightType = it.weightType,
            price = it.price,
            total = it.total,
            isBought = false,
            listId = it.recipeId
        )
        itemList.add(newItem)
    }

    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
    ) {
        LazyColumn{

            item {
                val stroke = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f,10f),0f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(4.dp)
                        .drawBehind { drawRoundRect(color = Color.DarkGray, style = stroke) }
                        .clickable {
                            //TODO add a new ingredient to DB
                            //Toast.makeText(context, "Adding new item...",Toast.LENGTH_SHORT).show()
                            //TODO add custom dialog to insert new ingredient

//                            val newIngredient = DishComponent(
//                                id = UUID.randomUUID(),
//                                recipeId = recipeId,
//                                name = "Tomato",
//                                weight = 2.0f,
//                                weightType = "kg",
//                                price = 5000.0f,
//                                total = 5000.0f * 2.0f,
//                            )
//
//                            insertNewDishComponent(newIngredient)

                                   showDialog.value = !showDialog.value
                        }
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Add a new ingredient", textAlign = TextAlign.Center)
                }

            }

            items(dishComponentList.size){
                ItemCard(
                    item = itemList[it],
                    onClick = {_,_ ->
                              Toast.makeText(context,"test",Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

    }
}

@Composable
fun RecipeCardEditScreen(
    recipe: Recipe,
    isExpanded: MutableState<Boolean>,
    onSubmit: (Recipe)->Unit,
    modifier: Modifier = Modifier
){
    var nameField by remember { mutableStateOf(TextFieldValue(recipe.name)) }
    var portionsField by remember { mutableStateOf(recipe.portions) }
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

        //Portions
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            Row(modifier = Modifier.weight(1f)) {

                OutlinedButton(
                    onClick = { if (portionsField != 0) portionsField-- },
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(text = "-")
                }

                Text(text = "$portionsField", modifier = Modifier.padding(8.dp) )

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

                        isExpanded.value = false
                        onSubmit(newRecipe)
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

@Composable
fun NewRecipeDialog(
    setShowDialog: (Boolean) -> Unit,
    currentListId: UUID,
    onConfirm: (Recipe) -> Unit,
    modifier: Modifier = Modifier,
){
    var nameField by remember{ mutableStateOf(TextFieldValue("")) }
    var errorFieldStatus by remember { mutableStateOf(false) }


    Dialog(onDismissRequest = {setShowDialog(false)}) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
        ) {
            LazyColumn(modifier = modifier.fillMaxWidth()){
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pasta1),
                            contentDescription ="Test",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(64.dp),
                        )
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            OutlinedTextField(
                                value = nameField,
                                onValueChange = { nameField = it},
                                placeholder = { Text(text = "Cake") },
                                label = {
                                    Text(
                                        text = "Enter new recipe name",
                                        color = Color.Black,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    autoCorrect = true,
                                    imeAction = ImeAction.Next,
                                )
                            )
                            Text(
                                text = "Portions: 1",
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
                item{
                    if (errorFieldStatus){
                        Text(
                            text = "*Sure that you fill all fields, if message still remains, check symbols",
                            color = Color.Red,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .height(40.dp)
                        )
                    }
                    else{
                        Spacer(modifier = Modifier.height(40.dp))
                    }



                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { setShowDialog(false) }
                        ) {
                            Text(text = "Cancel")
                        }

                        Spacer(modifier = Modifier.weight(0.5f))

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                //Check if all fields are not null
                                if (nameField.text.isBlank()){
                                    errorFieldStatus = true
                                }
                                else{
                                    val newRecipe = Recipe(
                                        id = UUID.randomUUID(),
                                        listId = currentListId,
                                        name = nameField.text,
                                        portions = 1,
                                    )
                                    onConfirm(newRecipe)
                                    setShowDialog(false)
                                }
                            }
                        ) {
                            Text(text = "Confirm")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewDishComponentDialog(
    recipeId: UUID,
    setShowDialog: (Boolean) -> Unit,
    onConfirm: (DishComponent) -> Unit,
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
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
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
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
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
                                //TODO create a new DishComponent and submit it
                                val newDishComponent = DishComponent(
                                    id = UUID.randomUUID(),
                                    recipeId = recipeId,
                                    name = fieldValue.text,
                                    weight = weight.text.toFloat(),
                                    weightType = selectedOptionText,
                                    price = price.text.toFloat(),
                                    total = weight.text.toFloat() * price.text.toFloat(),
                                )
                                setShowDialog(false)
                                onConfirm(newDishComponent)
                            }
                        }
                    ) { Text(text = "Confirm") }
                }
            }
        }
    }


}