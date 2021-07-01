package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.UserServeSequence
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.base.BaseActivity
import com.saj.android.scoreboardtracker.ui.components.ScoreboardButton
import com.saj.android.scoreboardtracker.ui.theme.CardBackgroundEnd
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun ServerSequenceSetupContent(
    viewModel: MainViewModel,
    bottomSheetScaffoldState: ModalBottomSheetState
) {
    val users: List<UserServeSequence>? by viewModel.usersServeSequenceLiveData.observeAsState()
    val focusManager = LocalFocusManager.current
    Column(
        Modifier
            .background(CardBackgroundEnd)
            .padding(16.dp, 16.dp, 16.dp, 120.dp)
            .fillMaxWidth()
    ) {
        users?.let { sequenceData ->
            Text(text = "Specify the serve order", color = Color.White, fontSize = 16.sp)
            LazyColumn(Modifier.padding(16.dp, 32.dp, 16.dp, 0.dp)) {
                items(count = sequenceData.size, itemContent = { index ->
                    val userServeSequence = sequenceData[index]
                    val textValue =
                        remember { mutableStateOf(userServeSequence.order?.toString() ?: "") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userServeSequence.userName,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.defaultMinSize(100.dp)
                        )
                        OutlinedTextField(
                            value = textValue.value,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Number
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White),
                            modifier = Modifier
                                .padding(6.dp)
                                .width(150.dp),
                            shape = RoundedCornerShape(4.dp),
                            onValueChange = {
                                if (it.length <= 1) {
                                    textValue.value = it
                                    with(if (it.isEmpty()) null else it.toInt()) {
                                        userServeSequence.order = this
                                    }
                                }
                            },
                        )
                    }
                })
            }
            Box(Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp)) {
                saveButton(viewModel, bottomSheetScaffoldState)
            }
        }
    }
}


@ExperimentalMaterialApi
@Composable
private fun saveButton(
    viewModel: MainViewModel,
    bottomSheetScaffoldState: ModalBottomSheetState
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    ScoreboardButton(
        onClick = {
            if (viewModel.onSaveServeSequence()) {
                coroutineScope.launch {
                    bottomSheetScaffoldState.hide()
                }
            } else {
                if (context is BaseActivity) {
                    context.showToast("Enter valid serve sequence")
                }
            }
        },
        shape = RectangleShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.save),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
    }
}