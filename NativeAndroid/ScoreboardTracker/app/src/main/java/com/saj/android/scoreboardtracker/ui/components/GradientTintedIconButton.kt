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

package com.saj.android.scoreboardtracker.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.saj.android.scoreboardtracker.ui.theme.ScoreboardTheme

@Composable
fun ScoreboardGradientTintedIconButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    colors: List<Color> = ScoreboardTheme.colors.interactiveSecondary
) {
    // This should use a layer + srcIn but needs investigation
    val blendMode = if (ScoreboardTheme.colors.isDark) BlendMode.Darken else BlendMode.Plus
    IconButton(onClick = onClick, modifier) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.diagonalGradientTint(
                colors = colors,
                blendMode = blendMode
            )
        )
    }
}
