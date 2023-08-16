package com.example.smartlist.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.model.ListOfMenuItem
import com.example.smartlist.ui.menu.DrawerBody
import com.example.smartlist.ui.menu.DrawerHeader
import com.example.smartlist.ui.menu.HomeAppBar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
){
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val state by homeViewModel.voiceToTextParser.state.collectAsState()

    //Menu drawer items
    val myItems = ListOfMenuItem(context).getItems()

    //Language attributes
    val currentLanguage by homeViewModel.currentLanguage.collectAsState()

    val voiceCommand by homeViewModel.voiceCommand.collectAsState()

    val isDarkTheme by homeViewModel.isDarkThemeEnabled.collectAsState()

    val toggleTheme: (Boolean) -> Unit = {homeViewModel.setDarkThemeEnabled(it)}


    LaunchedEffect(navController.currentBackStackEntry){
        homeViewModel.clearVoiceCommand()
    }

    voiceCommand?.let { command->
        homeViewModel.processNavigationCommand(
            command = command,
            currentScreen = context.getString(R.string.settings_screen),
            navController = navController,
            context = context
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                name = stringResource(id = R.string.settings),
                state = state,
                onNavigationIconClick = { scope.launch { scaffoldState.drawerState.open() } },
                onMicrophoneOn = {
                    if(it){ homeViewModel.startListening() }

                    else { homeViewModel.stopListening() }
                }
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
                        currentScreen = "settings",
                        context = context,
                        navController = navController,
                    )
                }
            )
        },
    ) {
        Surface(modifier = modifier.padding(it)) {

            Column {
                DarkModeCard(isDarkTheme = isDarkTheme, toggleTheme = toggleTheme)
                VoiceCommandListCard(currentLanguage)
                LanguageSelectionCard(
                    homeViewModel = homeViewModel,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

@Composable
fun DarkModeCard(isDarkTheme: Boolean, toggleTheme: (Boolean)->Unit, modifier: Modifier = Modifier){
    Card(
        elevation = 4.dp,
        modifier = modifier.padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = stringResource(id = R.string.appearance),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.dark_theme_mode),
                    modifier = Modifier.weight(2f)
                )

                Spacer(modifier = Modifier.weight(2f))

                Switch(
                    checked = isDarkTheme,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = {
                        toggleTheme(!isDarkTheme)
                    }
                )

            }

        }

    }
}

@Composable
fun VoiceCommandListCard(currentLanguage: String,modifier: Modifier = Modifier){

    val isExpanded = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val textId = "inlineContent"
    var text = buildAnnotatedString {
        append(stringResource(id = R.string.voice_commands))
        //Append a placeholder string "[icon]" and attach an annotation "inlineContent" on it.
        appendInlineContent(textId,"[icon]")
    }

    LaunchedEffect(currentLanguage){
        val updateText = buildAnnotatedString {
            append(context.getString(R.string.voice_commands))
            appendInlineContent(textId,"[icon]")
        }
        text = updateText
    }


    val inlineContent = mapOf(
        Pair(
            textId,
            InlineTextContent(
                Placeholder(
                   width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                )
            ){
                IconButton(onClick = {
                    Toast.makeText(context,
                        context.getString(R.string.voice_command_hint),
                        Toast.LENGTH_SHORT)
                        .show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "",
                        tint = Color.Gray
                    )
                }
            }
        )
    )

    Card(
        elevation = 4.dp,
        modifier = modifier.padding(8.dp)
    ) {
        LazyColumn(modifier = Modifier
            .padding(8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        text = text,
                        inlineContent = inlineContent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = modifier.weight(4f)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(onClick = { isExpanded.value = !isExpanded.value}) {
                        Icon(
                            imageVector = if (isExpanded.value )Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = ""
                        )
                    }
                }
            }
            item {
                if (isExpanded.value){
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text = stringResource(R.string.command_hint),
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                        )
                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            text = stringResource(R.string.navigation),
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                        )
                        Text(
                            text = stringResource(R.string.break_line),
                            fontSize = 16.sp,
                        )
                        Text(text = " - "+stringResource(id = R.string.home_screen))
                        Text(text = " - "+stringResource(id = R.string.purchase_screen))
                        Text(text = " - "+stringResource(id = R.string.dish_screen))
                        Text(text = " - "+stringResource(id = R.string.graph_screen))
                        Text(text = " - "+stringResource(id = R.string.settings_screen))

                        Spacer(modifier = Modifier.height(15.dp))

                        Text(
                            text = stringResource(R.string.creation),
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                        )
                        Text(
                            text = stringResource(R.string.break_line),
                            fontSize = 16.sp,
                        )

                        Text(
                            text = " - "
                                    + stringResource(id = R.string.create_command)
                                    + " "
                                    + stringResource(id = R.string.create_command_parameter_new)
                                    + " "
                                    + stringResource(id = R.string.create_command_list) 
                                    + " "
                                    + stringResource(id = R.string.create_command_value)
                        )
                        Row(horizontalArrangement = Arrangement.Start){

                            Text(text = stringResource(R.string.command_in))

                            Text(text = stringResource(id = R.string.purchase_screen)
                                    + ","
                                    + stringResource(id = R.string.dish_screen),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = " - "
                                    + stringResource(id = R.string.create_command)
                                    + " "
                                    + stringResource(id = R.string.create_command_parameter_new)
                                    + " "
                                    + stringResource(id = R.string.create_command_recipe)
                                    + " "
                                    + stringResource(id = R.string.create_command_value) 
                                    + " " 
                                    + stringResource(id = R.string.create_command_portions_tag) 
                                    + " " 
                                    + stringResource(id = R.string.create_command_portions_value)
                        )
                        Row(horizontalArrangement = Arrangement.Start){

                            Text(text = stringResource(R.string.command_in))

                            Text(
                                text = ""
                                        + stringResource(id = R.string.inside)
                                        + " "
                                        + stringResource(id = R.string.dish_screen_alt),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        //create a new purchase item
                        Text(
                            text = " - "
                                    + stringResource(id = R.string.create_command)
                                    + " "
                                    + stringResource(id = R.string.create_command_parameter_new)
                                    + " "
                                    + stringResource(id = R.string.create_command_object)
                                    + " "
                                    + stringResource(id = R.string.create_command_object_name)
                                    + " "
                                    + stringResource(id = R.string.create_command_object_weight)
                                    + " "
                                    + stringResource(id = R.string.create_command_portions_value)
                                    + " "
                                    + stringResource(id = R.string.create_command_object_unit)
                                    + " "
                                    + stringResource(id = R.string.create_command_portions_value)
                                    + " "
                                    + stringResource(id = R.string.create_command_object_price)
                                    + " "
                                    + stringResource(id = R.string.create_command_portions_value)
                        )
                        Row(horizontalArrangement = Arrangement.Start){

                            Text(text = stringResource(R.string.command_in))

                            Text(
                                text = ""
                                        + stringResource(id = R.string.inside)
                                        + " "
                                        + stringResource(id = R.string.purchase_screen_alt),
                                fontWeight = FontWeight.Bold
                            )
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LanguageSelectionCard(
    homeViewModel: HomeViewModel,
    currentLanguage: String,
    modifier: Modifier = Modifier
){
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    //Available languages
    val languages = listOf("en","ru")

    Card(
        elevation = 4.dp,
        modifier = modifier.padding(8.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = stringResource(id = R.string.languages),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.current_language),
                    modifier = Modifier.weight(2f)
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownMenuExpanded,
                    onExpandedChange = {dropdownMenuExpanded = !dropdownMenuExpanded},
                    modifier = Modifier.weight(2f)
                ) {
                    OutlinedTextField(
                        value = currentLanguage,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownMenuExpanded)},
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownMenuExpanded,
                        onDismissRequest = { dropdownMenuExpanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                onClick = {
                                    homeViewModel.setCurrentLanguage(language)
                                    dropdownMenuExpanded = false
                                }) {
                                Text(text = language)
                            }
                        }
                    }
                }
            }
        }
    }
}