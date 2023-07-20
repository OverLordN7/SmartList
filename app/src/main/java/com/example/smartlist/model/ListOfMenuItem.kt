package com.example.smartlist.model

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import com.example.smartlist.R

class ListOfMenuItem(val context: Context) {
    private val items = listOf(
        MenuItem(
            id = "home",
            title =context.getString(R.string.home_title_item),
            contentDescription = context.getString(R.string.home_title_item_hint),
            icon = Icons.Default.Home
        ),
        MenuItem(
            id = "purchaseList",
            title =context.getString(R.string.purchase_list_title_item),
            contentDescription = context.getString(R.string.purchase_list_title_item_hint),
            icon = Icons.Default.ShoppingBasket
        ),
        MenuItem(
            id = "dishList",
            title = context.getString(R.string.dishes_list_title_item),
            contentDescription = context.getString(R.string.dishes_list_title_item_hint),
            icon = Icons.Default.Fastfood
        ),
        MenuItem(
            id = "graphs",
            title = context.getString(R.string.graphs_title_item),
            contentDescription = context.getString(R.string.graphs_title_item_hint),
            icon = Icons.Default.BarChart
        ),
        MenuItem(
            id = "settings",
            title = context.getString(R.string.settings),
            contentDescription = context.getString(R.string.go_to_settings_screen),
            icon = Icons.Default.Settings
        )
    )

    fun getItems(): List<MenuItem>{
        return items
    }
}



val items = listOf(
    MenuItem(
        id = "home",
        title ="Home",
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