package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.ui.theme.Neutral7
import com.saj.android.scoreboardtracker.ui.theme.SemiTransparentBlack

@ExperimentalAnimationApi
@Composable
fun LoadingView(message: String, isLoading: Boolean) {
    val context = LocalContext.current
    val customView = remember { LottieAnimationView(context) }
    AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SemiTransparentBlack),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView({ customView }, modifier = Modifier.size(50.dp)) { view ->
                // View's been inflated - add logic here if necessary
                with(view) {
                    setAnimation(R.raw.card_loading)
                    playAnimation()
                    repeatCount = 10
                    repeatMode = LottieDrawable.RESTART
                }
            }
            Text(message, color = Color.White, modifier = Modifier.padding(2.dp))
        }
    }
}