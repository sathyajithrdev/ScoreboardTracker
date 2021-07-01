package com.saj.android.scoreboardtracker.ui.screens.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.saj.android.scoreboardtracker.ui.MainViewModel
import com.saj.android.scoreboardtracker.ui.components.CoilImage
import com.saj.android.scoreboardtracker.ui.components.piechart.PieChart
import com.saj.android.scoreboardtracker.ui.components.piechart.PieChartData
import com.saj.android.scoreboardtracker.ui.components.piechart.SimpleSliceDrawer
import com.saj.android.scoreboardtracker.ui.components.piechart.simpleChartAnimation
import com.saj.android.scoreboardtracker.ui.theme.RunnerUp

@Composable
fun UserStatistics(viewModel: MainViewModel, userId: String) {
    val stats by viewModel.winStatsLiveData.observeAsState()
    val users by viewModel.userLiveData.observeAsState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        content = {
            val (profileImage, winStats, statGraph, lastGamePerformance) = createRefs()
            users?.firstOrNull { it.userId == userId }?.let { user ->
                Box(
                    modifier = Modifier
                        .constrainAs(profileImage) {
                            top.linkTo(parent.top)
                            bottom.linkTo(winStats.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.fillToConstraints
                        }
                        .fillMaxWidth()
                ) {
                    CoilImage(
                        modifier = Modifier.fillMaxSize(),
                        data = user.profileImageUrl,
                        contentScale = ContentScale.Crop,
                        contentDescription = "",
                    )
                }
                winStatsView(
                    viewModel, user,
                    Modifier
                        .constrainAs(winStats) {
                            bottom.linkTo(statGraph.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.wrapContent
                        }
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                stats?.firstOrNull { it.user.userId == userId }?.let {
                    PieChart(
                        pieChartData = PieChartData(
                            listOf(
                                PieChartData.Slice(it.wins.toFloat(), Color.Green),
                                PieChartData.Slice(it.runnerUp.toFloat(), RunnerUp),
                                PieChartData.Slice(it.losses.toFloat(), Color.Red)
                            )
                        ),
                        modifier = Modifier
                            .constrainAs(statGraph) {
                                bottom.linkTo(lastGamePerformance.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                height = Dimension.wrapContent
                            }
                            .size(250.dp)
                            .fillMaxWidth()
                            .padding(16.dp),
                        animation = simpleChartAnimation(),
                        sliceDrawer = SimpleSliceDrawer(100f)
                    )
                }

                recentPerformance(
                    viewModel,
                    user,
                    modifier = Modifier
                        .constrainAs(lastGamePerformance) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.wrapContent
                        }
                        .fillMaxWidth()
                        .padding(8.dp, 4.dp, 8.dp, 80.dp))
            }
        })
}