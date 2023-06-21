package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
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
import com.example.smartlist.model.DishList
import com.example.smartlist.model.Item
import com.example.smartlist.model.MenuItem
import com.example.smartlist.model.Recipe
import com.example.smartlist.model.items
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.MainAppBar
import com.example.smartlist.ui.theme.Cal100
import com.example.smartlist.ui.theme.Carb100
import com.example.smartlist.ui.theme.Fats100
import com.example.smartlist.ui.theme.Orange100
import com.example.smartlist.ui.theme.Orange150
import com.example.smartlist.ui.theme.Protein100
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    loadDishComponent: (Recipe) -> Unit,
    deleteDishComponent: (UUID) -> Unit,
    onEdit: (DishComponent) -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: ()->Unit,
    onExport: (String)->Unit,
){
    val state: RecipeUiState = dishViewModel.recipeUiState
    val showDialog = remember { mutableStateOf(false) }

    val menuState = remember { mutableStateOf(false) }

    val dishComponentList = dishViewModel.dishComponents.collectAsState(emptyList())

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (showDialog.value){
        NewRecipeDialog(
            setShowDialog = {showDialog.value = it},
            currentListId = dishViewModel.currentListId,
            onConfirm = addNewRecipe,
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MainAppBar(
                name = dishViewModel.currentName,
                menuState = menuState,
                retryAction = onRefresh,
                onExport = onExport,
                onNavigationIconClick = {
                    scope.launch { scaffoldState.drawerState.open()
                    } },
            )
        },
        drawerContent = {
            DrawerHeader()
            DrawerBody(
                items = items,
                onItemClick = {
                    when(it.id){
                        "dishList" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.DishesScreen.route)
                        }
                        "purchaseList" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.PurchasesScreen.route)
                        }
                        "graphs" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.GraphScreen.route)
                        }
                        "home" ->{
                            scope.launch { scaffoldState.drawerState.close() }
                            navController.navigate(Screen.HomeScreen.route)
                        }
                        else -> {
                            val message = context.getString(R.string.menu_item_toast_default,it.title)
                            Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {showDialog.value = true} ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "add new recipe")
            }
        }
    ) {
        Surface(modifier = modifier.padding(it).fillMaxSize()) {
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
                        dishComponentList = dishComponentList.value,
                        deleteDishComponent = deleteDishComponent,
                        onEdit = onEdit,
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
    loadDishComponent: (Recipe) -> Unit,
    deleteDishComponent: (UUID) -> Unit,
    onEdit: (DishComponent) -> Unit,
){
    LazyColumn {
        //item { SearchCard() }

        items(list.size){id->
            RecipeCard(
                recipe = list[id],
                dishComponentList = dishComponentList,
                onDelete = onDelete,
                onSubmit = onSubmit,
                insertNewDishComponent = insertNewDishComponent,
                loadDishComponent = loadDishComponent,
                deleteDishComponent = deleteDishComponent,
                onEdit = onEdit,

            )
        }
    }

}

//@Composable
//fun SearchCard(modifier: Modifier = Modifier){
//
//    var searchText by remember { mutableStateOf("")}
//
//    Card(
//        elevation = 4.dp,
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Column(
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            TextField(
//                value = searchText,
//                onValueChange = {searchText = it},
//                placeholder = { Text(text = "Search..") },
//                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp)
//            )
//        }
//    }
//}



@Composable
fun RecipeCard(
    recipe: Recipe,
    dishComponentList: List<DishComponent>,
    onDelete: (Recipe)->Unit,
    onSubmit: (Recipe) -> Unit,
    insertNewDishComponent: (DishComponent) -> Unit,
    loadDishComponent: (Recipe) -> Unit,
    deleteDishComponent: (UUID) -> Unit,
    onEdit: (DishComponent) -> Unit,
    modifier: Modifier = Modifier
){
    val isExpanded = remember { mutableStateOf(false) }
    val showDishComponents = remember { mutableStateOf(false) }
    val showCalTable = remember { mutableStateOf(false) }

    Card(
        backgroundColor = Orange100,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                showDishComponents.value = !showDishComponents.value
                showCalTable.value = !showCalTable.value
                loadDishComponent(recipe)
            }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        elevation = 4.dp,

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
            if (showCalTable.value){
                var carbs = 0.0f
                var fat = 0.0f
                var protein = 0.0f
                var cal = 0.0f

                dishComponentList.forEach {
                    carbs += it.carbs
                    fat += it.fat
                    protein +=it.protein
                    cal += it.cal
                }

                RecipeCardCalTable(
                    carbs = carbs * recipe.portions,
                    fat = fat * recipe.portions,
                    protein = protein * recipe.portions,
                    cal = cal * recipe.portions
                )
            }

            if(showDishComponents.value){
                RecipeCardList(
                    recipe = recipe,
                    dishComponentList = dishComponentList,
                    insertNewDishComponent = insertNewDishComponent,
                    onEdit = onEdit,
                    deleteDishComponent = deleteDishComponent,

                )
            }
        }
    }
}

@Composable
fun RecipeCardList(
    recipe: Recipe,
    dishComponentList: List<DishComponent>,
    insertNewDishComponent: (DishComponent) -> Unit,
    deleteDishComponent: (UUID) -> Unit,
    onEdit: (DishComponent) -> Unit,
    modifier: Modifier = Modifier
){

    val height by remember { mutableStateOf(300) }

    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value){
        NewDishComponentDialog(
            recipeId = recipe.id,
            setShowDialog = {showDialog.value = it},
            onConfirm = {
                insertNewDishComponent(it)
            }
        )
    }


    Card(
        backgroundColor = Orange100,
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
                        .padding(start = 8.dp, end = 8.dp, bottom = 4.dp, top = 16.dp)
                        .drawBehind { drawRoundRect(color = Color.DarkGray, style = stroke) }
                        .clickable { showDialog.value = !showDialog.value },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Add a new ingredient", textAlign = TextAlign.Center)
                }

            }

            items(dishComponentList.size){

                DishComponentCard(
                    component = dishComponentList[it],
                    onEdit = onEdit,
                    onDelete = deleteDishComponent,
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
fun RecipeCardCalTable(
    carbs: Float,
    fat: Float,
    protein: Float,
    cal: Float,
    modifier: Modifier = Modifier
){
    Card(
        elevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            //TODO make colors of text (fats, protein, Ccal etc.) more soft
            //Carbohydrates - Углеводы
            Text(text = "Carb: ${carbs.toInt()} g", color = Carb100)
            // Fat - Жиры
            Text(text = "Fat: ${fat.toInt()} g", color = Fats100)
            // Protein - Белки
            Text(text = " Protein: ${protein.toInt()} g", color = Protein100)
            //Calories - Калории
            Text(text = " Ccal: ${cal.toInt()} ", color = Cal100)

        }
    }
}

@Composable
fun DishComponentCard(
    component: DishComponent,
    modifier: Modifier = Modifier,
    onEdit: (DishComponent) -> Unit = {},
    onDelete: (UUID) -> Unit = {},
){
    val context = LocalContext.current
    val isExpanded = remember { mutableStateOf(false) }

    Card(
        elevation = 4.dp,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()

    ) {
        Column {
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
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(8f)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = component.name,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Row(horizontalArrangement = Arrangement.SpaceAround){
                        Text(
                            text = "${component.weight} ${component.weightType}",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = modifier
                                .weight(3f)
                                .padding(4.dp)
                        )

                        Text(
                            text = "${component.total.toInt()} UZS",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = modifier
                                .weight(4f)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = modifier.weight(1f))

                Column(modifier = Modifier
                    .weight(4f)
                ) {
                    Row {
                        IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit current list")
                        }
                        IconButton(onClick = {
                            Toast.makeText(context,"Deleting item...", Toast.LENGTH_SHORT).show()
                            onDelete(component.id)
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
                    DishComponentEditScreen(
                        dishComponent = component,
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
fun DishComponentEditScreen(
    dishComponent: DishComponent,
    isExpanded: MutableState<Boolean>,
    onSubmit: (DishComponent)-> Unit,
    modifier: Modifier = Modifier
){
    var fieldValue by remember{ mutableStateOf(TextFieldValue(dishComponent.name)) }
    var weight by remember { mutableStateOf(TextFieldValue(dishComponent.weight.toString())) }
    var price by remember { mutableStateOf(TextFieldValue(dishComponent.price.toString())) }
    var totalPrice : Float = dishComponent.weight * dishComponent.price
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
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
            )

            OutlinedTextField(
                value = weight,
                onValueChange = {weight = it},
                placeholder = {Text(text = "ex 10.0")},
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
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
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
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
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
                        val temp = DishComponent(
                            id = dishComponent.id,
                            name = fieldValue.text,
                            weight = weight.text.toFloat(),
                            weightType = selectedOptionText,
                            price = price.text.toFloat(),
                            total = weight.text.toFloat() * price.text.toFloat(),
                            recipeId = dishComponent.recipeId
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
    var error by remember { mutableStateOf(false) }
    var name by remember{ mutableStateOf(TextFieldValue("")) }
    var weight by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }

    //values for DropDownMenu
    val options = listOf("kgs","lbs","pcs","pkg")
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    //values for additional info regarding carbs, fats, protein, cals
    var switchState by remember { mutableStateOf(false) }
    var carbs by remember{ mutableStateOf(TextFieldValue("")) }
    var fats by remember { mutableStateOf(TextFieldValue("")) }
    var protein by remember { mutableStateOf(TextFieldValue("")) }
    var cal by remember { mutableStateOf(TextFieldValue("")) }

    Dialog(onDismissRequest = {setShowDialog(false)}) {

        Surface( shape = RoundedCornerShape(16.dp), color = Color.White) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = modifier.padding(8.dp)
            ) {
                //Header of dialog
                Text(text = "New DishComponent", color = Color.Black, fontSize = 28.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = "Name: ",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = {name = it},
                        placeholder = {Text(text = "ex Potato")},
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrect = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.weight(2f)
                    )

                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "Weight: ",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = {weight = it},
                        placeholder = {Text(text = "ex 10.0")},
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.weight(2f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = "Unit: ",
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = "Price: ",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = {price = it},
                        placeholder = {Text(text = "ex 10000")},
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.weight(2f),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    if (error){
                        Text(
                            text = "*Sure that you fill all fields, if message still remains, check symbols",
                            color = Color.Red,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    } else{
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "Additional info: ",
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = switchState,
                        onCheckedChange = {switchState = it},
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = "Carbs: ",
                        fontSize = 16.sp,
                        color = if(switchState) Color.Black else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = {carbs = it},
                        placeholder = {Text(text = "ex 5.3")},
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        enabled = switchState,
                        modifier = Modifier.weight(2f),

                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = "Fats: ",
                        fontSize = 16.sp,
                        color = if(switchState) Color.Black else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = {fats = it},
                        placeholder = {Text(text = "ex 10.5")},
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        enabled = switchState,
                        modifier = Modifier.weight(2f),

                        )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = "Protein: ",
                        fontSize = 16.sp,
                        color = if(switchState) Color.Black else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = {protein = it},
                        placeholder = {Text(text = "ex 7.1")},
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        enabled = switchState,
                        modifier = Modifier.weight(2f),

                        )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(4.dp)
                ){
                    Text(
                        text = "Cals: ",
                        fontSize = 16.sp,
                        color = if(switchState) Color.Black else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cal,
                        onValueChange = {cal = it},
                        placeholder = {Text(text = "ex 200")},
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        enabled = switchState,
                        modifier = Modifier.weight(2f),

                        )
                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ){

                    Button(
                        onClick = { setShowDialog(false)},
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                    ) {
                        Text(text = "Cancel")
                    }

                    Button(
                        onClick = {
                            //Check if all fields are not null
                            if (checkForError(name, weight, price)){
                                error = true
                            }
                            else{
                                //Check is OK, continue..
                                var newDishComponent = DishComponent(
                                    id = UUID.randomUUID(),
                                    recipeId = recipeId,
                                    name = name.text,
                                    weight = weight.text.toFloat(),
                                    weightType = selectedOptionText,
                                    price = price.text.toFloat(),
                                    total = weight.text.toFloat() * price.text.toFloat(),
                                )

                                if (checkSwitchForError(switchState, carbs, fats, protein, cal)){
                                    error = true
                                }
                                else{
                                    newDishComponent = newDishComponent.copy(
                                        carbs = carbs.text.toFloat(),
                                        fat = fats.text.toFloat(),
                                        protein = protein.text.toFloat(),
                                        cal = cal.text.toFloat()
                                    )
                                }

                                setShowDialog(false)
                                onConfirm(newDishComponent)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),

                    ) { Text(text = "Confirm") }
                }
            }
        }
    }
}

fun checkForError(name: TextFieldValue, weight: TextFieldValue, price: TextFieldValue, ): Boolean {
    return name.text.isBlank() || weight.text.isBlank() || (price.text.isBlank() || price.text.toFloat() <= 0.0f)
}

fun checkSwitchForError(switchState: Boolean, carbs: TextFieldValue, fats: TextFieldValue, protein: TextFieldValue, cal: TextFieldValue):Boolean{
    return if (switchState){
        (carbs.text.isBlank() || carbs.text.toFloat() <0.0f) ||
                (fats.text.isBlank() || fats.text.toFloat() <0.0f) ||
                (protein.text.isBlank() || protein.text.toFloat() <0.0f) ||
                (cal.text.isBlank() || cal.text.toFloat() <0.0f)
    } else{
        false
    }
}