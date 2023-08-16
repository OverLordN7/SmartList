package com.example.smartlist.extend_functions

import java.util.Locale


fun String.capitalizeFirstChar(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}