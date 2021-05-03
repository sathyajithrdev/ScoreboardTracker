package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saj.android.scoreboardtracker.extensions.toUIFormat
import com.saj.android.scoreboardtracker.model.Game
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.components.CoilImage
import com.saj.android.scoreboardtracker.ui.components.ScoreboardCard
import com.saj.android.scoreboardtracker.ui.components.ScoreboardDivider
import com.saj.android.scoreboardtracker.ui.theme.TransparentBlack
import com.saj.android.scoreboardtracker.ui.theme.backgroundGradient
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun History(viewModel: MainViewModel) {
    GameList(viewModel = viewModel)
}

@ExperimentalCoroutinesApi
@Composable
fun GameList(viewModel: MainViewModel) {
    val items: List<Game> by viewModel.completedGamesLiveData.observeAsState(listOf())
    LazyColumn(
        Modifier
            .background(TransparentBlack)
            .padding(16.dp, 32.dp, 16.dp, 0.dp)
            .fillMaxSize(),
    ) {
        items(count = items.size, itemContent = { index ->
            GameScoreItem(items[index])
            ScoreboardDivider(modifier = Modifier.height(16.dp), color = Color.Transparent)
        })
    }
}

@Composable
private fun GameScoreItem(game: Game) {
    val gradientBackground = Brush.horizontalGradient(
        colors = backgroundGradient
    )
    val scoreCardHeight = 200.dp
    val winner = game.userScores.firstOrNull { us -> us.user.userId == game.winnerId }?.user

    ScoreboardCard(elevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBackground)
        ) {
            CoilImage(
                data = winner?.profileImageUrl ?: "",
                contentDescription = "",
                fadeIn = true,
                modifier = Modifier
                    .size(150.dp, scoreCardHeight)
                    .background(TransparentBlack),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                game.userScores.sortedBy { it.getTotalScore() }.forEach {
                    Text(
                        text = "${it.user.name} : ${it.getTotalScore()}",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(0.dp, 12.dp)
                    )
                }
            }
        }
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier
                .fillMaxWidth()
                .height(scoreCardHeight)
        ) {
            Text(
                text = game.getDate().toUIFormat(),
                textAlign = TextAlign.End,
                fontSize = 11.sp,
                modifier = Modifier.padding(0.dp, 0.dp, 16.dp, 8.dp)
            )
        }
    }
}