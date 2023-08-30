package com.example.smartlist.extend_functions

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

fun ContentResolver.getFilePath(uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = query(uri, projection, null, null, null)
    cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        it.moveToFirst()
        return it.getString(columnIndex)
    }
    return null
}