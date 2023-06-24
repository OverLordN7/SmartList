package com.example.smartlist.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartlist.R
import com.example.smartlist.model.MenuItem
import com.example.smartlist.ui.theme.LightBlue500
import com.example.smartlist.ui.theme.Orange200


@Composable
fun DrawerHeader(modifier: Modifier = Modifier){
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(LightBlue500)
            .height(200.dp),
    ){
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 50.sp,
            color = Color.White
        )
    }
}

@Composable
fun DrawerBody(
    items: List<MenuItem>,
    onItemClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier,
    itemTextStyle: TextStyle = TextStyle(fontSize = 18.sp)
){
    LazyColumn(modifier){
        items(items.size){item->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(items[item]) }
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = items[item].icon,
                    contentDescription = items[item].contentDescription
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = items[item].title,
                    style = itemTextStyle,
                    modifier = modifier.weight(1f)
                )
            }
        }
    }
}