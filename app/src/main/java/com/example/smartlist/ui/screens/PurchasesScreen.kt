package com.example.smartlist.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.smartlist.R

@Composable
fun PurchasesScreen(modifier: Modifier = Modifier){
    Scaffold() {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Text(text = stringResource(id = R.string.purchases_screen_button))
        }
    }
}