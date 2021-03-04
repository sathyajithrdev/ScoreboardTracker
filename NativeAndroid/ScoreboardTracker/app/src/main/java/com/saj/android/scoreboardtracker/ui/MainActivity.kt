package com.saj.android.scoreboardtracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.saj.android.scoreboardtracker.ui.screens.MainViewModel
import com.saj.android.scoreboardtracker.ui.utils.LocalSysUiController
import com.saj.android.scoreboardtracker.ui.utils.SystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val systemUiController = remember { SystemUiController(window) }
            CompositionLocalProvider(LocalSysUiController provides systemUiController) {
                ScoreboardApp(viewModel, onBackPressedDispatcher)
            }
        }
    }
}
