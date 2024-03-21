package com.example.smartlist.ui.menu

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartlist.R
import com.example.smartlist.navigation.Screen

@Composable
fun CustomBottomAppBar(
    navController: NavController,
    context: Context
){
    BottomAppBar(
        modifier = Modifier.background(Color.Transparent),
        cutoutShape = CircleShape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBasket,
                contentDescription = context.getString(R.string.purchase_list_title_item_hint),
                Modifier
                    .weight(1f)
                    .size(32.dp)
                    .clickable { navController.navigate(Screen.PurchasesScreen.route) }
            )
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = context.getString(R.string.dishes_list_title_item_hint),
                Modifier
                    .weight(1f)
                    .size(32.dp)
                    .clickable { navController.navigate(Screen.DishesScreen.route) }
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Cookie,
                contentDescription = context.getString(R.string.product_screen_hint),
                Modifier
                    .weight(1f)
                    .size(32.dp)
                    .clickable { navController.navigate(Screen.ProductScreen.route) }
            )
            Icon(imageVector = Icons.Default.Settings,
                contentDescription = context.getString(R.string.go_to_settings_screen),
                Modifier
                    .weight(1f)
                    .size(32.dp)
                    .clickable { navController.navigate(Screen.SettingScreen.route) }
            )
        }
    }
}