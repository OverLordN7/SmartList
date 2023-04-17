package com.example.smartlist.ui.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DetailedPurchaseListScreen(
    listId: String,
    modifier: Modifier = Modifier
){
    Text(text = "Inside list $listId")
}