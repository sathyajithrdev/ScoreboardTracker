package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.saj.android.scoreboardtracker.R

@Composable
fun AvatarPlaceHolderView() {
    Image(
        painterResource(id = R.drawable.ic_man),
        contentDescription = null
    )
}