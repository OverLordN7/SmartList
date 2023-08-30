package com.example.smartlist.extend_functions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateUniqueFileName(): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "IMG_$timeStamp.jpg"
}
