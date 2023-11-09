package com.example.smartlist.extend_functions

import android.content.Context
import android.net.Uri
import java.io.File

fun deleteImageByUri(context: Context, uri: Uri){
    val filePath = getFilePathFromUri(context, uri)
    if (filePath != null){
        val fileToDelete = File(filePath)
        if (fileToDelete.exists()){
            fileToDelete.delete()
        }
    }
}