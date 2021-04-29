package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.saj.android.scoreboardtracker.ui.theme.Neutral7
import com.saj.android.scoreboardtracker.ui.theme.SemiTransparentBlack

@ExperimentalAnimationApi
@Composable
fun LoadingView(message: String, isLoading: Boolean) {
    AnimatedVisibility(visible = isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SemiTransparentBlack),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Neutral7)
            Text(message, color = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}