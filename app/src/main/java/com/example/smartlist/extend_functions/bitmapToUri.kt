package com.example.smartlist.extend_functions

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
    val wrapper = ContextWrapper(context)

    val file = File(wrapper.cacheDir, "images")
    if (!file.exists()) {
        file.mkdirs()
    }

    val imageFile = File(file, "image.png")

    try {
        val stream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }

    val imageUri = Uri.fromFile(imageFile)
    return saveImageToInternalStorage(context, imageUri)
}
