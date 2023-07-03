package com.example.smartlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smartlist.ui.SmartListApp
import com.example.smartlist.ui.theme.SmartListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartListTheme() {
                SmartListApp()
            }
        }
    }
}