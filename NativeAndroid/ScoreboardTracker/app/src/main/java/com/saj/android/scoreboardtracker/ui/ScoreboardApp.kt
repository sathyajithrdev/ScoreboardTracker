package com.saj.android.scoreboardtracker.ui

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.saj.android.scoreboardtracker.ui.screens.MainViewModel
import com.saj.android.scoreboardtracker.ui.screens.home.Home
import com.saj.android.scoreboardtracker.ui.theme.ScoreboardTheme
import com.saj.android.scoreboardtracker.ui.utils.Navigator

@Composable
fun ScoreboardApp(viewModel: MainViewModel, backDispatcher: OnBackPressedDispatcher) {
    val navigator: Navigator<Destination> = rememberSaveable(
        saver = Navigator.saver(backDispatcher)
    ) {
        Navigator(Destination.Home, backDispatcher)
    }
    val actions = remember(navigator) { Actions(navigator) }
    ScoreboardTheme {
        Crossfade(navigator.current) { destination ->
            when (destination) {
                Destination.Home -> Home(viewModel, actions.selectUser)
//                    is Destination.UserDetail -> SnackDetail(
//                        snackId = destination.userId,
//                        upPress = actions.upPress
//                    )
            }
        }
    }
}