package com.example.smartlist.extend_functions

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun saveImageToInternalStorage(context: Context, uri: Uri): Uri {
    val outputStream: OutputStream
    val uniqueFileName = generateUniqueFileName()

    val contentResolver = context.contentResolver
    val imageFile = File(context.filesDir, uniqueFileName)

    outputStream = FileOutputStream(imageFile)

    val inputStream = contentResolver.openInputStream(uri)
    inputStream?.copyTo(outputStream)

    outputStream.flush()
    outputStream.close()
    inputStream?.close()

    return Uri.fromFile(imageFile)
}