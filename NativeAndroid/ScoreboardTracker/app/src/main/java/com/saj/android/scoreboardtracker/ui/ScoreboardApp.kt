package com.saj.android.scoreboardtracker.ui

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.*
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.ui.screens.home.Game
import com.saj.android.scoreboardtracker.ui.screens.home.History
import com.saj.android.scoreboardtracker.ui.screens.home.Statistics
import com.saj.android.scoreboardtracker.ui.theme.ScoreboardTheme

@ExperimentalMaterialApi
@Composable
fun ScoreboardApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Stats,
        Screen.History
    )
    ScoreboardTheme {

        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
                    items.forEach { screen ->

                        val selected = screen.route == currentRoute

                        val colorState by animateColorAsState(
                            if (selected) {
                                ScoreboardTheme.colors.iconInteractive
                            } else {
                                ScoreboardTheme.colors.iconInteractiveInactive
                            }
                        )

                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = null,
                                    tint = colorState
                                )
                            },
                            modifier = Modifier.background(Color.Black),
                            alwaysShowLabel = true,
                            label = {
                                Text(
                                    stringResource(screen.resourceId),
                                    color = colorState
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo = navController.graph.startDestination
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            },
            backgroundColor = Color.Transparent
        ) {
            NavHost(navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) { Game(viewModel) }
                composable(Screen.Stats.route) { Statistics(viewModel) }
                composable(Screen.History.route) { History(viewModel) }
            }
        }
    }
}

sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.game_route, Icons.Outlined.Home)
    object Stats : Screen("stats", R.string.stats_route, Icons.Outlined.Stars)
    object History : Screen("history", R.string.history_route, Icons.Outlined.History)
}