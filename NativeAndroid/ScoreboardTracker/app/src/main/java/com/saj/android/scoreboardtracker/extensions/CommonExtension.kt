package com.saj.android.scoreboardtracker.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.toUIFormat(): String {
    val pattern = "E dd-MM-yyyy HH:mm"
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}