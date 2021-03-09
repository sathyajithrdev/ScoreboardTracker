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
import androidx.compose.ui.Alignment
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
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.Game
import com.saj.android.scoreboardtracker.model.UserScore
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.components.*
import com.saj.android.scoreboardtracker.ui.theme.backgroundGradient
import kotlinx.coroutines.ExperimentalCoroutinesApi


@Composable
fun Game(viewModel: MainViewModel, modifier: Modifier, onUserClick: (String) -> Unit) {
    ScoreboardSurface(modifier = modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize(),
            content = {
                val (finishButton, usersList) = createRefs()
                ScoreboardButton(
                    onClick = { /* Do something */ },
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
}

@ExperimentalCoroutinesApi
@Composable
fun UsersList(viewModel: MainViewModel, modifier: Modifier) {
    val game: Game? by viewModel.onGoingGameLiveData.observeAsState()
    game?.let {
        LazyColumn(modifier.padding(16.dp, 32.dp, 16.dp, 0.dp)) {
            items(count = it.userScores.size, itemContent = { index ->
                val userScore = it.userScores[index]
                UserScoreItem(userScore)
                ScoreboardDivider(thickness = 16.dp, color = Color.Transparent)
            })
        }
    }
}

@Composable
private fun UserScoreItem(userScore: UserScore) {
    val focusManager = LocalFocusManager.current
    val gradientBackground = Brush.horizontalGradient(backgroundGradient)
    ScoreboardCard(elevation = 4.dp, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBackground)
        ) {
            val image = loadPicture(
                url = userScore.user.profileImageUrl,
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
                                userScore.scores[index] = if (it.isEmpty()) null else it.toInt()
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
}

