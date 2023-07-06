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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.DishComponent
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.model.Recipe
import com.example.smartlist.model.items
import com.example.smartlist.navigation.Screen
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.MainAppBar
import com.example.smartlist.ui.theme.Cal200
import com.example.smartlist.ui.theme.Carb200
import com.example.smartlist.ui.theme.Fat200
import com.example.smartlist.ui.theme.LightBlue200
import com.example.smartlist.ui.theme.Protein200
import kotlinx.coroutines.launch
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
    val context = LocalContext.current

    val dishComponentList = dishViewModel.dishComponents.collectAsState(emptyList())

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

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
                onNavigationIconClick = { scope.launch { scaffoldState.drawerState.open() } },
            )
        },
        drawerContent = {
            DrawerHeader()
            DrawerBody(
                items = myItems,
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
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add_new_recipe)
                )
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
    if (list.isEmpty()){
        EmptyDishCard()
        return
    }

    else{
        LazyColumn {
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



}

@Composable
fun EmptyDishCard(modifier: Modifier = Modifier){
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text =  stringResource(id = R.string.empty_card_message_1))
            Text(text =  stringResource(id = R.string.empty_card_message_2))
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
    loadDishComponent: (Recipe) -> Unit,
    deleteDishComponent: (UUID) -> Unit,
    onEdit: (DishComponent) -> Unit,
    modifier: Modifier = Modifier
){
    val isExpanded = remember { mutableStateOf(false) }
    val showDishComponents = remember { mutableStateOf(false) }
    val showCalTable = remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        elevation = 4.dp,
        backgroundColor = LightBlue200,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                showDishComponents.value = !showDishComponents.value
                showCalTable.value = !showCalTable.value
                loadDishComponent(recipe)
            }
    ) {
        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        ) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {

                    Image(
                        painter = painterResource(id = R.drawable.pasta1),
                        contentDescription = stringResource(id = R.string.recipe_image),
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
                        Text(
                            text = recipe.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.Black
                        )

                        Text(text = context.getString(R.string.portions_number,recipe.portions))
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {

                    IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.edit_recipe)
                        )
                    }

                    IconButton(onClick = { onDelete(recipe) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_recipe)
                        )
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


    Card(backgroundColor = LightBlue200, modifier = modifier
        .fillMaxWidth()
        .height(height.dp)) {

        LazyColumn{
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
                        .drawBehind { drawRoundRect(color = Color.DarkGray, style = stroke) }
                        .clickable { showDialog.value = !showDialog.value },
                ) {
                    Text(text = stringResource(id = R.string.add_new_ingredient), textAlign = TextAlign.Center)
                }

            }

            items(dishComponentList.size){
                DishComponentCard(component = dishComponentList[it], onEdit = onEdit, onDelete = deleteDishComponent,)
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
                placeholder = {Text(text = stringResource(id = R.string.recipe_hint))},
                label = {
                    Text(
                        text = stringResource(id = R.string.recipe_title),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                modifier = Modifier.padding(4.dp),
            )
        }

        //Portions
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            Row(modifier = Modifier.weight(1f)) {

                OutlinedButton(onClick = { if (portionsField != 0) portionsField-- },modifier = Modifier.size(40.dp)) {
                    Text(text = stringResource(id = R.string.minus))
                }

                Text(text = "$portionsField", modifier = Modifier.padding(8.dp) )

                OutlinedButton(onClick = { portionsField++ }, modifier = Modifier.size(40.dp)) {
                    Text(text = stringResource(id = R.string.plus))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = {
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
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(id = R.string.button_confirm)
                    )
                }

                IconButton(onClick = { isExpanded.value = false }) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = stringResource(id = R.string.button_cancel)
                    )
                }
            }
        }

        Row {
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
}

@Composable
fun RecipeCardCalTable(
    carbs: Float,
    fat: Float,
    protein: Float,
    cal: Float,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current

    Card(elevation = 4.dp, modifier = modifier
        .fillMaxWidth()
        .height(50.dp)
        .padding(8.dp)) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            //Carbohydrates
            Text(text = context.getString(R.string.carb_table,carbs.toInt()), color = Carb200)
            // Fat
            Text(text = context.getString(R.string.fat_table,fat.toInt()), color = Fat200)
            // Protein
            Text(text = context.getString(R.string.protein_table,protein.toInt()), color = Protein200)
            //Calories
            Text(text = context.getString(R.string.cal_table,cal.toInt()), color = Cal200)

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

    Card(elevation = 4.dp, modifier = modifier
        .padding(8.dp)
        .fillMaxWidth()) {
        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(4.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.circle),
                    contentDescription = stringResource(id = R.string.ingredient_image),
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
                            text = context.getString(R.string.currency,component.total.toInt()),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = modifier
                                .weight(4f)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = modifier.weight(1f))

                Column(modifier = Modifier.weight(4f)
                ) {

                    Row {

                        IconButton(onClick = { isExpanded.value = !isExpanded.value }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(id = R.string.edit_current_list))
                        }

                        IconButton(onClick = { onDelete(component.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_current_list))
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
                    DishComponentEditScreen(dishComponent = component, isExpanded = isExpanded, onSubmit = onEdit)
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
                placeholder = {Text(text = stringResource(id = R.string.new_dish_component_hint))},
                label = {
                    Text(
                        text = stringResource(id = R.string.new_dish_component_title),
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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

@Composable
fun NewRecipeDialog(
    setShowDialog: (Boolean) -> Unit,
    currentListId: UUID,
    onConfirm: (Recipe) -> Unit,
    modifier: Modifier = Modifier,
){
    var nameField by remember{ mutableStateOf(TextFieldValue("")) }
    var portionsField by remember { mutableStateOf(TextFieldValue("1")) }
    var errorFieldStatus by remember { mutableStateOf(false) }


    Dialog(onDismissRequest = {setShowDialog(false)}) {

        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {

            LazyColumn(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ){
                //Header
                item{
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.new_recipe),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                //Name of recipe
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(id = R.string.recipe_title),
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = nameField,
                            onValueChange = { nameField = it},
                            placeholder = { Text(text = stringResource(id = R.string.recipe_hint)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                autoCorrect = true,
                                imeAction = ImeAction.Next,
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(2f)
                        )
                    }
                }

                //Plug for space
                item { Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)) }

                //Portions
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ){
                        Text(
                            text = stringResource(id = R.string.portions_title),
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = portionsField,
                            onValueChange = { portionsField = it},
                            placeholder = { Text(text = stringResource(id = R.string.portions_hint)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            modifier = Modifier.weight(2f)
                        )
                    }
                }

                //Error message
                item{
                    if (errorFieldStatus){
                        Text(text = stringResource(id = R.string.error_message), color = Color.Red)
                    }
                    else{
                        Spacer(modifier = Modifier.height(35.dp))
                    }
                }

                //Buttons
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {

                        Button(onClick = { setShowDialog(false) }, modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(id = R.string.button_cancel))
                        }
                        
                        Spacer(modifier = Modifier.weight(0.5f))

                        Button(
                            onClick = {
                                //Check if all fields are not null
                                if (nameField.text.isBlank() || portionsField.text.isBlank()){
                                    errorFieldStatus = true
                                }
                                else{
                                    val newRecipe = Recipe(
                                        id = UUID.randomUUID(),
                                        listId = currentListId,
                                        name = nameField.text,
                                        portions = portionsField.text.toFloat().toInt(),
                                    )
                                    onConfirm(newRecipe)
                                    setShowDialog(false)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = stringResource(id = R.string.button_confirm))
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
    val options = listOf(
        stringResource(id = R.string.kgs),
        stringResource(id = R.string.lbs),
        stringResource(id = R.string.pcs),
        stringResource(id = R.string.pkg)
    )
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


            LazyColumn(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = modifier.padding(8.dp)
            ) {

                //Header of dialog
                item{
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.new_dish_component), color = Color.Black, fontSize = 28.sp)
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
                            value = name,
                            onValueChange = {name = it},
                            placeholder = {Text(text = stringResource(id = R.string.name_hint))},
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
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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

                //Switch
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.additional_info),
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = switchState,
                            onCheckedChange = {switchState = it},
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                //Additional section
                item{
                    if(switchState){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(4.dp)
                        ){
                            Text(
                                text = stringResource(id = R.string.carbs_title),
                                fontSize = 16.sp,
                                color = if(switchState) Color.Black else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = carbs,
                                onValueChange = {carbs = it},
                                placeholder = {Text(text = stringResource(id = R.string.carbs_hint))},
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
                                text = stringResource(id = R.string.fats_title),
                                fontSize = 16.sp,
                                color = if(switchState) Color.Black else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = fats,
                                onValueChange = {fats = it},
                                placeholder = {Text(text = stringResource(id = R.string.fats_hint))},
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
                                text = stringResource(id = R.string.protein_title),
                                fontSize = 16.sp,
                                color = if(switchState) Color.Black else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = protein,
                                onValueChange = {protein = it},
                                placeholder = {Text(text = stringResource(id = R.string.protein_hint))},
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
                                text = stringResource(id = R.string.cal_title),
                                fontSize = 16.sp,
                                color = if(switchState) Color.Black else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = cal,
                                onValueChange = {cal = it},
                                placeholder = {Text(text = stringResource(id = R.string.cal_hint))},
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                enabled = switchState,
                                modifier = Modifier.weight(2f),
                            )
                        }
                    }
                }

                // Error message
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        if (error){
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

                //Buttons
                item{
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ){

                        Button(onClick = { setShowDialog(false)}, modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),) {
                            Text(text = stringResource(id = R.string.button_cancel))
                        }

                        Button(
                            onClick = {

                                //Check if all primary fields are not empty
                                if (checkForError(name, weight, price)){
                                    error = true
                                } else{
                                    //Check if additional section is enabled
                                    if(switchState){
                                        //Switch is on, check all fields on negative numbers and blank spaces
                                        if(checkSwitchForError(carbs, fats, protein, cal)){
                                            //If true, some fields are empty or negative value
                                            error = true
                                        } else{
                                            //is OK, can continue
                                            val newDishComponent = DishComponent(
                                                id = UUID.randomUUID(),
                                                recipeId = recipeId,
                                                name = name.text,
                                                weight = weight.text.toFloat(),
                                                weightType = selectedOptionText,
                                                price = price.text.toFloat(),
                                                total = weight.text.toFloat() * price.text.toFloat(),
                                                carbs = carbs.text.toFloat(),
                                                fat = fats.text.toFloat(),
                                                protein = protein.text.toFloat(),
                                                cal = cal.text.toFloat()
                                            )

                                            //Submit new DishComponent and close dialog
                                            setShowDialog(false)
                                            onConfirm(newDishComponent)
                                        }
                                    } else{
                                        //Additional section is disabled
                                        val newDishComponent = DishComponent(
                                            id = UUID.randomUUID(),
                                            recipeId = recipeId,
                                            name = name.text,
                                            weight = weight.text.toFloat(),
                                            weightType = selectedOptionText,
                                            price = price.text.toFloat(),
                                            total = weight.text.toFloat() * price.text.toFloat(),
                                        )

                                        //Submit new DishComponent and close dialog
                                        setShowDialog(false)
                                        onConfirm(newDishComponent)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            ) { Text(text = stringResource(id = R.string.button_confirm)) }
                    }
                }
            }
        }
    }
}

fun checkForError(name: TextFieldValue, weight: TextFieldValue, price: TextFieldValue, ): Boolean {
    return name.text.isBlank() || weight.text.isBlank() || (price.text.isBlank() || price.text.toFloat() <= 0.0f)
}

fun checkSwitchForError(carbs: TextFieldValue, fats: TextFieldValue, protein: TextFieldValue, cal: TextFieldValue):Boolean{
    return (
            (carbs.text.isBlank() || carbs.text.toFloat() < 0.0f) ||
            (fats.text.isBlank() || fats.text.toFloat() < 0.0f) ||
            (protein.text.isBlank() || protein.text.toFloat() < 0.0f) ||
            (cal.text.isBlank() || cal.text.toFloat() < 0.0f)
            )
}