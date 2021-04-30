package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.*
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.components.ScoreboardButton
import com.saj.android.scoreboardtracker.ui.components.ScoreboardCard
import com.saj.android.scoreboardtracker.ui.components.ScoreboardDivider
import com.saj.android.scoreboardtracker.ui.components.VerticalGrid
import com.saj.android.scoreboardtracker.ui.theme.*
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun Game(viewModel: MainViewModel, modifier: Modifier) {
    val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        sheetState = state,
        scrimColor = SemiTransparentBlack,
        sheetContent = { ServerSequenceSetupContent(viewModel, state) }
    ) {
        ScoreboardContent(state, modifier, viewModel)
    }
}

@ExperimentalMaterialApi
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ScoreboardContent(
    bottomSheetScaffoldState: ModalBottomSheetState,
    modifier: Modifier,
    viewModel: MainViewModel
) {

    val uiState by viewModel.uiState.observeAsState()
    val isLoading = uiState?.first == MainViewModel.UIState.Loading

    LoadingView(stringResource(R.string.loading_wait), isLoading)

    if (!isLoading) {
        ConstraintLayout(
            modifier = modifier.background(SemiTransparentBlack),
            content = {
                val (finishButton, lastWinStat, usersList, winnerAnimation) = createRefs()
                finishButton(
                    modifier = Modifier
                        .constrainAs(finishButton) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.wrapContent
                        }
                        .fillMaxWidth(),
                    viewModel = viewModel)

                UsersList(
                    bottomSheetScaffoldState = bottomSheetScaffoldState,
                    viewModel = viewModel,
                    modifier = Modifier
                        .constrainAs(usersList) {
                            top.linkTo(parent.top)
                            bottom.linkTo(finishButton.top)
                            start.linkTo(parent.start)
                            height = Dimension.fillToConstraints
                        }
                        .fillMaxWidth())

                WinnerAnimation(
                    viewModel = viewModel,
                    modifier = Modifier.constrainAs(winnerAnimation) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    })

                LastWinStatView(modifier = Modifier
                    .constrainAs(lastWinStat) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        height = Dimension.wrapContent
                    }
                    .fillMaxWidth(), viewModel = viewModel)
            })
        LoadingView(
            stringResource(R.string.finishing_game),
            uiState?.first == MainViewModel.UIState.FinishingGame
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun LastWinStatView(modifier: Modifier, viewModel: MainViewModel) {
    val winStatMessage: String by viewModel.lastSetWinStatLiveData.observeAsState("")
    AnimatedVisibility(visible = winStatMessage.isNotEmpty(), modifier = modifier.padding(16.dp)) {
        Text(
            text = winStatMessage,
            color = Color.Green,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@ExperimentalAnimationApi
@Composable
private fun finishButton(modifier: Modifier, viewModel: MainViewModel) {
    val canSaveGame: Boolean by viewModel.canFinishGameLiveData.observeAsState(false)
    AnimatedVisibility(visible = canSaveGame, modifier = modifier.fillMaxWidth()) {
        ScoreboardButton(
            onClick = { viewModel.onFinishGame() },
            shape = RectangleShape,
            backgroundGradient = buttonBackgroundGradient
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
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@Composable
fun UsersList(
    bottomSheetScaffoldState: ModalBottomSheetState,
    viewModel: MainViewModel,
    modifier: Modifier
) {
    val users: List<User>? by viewModel.userLiveData.observeAsState()
    users?.let {
        LazyColumn(modifier.padding(16.dp, 32.dp, 16.dp, 0.dp)) {
            items(count = it.size, itemContent = { index ->
                UserScoreItem(bottomSheetScaffoldState, viewModel, it[index])
                ScoreboardDivider(thickness = 16.dp, color = Color.Transparent)
            })
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
private fun UserScoreItem(
    bottomSheetScaffoldState: ModalBottomSheetState,
    viewModel: MainViewModel,
    user: User
) {
    val gradientBackground = Brush.horizontalGradient(backgroundGradient)
    val coroutineScope = rememberCoroutineScope()
    ScoreboardCard(elevation = 6.dp, color = Color.Transparent, shape = RoundedCornerShape(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBackground)
        ) {
            Box(modifier = Modifier.size(150.dp, 200.dp)) {
                CoilImage(
                    data = user.profileImageUrl,
                    contentDescription = "",
                    fadeIn = true,
                    colorFilter = ColorFilter.tint(Color(0x1E000000)),
                    modifier = Modifier
                        .size(150.dp, 200.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.show()
                                }
                            })
                        },
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
                nextToServeUserView(viewModel, user, Modifier.align(Alignment.CenterStart))
            }
            ScoreView(viewModel, user)
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun nextToServeUserView(viewModel: MainViewModel, user: User, modifier: Modifier) {

    val serverUserId: String by viewModel.nextServerUserLiveData.observeAsState("")
    AnimatedVisibility(visible = serverUserId == user.userId, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_next_server),
            modifier = Modifier.size(20.dp),
            tint = Ocean8,
            contentDescription = null
        )
    }

}

@Composable
private fun ScoreView(viewModel: MainViewModel, user: User) {
    val game: Game? by viewModel.onGoingGameLiveData.observeAsState()
    game?.userScores?.firstOrNull { it.user.userId == user.userId }?.let { userScore ->
        val focusManager = LocalFocusManager.current
        VerticalGrid(modifier = Modifier.padding(6.dp, 0.dp), 3) {
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
                    modifier = Modifier.padding(2.dp, 0.dp),
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

@ExperimentalAnimationApi
@Composable
fun WinnerAnimation(modifier: Modifier, viewModel: MainViewModel) {
    val winnerData: Pair<Boolean, User?>? by viewModel.winnerLiveData.observeAsState()
    val context = LocalContext.current
    val customView = remember { LottieAnimationView(context) }
    AnimatedVisibility(
        visible = winnerData?.first == true,
        modifier = modifier.size(300.dp, 300.dp),
        enter = fadeIn(), exit = fadeOut()
    ) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            winnerData?.second?.let { user ->
                CoilImage(
                    data = user.profileImageUrl,
                    contentDescription = "",
                    fadeIn = true,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                AndroidView({ customView }) { view ->
                    // View's been inflated - add logic here if necessary
                    with(view) {
                        setAnimation(R.raw.winner_animation)
                        playAnimation()
                        repeatCount = 10
                        repeatMode = LottieDrawable.RESTART
                    }
                }
            }
        }
    }
}

