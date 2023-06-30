package com.example.smartlist.ui.menu

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.smartlist.R
import com.example.smartlist.data.VoiceToTextParser
import com.example.smartlist.data.VoiceToTextParserState
import com.example.smartlist.ui.screens.HomeViewModel
import com.example.smartlist.ui.screens.NewRecipeDialog

@Composable
fun MainAppBar(
    name: String,
    menuState:MutableState<Boolean>,
    onNavigationIconClick:()->Unit,
    retryAction: () -> Unit,
    onExport: (String) -> Unit,
){
    val context = LocalContext.current
    val title = stringResource(id = R.string.app_name)

    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value){
        ExportListDialog(
            title = name,
            setShowDialog = {showDialog.value = it},
            onConfirm = {exportName->
                onExport(exportName)
                Toast.makeText(context,"Data exported successfully", Toast.LENGTH_SHORT).show()
            },
        )
    }

    TopAppBar(
        title = { Text(text = "$title > $name") },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle drawer")
            }
        },
        actions = {

            IconButton(onClick = retryAction) {
                Icon(Icons.Default.Refresh, "Refresh")
            }

            IconButton( onClick = { menuState.value = !menuState.value } ) {
                Icon(Icons.Default.MoreVert, "Menu" )
            }

            DropdownMenu( expanded = menuState.value, onDismissRequest = { menuState.value = false} ) {
                DropdownMenuItem(
                    onClick = {
                        menuState.value = true
                        showDialog.value = true
                    } ) {
                    Text(text = "Export ingredients")
                }
            }
        }
    )
}

@Composable
fun DishAppBar(
    onNavigationIconClick:()->Unit,
    retryAction: () -> Unit,
    onMicrophoneOn: () -> Unit = {},

) {

    var canRecord by remember { mutableStateOf(false) }

    // Creates an permission request
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            canRecord = isGranted
        }
    )

    LaunchedEffect(key1 = recordAudioLauncher) {
        // Launches the permission request
        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }



    val context = LocalContext.current



    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        navigationIcon = {
                         IconButton(onClick = onNavigationIconClick) {
                             Icon(
                                 imageVector = Icons.Default.Menu,
                                 contentDescription = "Toggle drawer")
                         }
        },
        actions = {
            IconButton(onClick = retryAction) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
            IconButton(onClick = {
                if (canRecord){

                }
            }) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "mic is on"
                )
            }
        }
    )
}

@Composable
fun HomeAppBar(
    state:VoiceToTextParserState,
    onNavigationIconClick:()->Unit,
    retryAction: () -> Unit,
    onMicrophoneOn: (Boolean) -> Unit = {},

    ) {

    var canRecord by remember { mutableStateOf(false) }

    // Creates an permission request
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            canRecord = isGranted
        }
    )

    LaunchedEffect(key1 = recordAudioLauncher) {
        // Launches the permission request
        recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }



    val context = LocalContext.current



    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle drawer")
            }
        },
        actions = {
            IconButton(onClick = retryAction) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
            IconButton(
                onClick = {
                    if(canRecord){
                        if(!state.isSpeaking){
                            onMicrophoneOn(true)
                        }
                        else{
                            onMicrophoneOn(false)
                        }
                    }
                    //canRecord = !canRecord
                    //onMicrophoneOn(canRecord)
                }
            ) {
                if (!state.isSpeaking){
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "mic is on"
                    )
                }
                else{
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "mic is off"
                    )
                }
            }
        }
    )
}

@Composable
fun AppBarItem(
    name: String,
    onNavigationIconClick: () -> Unit,
    retryAction: () -> Unit,
) {
    val context = LocalContext.current
    val title = stringResource(id = R.string.app_name)
    TopAppBar(
        title = { Text(text = "$title > $name") },
        navigationIcon = {
            IconButton(onClick = {
                onNavigationIconClick()
                Toast.makeText(context,"Touched",Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle drawer")
            }
        },
        actions = {
            IconButton(onClick = retryAction) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }
    )
}


@Composable
fun ExportListDialog(
    title: String,
    setShowDialog: (Boolean) -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier,
){
    var fieldValue by remember{ mutableStateOf(TextFieldValue(title)) }
    var errorFieldStatus by remember { mutableStateOf(false) }


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
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = {fieldValue = it},
                    placeholder = {Text(text = "ex Lunch")},
                    label = {
                        Text(
                            text = "Enter new list name: ",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )

                if (errorFieldStatus){
                    Text(
                        text = "*Sure that you fill all fields, if message still remains, check symbols",
                        color = Color.Red,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .height(40.dp)
                    )
                } else{
                    Spacer(modifier = Modifier.height(40.dp))
                }

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

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        onClick = {

                            //Check if all fields are not null
                            if (fieldValue.text.isBlank()){
                                errorFieldStatus = true
                            }
                            else{
                                onConfirm(fieldValue.text)
                                setShowDialog(false)
                            }
                        }
                    ) { Text(text = "Confirm") }
                }
            }
        }
    }
}