package com.saj.android.scoreboardtracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.coil.rememberCoilPainter

@Composable
fun CoilImage(
    modifier: Modifier,
    data: String,
    contentScale: ContentScale,
    contentDescription: String = "",
    colorFilter: ColorFilter? = null
) {
    Image(
        modifier = modifier,
        painter = rememberCoilPainter(data),
        contentDescription = contentDescription,
        contentScale = contentScale,
        colorFilter = colorFilter
    )
}

