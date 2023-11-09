package com.example.smartlist.extend_functions

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

fun getFilePathFromUri(context: Context, uri: Uri): String?{
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri,projection,null,null, null)
    val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor?.moveToFirst()
    val filepath = cursor?.getString(columnIndex ?: 0)
    cursor?.close()
    return filepath
}