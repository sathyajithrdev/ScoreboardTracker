package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.*
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.components.*
import com.saj.android.scoreboardtracker.ui.theme.TransparentBlack
import com.saj.android.scoreboardtracker.ui.theme.backgroundGradient
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Game(viewModel: MainViewModel, modifier: Modifier, onUserClick: (String) -> Unit) {
    ConstraintLayout(modifier = modifier
        .fillMaxSize()
        .background(TransparentBlack),
        content = {
            val (finishButton, usersList) = createRefs()
            ScoreboardButton(
                onClick = { viewModel.onFinishGame() },
                shape = RectangleShape,
                modifier = Modifier
                    .constrainAs(finishButton) {
                        bottom.linkTo(parent.bottom)
                    }
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.finish),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
            }

            UsersList(viewModel = viewModel, modifier = Modifier
                .constrainAs(usersList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(finishButton.top)
                    start.linkTo(parent.start)
                    height = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
                .fillMaxWidth())
        })
}

@ExperimentalCoroutinesApi
@Composable
fun UsersList(viewModel: MainViewModel, modifier: Modifier) {
    val users: List<User>? by viewModel.userLiveData.observeAsState()
    users?.let {
        LazyColumn(modifier.padding(16.dp, 32.dp, 16.dp, 0.dp)) {
            items(count = it.size, itemContent = { index ->
                UserScoreItem(viewModel, it[index])
                ScoreboardDivider(thickness = 16.dp, color = Color.Transparent)
            })
        }
    }
}

@Composable
private fun UserScoreItem(viewModel: MainViewModel, user: User) {
    val gradientBackground = Brush.horizontalGradient(backgroundGradient)
    ScoreboardCard(elevation = 4.dp, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBackground)
        ) {
            val image = loadPicture(url = user.profileImageUrl).value
            image?.let { img ->

                Box(modifier = Modifier.size(150.dp, 200.dp)) {
                    Image(
                        bitmap = img.asImageBitmap(),
                        "",
                        modifier = Modifier.size(150.dp, 200.dp),
                        contentScale = ContentScale.Crop,
                    )
                    recentPerformance(
                        viewModel, user,
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 4.dp)
                    )
                    winStatsView(
                        viewModel, user, Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(TransparentBlack)
                    )
                }
            }
            ScoreView(viewModel, user)
        }
    }
}

@Composable
private fun ScoreView(viewModel: MainViewModel, user: User) {
    val game: Game? by viewModel.onGoingGameLiveData.observeAsState()
    game?.userScores?.firstOrNull { it.user.userId == user.userId }?.let { userScore ->
        val focusManager = LocalFocusManager.current
        VerticalGrid(modifier = Modifier.padding(0.dp), 3) {
            userScore.scores.forEachIndexed { index, score ->
                val textValue = remember { mutableStateOf("") }
                textValue.value = score?.toString() ?: ""
                OutlinedTextField(
                    value = textValue.value,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.padding(6.dp, 0.dp),
                    onValueChange = {
                        if (it.length <= 3) {
                            textValue.value = it
                            with(if (it.isEmpty()) null else it.toInt()) {
                                if (userScore.scores[index] != this) {
                                    viewModel.updateCurrentGameScore(
                                        userScore.user.userId,
                                        index,
                                        this
                                    )
                                }
                            }
                        }
                    },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userScore.getTotalScore().toString(),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun recentPerformance(viewModel: MainViewModel, user: User, modifier: Modifier) {
    val usersPerformance: List<UserRecentPerformance>? by viewModel.recentPerformanceLiveData.observeAsState()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        usersPerformance?.firstOrNull { it.user.userId == user.userId }
            ?.performance?.forEach {
                Icon(
                    painter = painterResource(
                        when (it) {
                            GameResultStatus.Winner -> R.drawable.ic_winner
                            GameResultStatus.Loser -> R.drawable.ic_loser
                            GameResultStatus.Neutral -> R.drawable.ic_neutral
                        }
                    ),
                    tint =
                    when (it) {
                        GameResultStatus.Winner -> Color.Green
                        GameResultStatus.Loser -> Color.Red
                        GameResultStatus.Neutral -> Color(0xFFFFC000)
                    },
                    modifier = Modifier.size(16.dp),
                    contentDescription = null // decorative element
                )
            }
    }
}

@Composable
private fun winStatsView(viewModel: MainViewModel, user: User, modifier: Modifier) {
    val usersPerformance: List<UserWinStats>? by viewModel.winStatsLiveData.observeAsState()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        usersPerformance?.firstOrNull { it.user.userId == user.userId }?.let {
            Text(
                modifier = Modifier.padding(4.dp),
                text = it.wins.toString(),
                color = Color.Green
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = it.runnerUp.toString(),
                color = Color(0xFFFFC000)
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = it.losses.toString(),
                color = Color.Red
            )
        }

    }
}

