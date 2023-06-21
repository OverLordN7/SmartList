package com.example.smartlist.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBasket

val items = listOf(
    MenuItem(
        id = "home",
        title = "Home",
        contentDescription = "Go to home screen",
        icon = Icons.Default.Home
    ),
    MenuItem(
        id = "purchaseList",
        title = "Purchase list",
        contentDescription = "Go to Purchase list screen",
        icon = Icons.Default.ShoppingBasket
    ),
    MenuItem(
        id = "dishList",
        title = "Dishes list",
        contentDescription = "Go to Dishes list screen",
        icon = Icons.Default.Fastfood
    ),
    MenuItem(
        id = "graphs",
        title = "Graphs",
        contentDescription = "Go to graphs screen",
        icon = Icons.Default.BarChart
    ),
)