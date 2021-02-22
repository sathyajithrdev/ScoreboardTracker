package com.saj.android.scoreboardtracker.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.User
import com.saj.android.scoreboardtracker.ui.controls.loadPicture
import com.saj.android.scoreboardtracker.ui.theme.ScoreboardTrackerTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getUsersList()
        setContent {
            ScoreboardTrackerTheme {
                // A surface container using the 'background' color from the theme
                HomeScreen(viewModel)
            }
        }
    }
}


@Composable
fun HomeScreen(viewModel: MainViewModel) {

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
        MainScreenNavigationConfigurations(viewModel, navController)
    }
}


@Composable
private fun MainScreenNavigationConfigurations(
    viewModel: MainViewModel,
    navController: NavHostController
) {
    NavHost(navController, startDestination = BottomNavigationScreens.Game.route) {
        composable(BottomNavigationScreens.Game.route) {
            GameScreen(viewModel)
        }
        composable(BottomNavigationScreens.Stats.route) {
            GameScreen(viewModel)
        }
        composable(BottomNavigationScreens.History.route) {
            GameScreen(viewModel)
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
fun GameScreen(viewModel: MainViewModel) {
    UsersList(viewModel)
}

@ExperimentalCoroutinesApi
@Composable
fun UsersList(viewModel: MainViewModel) {
    val items: List<User> by viewModel.userLiveData.observeAsState(listOf())
    LazyColumn(modifier = Modifier.fillMaxHeight().padding(0.dp, 0.dp, 0.dp, 58.dp)) {
        items(count = items.size, itemContent = { index ->
            val user = items[index]
            Log.d("COMPOSE", "This get rendered $user")
            Column {
                Text(text = user.name, style = TextStyle(fontSize = 80.sp))
                val image = loadPicture(
                    url = user.profileImageUrl,
                    defaultImage = R.drawable.common_full_open_on_phone
                ).value
                image?.let { img ->
                    Image(
                        bitmap = img.asImageBitmap(),
                        "",
                        modifier = Modifier.preferredSize(150.dp, 200.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        })
    }
}
