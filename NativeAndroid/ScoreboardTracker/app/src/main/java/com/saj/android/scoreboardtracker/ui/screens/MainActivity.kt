package com.saj.android.scoreboardtracker.ui.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.ui.theme.ScoreboardTrackerTheme

sealed class BottomNavigationScreens(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    object Game :
        BottomNavigationScreens("game", R.string.game_route, Icons.Filled.Home)

    object Stats :
        BottomNavigationScreens("stats", R.string.stats_route, Icons.Filled.Star)

    object History :
        BottomNavigationScreens("history", R.string.history_route, Icons.Filled.History)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScoreboardTrackerTheme {
                // A surface container using the 'background' color from the theme
                HomeScreen()
            }
        }
    }
}


@Composable
fun HomeScreen() {

    val navController = rememberNavController()

    val bottomNavigationItems = listOf(
        BottomNavigationScreens.Game,
        BottomNavigationScreens.Stats,
        BottomNavigationScreens.History
    )
    Scaffold(
        bottomBar = {
            SpookyAppBottomNavigation(navController, bottomNavigationItems)
        },
    ) {
        MainScreenNavigationConfigurations(navController)
    }
}


@Composable
private fun MainScreenNavigationConfigurations(
    navController: NavHostController
) {
    NavHost(navController, startDestination = BottomNavigationScreens.Game.route) {
        composable(BottomNavigationScreens.Game.route) {
            ScaryScreen()
        }
        composable(BottomNavigationScreens.Stats.route) {
            ScaryScreen()
        }
        composable(BottomNavigationScreens.History.route) {
            ScaryScreen()
        }
    }
}

@Composable
private fun SpookyAppBottomNavigation(
    navController: NavHostController,
    items: List<BottomNavigationScreens>
) {
    BottomNavigation {
        val currentRoute = currentRoute(navController)
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, "") },
                label = { Text(stringResource(id = screen.resourceId)) },
                selected = currentRoute == screen.route,
                alwaysShowLabels = false, // This hides the title for the unselected items
                onClick = {
                    // This if check gives us a "singleTop" behavior where we do not create a
                    // second instance of the composable if we are already on that destination
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route)
                    }
                }
            )
        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.arguments?.getString(KEY_ROUTE)
}


@Composable
fun ScaryScreen() {
    // Adds view to Compose
    Text(text = "test")
}