package com.saj.android.scoreboardtracker.extensions

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.ui.unit.TextUnit
import java.text.SimpleDateFormat
import java.util.*

fun Date.toUIFormat(): String {
    val pattern = "E dd-MM-yyyy HH:mm"
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return simpleDateFormat.format(this)
}

fun TextUnit.toPx(): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    this.value,
    Resources.getSystem().displayMetrics
)