package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.Game
import com.saj.android.scoreboardtracker.model.User
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.components.*
import com.saj.android.scoreboardtracker.ui.theme.backgroundGradient
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun History(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    ScoreboardSurface(modifier = modifier.fillMaxSize()) {
        GameList(viewModel = viewModel, modifier = modifier.fillMaxSize())
    }
}

@ExperimentalCoroutinesApi
@Composable
fun GameList(viewModel: MainViewModel, modifier: Modifier) {
    val items: List<Game> by viewModel.completedGamesLiveData.observeAsState(listOf())
    LazyColumn(modifier.padding(16.dp, 32.dp, 16.dp, 0.dp)) {
        items(count = items.size, itemContent = { index ->
            GameScoreItem(items[index])
            ScoreboardDivider(thickness = 16.dp, color = Color.Transparent)
        })
    }
}

@Composable
private fun GameScoreItem(game: Game) {
    val focusManager = LocalFocusManager.current
    val gradientBackground = Brush.horizontalGradient(
        colors = backgroundGradient
    )
    ScoreboardCard(elevation = 4.dp, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBackground)
        ) {
            val image = loadPicture(
                url = game.userScores.firstOrNull { us -> us.user.userId == game.winnerId }?.user?.profileImageUrl
                    ?: "",
                defaultImage = R.drawable.common_full_open_on_phone
            ).value
            image?.let { img ->
                Image(
                    bitmap = img.asImageBitmap(),
                    "",
                    modifier = Modifier.size(150.dp, 200.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(modifier = Modifier.padding(16.dp).wrapContentSize()) {
                game.userScores.sortedBy { it.getTotalScore() }.forEach {
                    Text(
                        text = "${it.user.name} : ${it.getTotalScore()}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(0.dp, 16.dp)
                    )
                }
            }
        }
    }
}