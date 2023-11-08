package com.example.smartlist.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.extend_functions.capitalizeFirstChar
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.model.Product
import com.example.smartlist.ui.common_composables.LoadingScreen
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import kotlinx.coroutines.launch
import java.util.UUID


private const val TAG = "ProductScreen"
@Composable
fun ProductScreen(
    homeViewModel: HomeViewModel,
    productViewModel: ProductViewModel,
    navController: NavController,
    onConfirm: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onUpdate: (Product) -> Unit,
    onRefresh: ()-> Unit,
    modifier: Modifier = Modifier
){
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val voiceState by homeViewModel.voiceToTextParser.state.collectAsState()

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    val voiceCommand by homeViewModel.voiceCommand.collectAsState()


    val state: ProductUIState = productViewModel.productUIState

    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    voiceCommand?.let { command->
        homeViewModel.processNavigationCommand(
            command = command,
            currentScreen = context.getString(R.string.product_screen),
            navController = navController,
            context = context
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                state = voiceState,
                onNavigationIconClick = { scope.launch { scaffoldState.drawerState.open() } },
                onMicrophoneOn = {
                    if(it){ homeViewModel.startListening() }

                    else { homeViewModel.stopListening() }
                },
                isRetryActionEnabled = true,
                retryAction = onRefresh,
                name = stringResource(R.string.products)
            )
        },
        drawerContent = {
            DrawerHeader()
            DrawerBody(
                items = myItems,
                onItemClick = {
                    scope.launch { scaffoldState.drawerState.close() }
                    homeViewModel.processDrawerBodyCommand(
                        item = it,
                        currentScreen = "products",
                        context = context,
                        navController = navController,
                    )
                }
            )
        },
    ) {
        Surface(modifier = modifier.padding(it)) {
            //Content

            when(state){
                is ProductUIState.Error -> {}
                is ProductUIState.Loading -> LoadingScreen()
                is ProductUIState.Success -> {
                    // List of database
                    TableOfProducts(
                        products = state.productList,
                        onConfirm = onConfirm,
                        onDelete = onDelete,
                        onUpdate = onUpdate,
                    )
                }
            }
        }

    }
}

@Composable
fun TableOfProducts(
    products: List<Product>,
    onConfirm: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    onUpdate: (Product) -> Unit,
    modifier: Modifier = Modifier
){

    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value){
        NewProductDialog(
            setShowDialog = {showDialog.value = it},
            onConfirm = onConfirm,
        )
    }

    Card(
        elevation = 4.dp,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        LazyColumn{
            //Header of table
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(4.dp)
                        .background(MaterialTheme.colors.primary),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text(text = "id", color = Color.White)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(text = "name", color = Color.White)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "carbs", color = Color.White)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "fats", color = Color.White)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "protein", color = Color.White)
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "cals", color = Color.White)
                    }
                }
            }

            //Content of table
            items(products.size){
                TableItem(
                    product = products[it],
                    id = it,
                    onDelete = onDelete,
                    onUpdate = onUpdate,
                )
            }

            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = { showDialog.value = true }) {
                        Text(text = "+")
                    }
                }
            }

        }
    }
}

@Composable
fun TableItem(
    product: Product,
    id: Int,
    onDelete: (Product) -> Unit,
    onUpdate: (Product) -> Unit,
){

    val showEditDialog = remember { mutableStateOf(false) }

    if (showEditDialog.value){
        EditProductDialog(
            product = product,
            setShowDialog = {showEditDialog.value = it},
            onUpdate = onUpdate,
        )
    }
    var isExpanded by remember {mutableStateOf(false)}

    Card {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { isExpanded = !isExpanded },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(0.5f)
                ) {
                    // increase id by 1 for user convenience
                    Text(text = (id + 1).toString())
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text(
                        text = product.name,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = product.carb.toString())
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = product.fat.toString())
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = product.protein.toString())
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = product.cal.toString())
                }
            }

            if (isExpanded){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { showEditDialog.value = true }
                    ) {
                        Text(text = stringResource(R.string.edit))
                    }

                    Spacer(modifier = Modifier.weight(0.5f))

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onDelete(product) }
                    ) {
                        Text(text = stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}


@Composable
fun NewProductDialog(
    setShowDialog: (Boolean) -> Unit,
    onConfirm: (Product) -> Unit,
    modifier: Modifier = Modifier
){
    var error by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(TextFieldValue("")) }
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
                        Text(text = stringResource(id = R.string.new_product_title), color = Color.Black, fontSize = 28.sp)
                    }
                }

                //Primary fields
                item{
                    //Name row
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

                    //Carbs row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.carbs_title),
                            fontSize = 16.sp,
                            color = Color.Black,
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
                            modifier = Modifier.weight(2f),
                        )
                    }

                    //Fats row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.fats_title),
                            fontSize = 16.sp,
                            color = Color.Black,
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
                            modifier = Modifier.weight(2f),
                        )
                    }

                    //Protein row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.protein_title),
                            fontSize = 16.sp,
                            color = Color.Black,
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
                            color = Color.Black,
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
                            modifier = Modifier.weight(2f),
                        )
                    }
                }

                //Error message
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
                                if (name.text.isEmpty() || checkSwitchForError(carbs, fats, protein, cal)){
                                    error = true
                                } else{
                                    val test = name.text.capitalizeFirstChar().replace("\\s+$".toRegex(), "")
                                    Log.d(TAG, "*$test*")

                                    val newProduct = Product(
                                        id = UUID.randomUUID(),
                                        name = name.text.capitalizeFirstChar().replace("\\s+$".toRegex(), ""),
                                        carb = carbs.text.toFloat(),
                                        fat = fats.text.toFloat(),
                                        protein = protein.text.toFloat(),
                                        cal = cal.text.toFloat(),
                                    )

                                    setShowDialog(false)
                                    onConfirm(newProduct)
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

@Composable
fun EditProductDialog(
    product: Product,
    setShowDialog: (Boolean) -> Unit,
    onUpdate: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var error by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(TextFieldValue(product.name)) }
    var carbs by remember{ mutableStateOf(TextFieldValue(product.carb.toString())) }
    var fats by remember { mutableStateOf(TextFieldValue(product.fat.toString())) }
    var protein by remember { mutableStateOf(TextFieldValue(product.protein.toString())) }
    var cal by remember { mutableStateOf(TextFieldValue(product.cal.toString())) }

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
                        Text(text = stringResource(id = R.string.new_product_title), color = Color.Black, fontSize = 28.sp)
                    }
                }

                //Primary fields
                item{
                    //Name row
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

                    //Carbs row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.carbs_title),
                            fontSize = 16.sp,
                            color = Color.Black,
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
                            modifier = Modifier.weight(2f),
                        )
                    }

                    //Fats row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.fats_title),
                            fontSize = 16.sp,
                            color = Color.Black,
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
                            modifier = Modifier.weight(2f),
                        )
                    }

                    //Protein row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(
                            text = stringResource(id = R.string.protein_title),
                            fontSize = 16.sp,
                            color = Color.Black,
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
                            color = Color.Black,
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
                            modifier = Modifier.weight(2f),
                        )
                    }
                }

                //Error message
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
                                if (name.text.isEmpty() || checkSwitchForError(carbs, fats, protein, cal)){
                                    error = true
                                } else{
                                    val newProduct = Product(
                                        id = product.id,
                                        name = name.text.capitalizeFirstChar(),
                                        carb = carbs.text.toFloat(),
                                        fat = fats.text.toFloat(),
                                        protein = protein.text.toFloat(),
                                        cal = cal.text.toFloat(),
                                    )

                                    setShowDialog(false)
                                    onUpdate(newProduct)
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