package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.saj.android.scoreboardtracker.R

@Composable
fun History(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.history_route),
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize()
    )
}