package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.model.Statistics
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.components.Pager
import com.saj.android.scoreboardtracker.ui.components.PagerState
import com.saj.android.scoreboardtracker.ui.components.loadPicture
import com.saj.android.scoreboardtracker.ui.theme.Ocean11
import com.saj.android.scoreboardtracker.ui.theme.backgroundGradient


@Composable
fun Statistics(modifier: Modifier = Modifier) {
    StatisticsContent()
}

@Composable
fun StatisticsContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // use `item` for separate elements like headers
        // and `items` for lists of identical elements
        item { Spacer(modifier = Modifier.height(30.dp)) }
        item { MoviesPager() }
    }
}

@Composable
fun MoviesPager() {
    val mainViewModel: MainViewModel = viewModel()
    val statistics by mainViewModel.scoreStatisticsLiveData.observeAsState(emptyList())

    if (statistics.isNotEmpty()) {
        val pagerState: PagerState = run {
            remember {
                PagerState(0, 0, statistics.size - 1)
            }
        }
        Pager(state = pagerState, modifier = Modifier.height(645.dp)) {
            val isSelected = pagerState.currentPage == page
            StatisticsPagerItem(statistics[page], isSelected)
        }
    }
}

@Composable
fun StatisticsPagerItem(statistics: Statistics, isSelected: Boolean) {
    val animateHeight = animateDpAsState(if (isSelected) 500.dp else 360.dp).value
    val animateWidth = animateDpAsState(if (isSelected) 340.dp else 320.dp).value
    val animateElevation = if (isSelected) 12.dp else 2.dp
    val posterFullPath = statistics.imageUrl
    val gradientBackground = Brush.horizontalGradient(
        colors = backgroundGradient
    )

    Card(
        elevation = animateDpAsState(animateElevation).value,
        modifier = Modifier
            .width(animateWidth)
            .height(animateHeight)
            .padding(24.dp),
        shape = RoundedCornerShape(16.dp),
        contentColor = MaterialTheme.colors.background
    ) {
        Column {
            val image = loadPicture(
                url = posterFullPath,
                defaultImage = R.drawable.common_full_open_on_phone
            ).value
            image?.let { img ->
                Image(
                    bitmap = img.asImageBitmap(),
                    "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(gradientBackground),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${statistics.title} : ",
                    modifier = Modifier.padding(8.dp),
                    color = Ocean11,
                    fontSize = 24.sp
                )
                Text(
                    text = statistics.value.toString(),
                    modifier = Modifier.padding(8.dp),
                    color = Ocean11,
                    fontSize = 26.sp
                )
            }
        }
    }
}