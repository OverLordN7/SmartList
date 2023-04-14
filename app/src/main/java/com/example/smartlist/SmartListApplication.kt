package com.example.smartlist

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.smartlist.data.AppContainer
import com.example.smartlist.data.DefaultAppContainer

class SmartListApplication: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}