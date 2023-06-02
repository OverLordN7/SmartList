package com.example.smartlist.ui.menu

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.example.smartlist.R

@Composable
fun MainAppBar(
    menuState:MutableState<Boolean>,
    retryAction: () -> Unit,
    onExport: ()-> Unit,
){
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
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
                        //TODO add a call to export DC components to PurchaseList
                        menuState.value = false
                        onExport()
                    } ) {
                    Text(text = "Export ingredients")
                }
            }
        }
    )
}