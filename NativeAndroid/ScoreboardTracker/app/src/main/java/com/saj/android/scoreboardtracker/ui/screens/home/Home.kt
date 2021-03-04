/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.saj.android.scoreboardtracker.ui.screens.home

import androidx.annotation.FloatRange
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.os.ConfigurationCompat
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.ui.components.ScoreboardScaffold
import com.saj.android.scoreboardtracker.ui.components.ScoreboardSurface
import com.saj.android.scoreboardtracker.ui.screens.MainViewModel
import com.saj.android.scoreboardtracker.ui.theme.ScoreboardTheme
import kotlinx.coroutines.launch

@Composable
fun Home(homeViewModel: MainViewModel, onUserSelected: (String) -> Unit) {
    val (currentSection, setCurrentSection) = rememberSaveable {
        mutableStateOf(HomeSections.Game)
    }
    val navItems = HomeSections.values().toList()
    ScoreboardScaffold(
        bottomBar = {
            ScoreboardBottomNav(
                currentSection = currentSection,
                onSectionSelected = setCurrentSection,
                items = navItems
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        Crossfade(currentSection) { section ->
            when (section) {
                HomeSections.Game -> Game(
                    viewModel = homeViewModel,
                    onUserClick = onUserSelected,
                    modifier = modifier
                )
                HomeSections.Stats -> Statistics(
                    modifier = modifier
                )
                HomeSections.History -> History(
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun ScoreboardBottomNav(
    currentSection: HomeSections,
    onSectionSelected: (HomeSections) -> Unit,
    items: List<HomeSections>,
    color: Color = ScoreboardTheme.colors.iconPrimary,
    contentColor: Color = ScoreboardTheme.colors.iconInteractive
) {
    ScoreboardSurface(
        color = color,
        contentColor = contentColor
    ) {
        val springSpec = remember {
            SpringSpec<Float>(
                stiffness = 800f,
                dampingRatio = 0.8f
            )
        }
        ScoreboardBottomNavLayout(
            selectedIndex = currentSection.ordinal,
            itemCount = items.size,
            indicator = { ScoreboardBottomNavIndicator() },
            animSpec = springSpec,
            modifier = Modifier.padding(0.dp)
        ) {
            items.forEach { section ->
                val selected = section == currentSection
                val tint by animateColorAsState(
                    if (selected) {
                        ScoreboardTheme.colors.iconInteractive
                    } else {
                        ScoreboardTheme.colors.iconInteractiveInactive
                    }
                )

                ScoreboardBottomNavigationItem(
                    icon = {
                        Icon(
                            imageVector = section.icon,
                            tint = tint,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(section.title).toUpperCase(
                                ConfigurationCompat.getLocales(
                                    LocalConfiguration.current
                                ).get(0)
                            ),
                            color = tint,
                            style = MaterialTheme.typography.button,
                            maxLines = 1
                        )
                    },
                    selected = selected,
                    onSelected = { onSectionSelected(section) },
                    animSpec = springSpec,
                    modifier = BottomNavigationItemPadding
                        .clip(BottomNavIndicatorShape)
                )
            }
        }
    }
}

@Composable
private fun ScoreboardBottomNavLayout(
    selectedIndex: Int,
    itemCount: Int,
    animSpec: AnimationSpec<Float>,
    indicator: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Track how "selected" each item is [0, 1]
    val selectionFractions = remember(itemCount) {
        List(itemCount) { i ->
            Animatable(if (i == selectedIndex) 1f else 0f)
        }
    }
    val scope = rememberCoroutineScope()
    selectionFractions.forEachIndexed { index, selectionFraction ->
        val target = if (index == selectedIndex) 1f else 0f
        if (selectionFraction.targetValue != target) {
            scope.launch {
                selectionFraction.animateTo(target, animSpec)
            }
        }
    }

    // Animate the position of the indicator
    val indicatorIndex = remember { Animatable(0f) }
    val targetIndicatorIndex = selectedIndex.toFloat()
    LaunchedEffect(targetIndicatorIndex) {
        indicatorIndex.animateTo(targetIndicatorIndex, animSpec)
    }

    Layout(
        modifier = modifier.height(BottomNavHeight),
        content = {
            content()
            Box(Modifier.layoutId("indicator"), content = indicator)
        }
    ) { measurables, constraints ->
        check(itemCount == (measurables.size - 1)) // account for indicator

        // Divide the width into n+1 slots and give the selected item 2 slots
        val unselectedWidth = constraints.maxWidth / (itemCount + 1)
        val selectedWidth = 2 * unselectedWidth
        val indicatorMeasurable = measurables.first { it.layoutId == "indicator" }

        val itemPlaceables = measurables
            .filterNot { it == indicatorMeasurable }
            .mapIndexed { index, measurable ->
                // Animate item's width based upon the selection amount
                val width = lerp(unselectedWidth, selectedWidth, selectionFractions[index].value)
                measurable.measure(
                    constraints.copy(
                        minWidth = width,
                        maxWidth = width
                    )
                )
            }
        val indicatorPlaceable = indicatorMeasurable.measure(
            constraints.copy(
                minWidth = selectedWidth,
                maxWidth = selectedWidth
            )
        )

        layout(
            width = constraints.maxWidth,
            height = itemPlaceables.maxByOrNull { it.height }?.height ?: 0
        ) {
            val indicatorLeft = indicatorIndex.value * unselectedWidth
            indicatorPlaceable.place(x = indicatorLeft.toInt(), y = 0)
            var x = 0
            itemPlaceables.forEach { placeable ->
                placeable.place(x = x, y = 0)
                x += placeable.width
            }
        }
    }
}

@Composable
fun ScoreboardBottomNavigationItem(
    icon: @Composable BoxScope.() -> Unit,
    text: @Composable BoxScope.() -> Unit,
    selected: Boolean,
    onSelected: () -> Unit,
    animSpec: AnimationSpec<Float>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.selectable(selected = selected, onClick = onSelected),
        contentAlignment = Alignment.Center
    ) {
        // Animate the icon/text positions within the item based on selection
        val animationProgress by animateFloatAsState(if (selected) 1f else 0f, animSpec)
        ScoreboardBottomNavItemLayout(
            icon = icon,
            text = text,
            animationProgress = animationProgress
        )
    }
}

@Composable
private fun ScoreboardBottomNavItemLayout(
    icon: @Composable BoxScope.() -> Unit,
    text: @Composable BoxScope.() -> Unit,
    @FloatRange(from = 0.0, to = 1.0) animationProgress: Float
) {
    Layout(
        content = {
            Box(Modifier.layoutId("icon"), content = icon)
            val scale = lerp(0.6f, 1f, animationProgress)
            Box(
                modifier = Modifier
                    .layoutId("text")
                    .padding(start = TextIconSpacing)
                    .graphicsLayer {
                        alpha = animationProgress
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = BottomNavLabelTransformOrigin
                    },
                content = text
            )
        }
    ) { measurables, constraints ->
        val iconPlaceable = measurables.first { it.layoutId == "icon" }.measure(constraints)
        val textPlaceable = measurables.first { it.layoutId == "text" }.measure(constraints)

        placeTextAndIcon(
            textPlaceable,
            iconPlaceable,
            constraints.maxWidth,
            constraints.maxHeight,
            animationProgress
        )
    }
}

private fun MeasureScope.placeTextAndIcon(
    textPlaceable: Placeable,
    iconPlaceable: Placeable,
    width: Int,
    height: Int,
    @FloatRange(from = 0.0, to = 1.0) animationProgress: Float
): MeasureResult {
    val iconY = (height - iconPlaceable.height) / 2
    val textY = (height - textPlaceable.height) / 2

    val textWidth = textPlaceable.width * animationProgress
    val iconX = (width - textWidth - iconPlaceable.width) / 2
    val textX = iconX + iconPlaceable.width

    return layout(width, height) {
        iconPlaceable.place(iconX.toInt(), iconY)
        if (animationProgress != 0f) {
            textPlaceable.place(textX.toInt(), textY)
        }
    }
}

@Composable
private fun ScoreboardBottomNavIndicator(
    strokeWidth: Dp = 2.dp,
    color: Color = ScoreboardTheme.colors.iconInteractive,
    shape: Shape = BottomNavIndicatorShape
) {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .then(BottomNavigationItemPadding)
            .border(strokeWidth, color, shape)
    )
}

private val TextIconSpacing = 4.dp
private val BottomNavHeight = 56.dp
private val BottomNavLabelTransformOrigin = TransformOrigin(0f, 0.5f)
private val BottomNavIndicatorShape = RoundedCornerShape(percent = 50)
private val BottomNavigationItemPadding = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)

private enum class HomeSections(
    @StringRes val title: Int,
    val icon: ImageVector
) {
    Game(R.string.game_route, Icons.Outlined.Home),
    Stats(R.string.stats_route, Icons.Outlined.Stars),
    History(R.string.history_route, Icons.Outlined.History),
}

@Preview
@Composable
private fun JsetsnackBottomNavPreview() {
    ScoreboardTheme {
        ScoreboardBottomNav(
            currentSection = HomeSections.Game,
            onSectionSelected = { },
            items = HomeSections.values().toList()
        )
    }
}
