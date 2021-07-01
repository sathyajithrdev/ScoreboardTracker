package com.saj.android.scoreboardtracker.ui.components.piechart

import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.saj.android.scoreboardtracker.extensions.toPx
import com.saj.android.scoreboardtracker.ui.components.piechart.PieChartUtils.calculateAngle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun PieChart(
    pieChartData: PieChartData,
    modifier: Modifier = Modifier,
    animation: AnimationSpec<Float> = simpleChartAnimation(),
    sliceDrawer: SliceDrawer = SimpleSliceDrawer(100f)
) {
    val transitionProgress = remember(pieChartData.slices) { Animatable(initialValue = 0f) }

    // When slices value changes we want to re-animated the chart.
    LaunchedEffect(pieChartData.slices) {
        transitionProgress.animateTo(1f, animationSpec = animation)
    }

    DrawChart(
        pieChartData = pieChartData,
        modifier = modifier.fillMaxSize(),
        progress = transitionProgress.value,
        sliceDrawer = sliceDrawer
    )
}

@Composable
private fun DrawChart(
    pieChartData: PieChartData,
    modifier: Modifier,
    progress: Float,
    sliceDrawer: SliceDrawer
) {
    val slices = pieChartData.slices


    val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 16.sp.toPx()
        color = android.graphics.Color.WHITE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    Canvas(modifier = modifier) {
        drawIntoCanvas {
            var startArc = 0f

            slices.forEach { slice ->

                val arc = calculateAngle(
                    sliceLength = slice.value,
                    totalLength = pieChartData.totalSize,
                    progress = progress
                )

                val area = sliceDrawer.drawSlice(
                    drawScope = this,
                    canvas = drawContext.canvas,
                    area = size,
                    startAngle = startArc,
                    sweepAngle = arc,
                    slice = slice
                )
                if (progress == 1.0f) {
                    val percentage = slice.value * 100 / pieChartData.totalSize
                    drawPercentageText(it, percentage, startArc, arc, area, textPaint)
                }
                startArc += arc
            }
        }

    }
}

fun drawPercentageText(
    canvas: Canvas,
    slice: Float,
    startAngle: Float,
    sweepAngle: Float,
    drawableArea: Rect,
    textPaint: NativePaint
) {
    val angleInRadians = (360 - (startAngle + sweepAngle / 2)) * PI / 180
    val radius = drawableArea.height / 2
    val x = (radius * cos(angleInRadians)).toFloat()
    val y = (radius * sin(angleInRadians)).toFloat()
    val textLengthXCorrection = -20

    drawableArea.let {
        canvas.nativeCanvas.drawText(
            "${slice.roundToInt()}%",
            it.center.x + x + textLengthXCorrection,
            it.center.y - y,
            textPaint
        )
    }
}

@Preview
@Composable
fun PieChartPreview() = PieChart(
    pieChartData = PieChartData(
        slices = listOf(
            PieChartData.Slice(25f, Color.Red),
            PieChartData.Slice(42f, Color.Blue),
            PieChartData.Slice(23f, Color.Green)
        )
    )
)