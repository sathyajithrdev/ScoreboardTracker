package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.User
import com.saj.android.scoreboardtracker.ui.components.*
import com.saj.android.scoreboardtracker.ui.screens.MainViewModel
import com.saj.android.scoreboardtracker.ui.theme.Ocean11
import kotlinx.coroutines.ExperimentalCoroutinesApi


@Composable
fun Game(viewModel: MainViewModel, modifier: Modifier, onUserClick: (String) -> Unit) {
    viewModel.getUsersList()
    ScoreboardSurface(modifier = modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize(),
            content = {
                val (finishButton, usersList) = createRefs()
                ScoreboardButton(
                    onClick = { /* Do something */ },
                    shape = RectangleShape,
                    modifier = Modifier.constrainAs(finishButton) {
                        bottom.linkTo(parent.bottom)
                    }.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.finish),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    )
                }
                UsersList(viewModel = viewModel, modifier = Modifier.constrainAs(usersList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(finishButton.top)
                    start.linkTo(parent.start)
                    height = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }.fillMaxWidth())
            })
    }
}

@ExperimentalCoroutinesApi
@Composable
fun UsersList(viewModel: MainViewModel, modifier: Modifier) {
    val items: List<User> by viewModel.userLiveData.observeAsState(listOf())
    LazyColumn(modifier.padding(16.dp, 32.dp, 16.dp, 0.dp)) {
        items(count = items.size, itemContent = { index ->
            val user = items[index]
            UserScoreItem(user)
            ScoreboardDivider(thickness = 16.dp, color = Color.Transparent)
        })
    }
}

@Composable
private fun UserScoreItem(user: User) {
    Card(backgroundColor = Ocean11, elevation = 4.dp) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val image = loadPicture(
                url = user.profileImageUrl,
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
                (0..7).forEach { i ->
                    Surface(color = Color.Transparent, modifier = Modifier.padding(0.dp)) {
                        val textValue = remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = textValue.value,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.weight(1f).padding(6.dp, 0.dp),
                            onValueChange = { textValue.value = it },
                        )
                    }
                }
            }
        }
    }
}
