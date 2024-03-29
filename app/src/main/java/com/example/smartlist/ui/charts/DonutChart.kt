package com.example.smartlist.ui.charts



import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt


private const val ANIMATION_DURATION = 800
private const val CHART_DEGREES = 360f
private const val EMPTY_INDEX = -1
private val defaultSliceWidth = 20.dp
private val defaultSlicePadding = 5.dp
private val defaultSliceClickPadding = 10.dp

@Composable
internal fun DonutChart(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    inputValues: List<Float>,
    textColor: Color = MaterialTheme.colors.primary,
    sliceWidthDp: Dp = defaultSliceWidth,
    slicePaddingDp: Dp = defaultSlicePadding,
    sliceClickPaddingDp: Dp = defaultSliceClickPadding,
    animated: Boolean = true
){
    assert(inputValues.isNotEmpty() && inputValues.size == colors.size){
        "Input values count must be equal to colors size"
    }

    //calculate each input percentage
    val proportions = inputValues.toPercent()

    //calculate each input slice degrees
    val angleProgress = proportions.map{ prop ->
        CHART_DEGREES * prop / 100
    }

    //start drawing clockwise (top to right)
    var startAngle = 270f

    //used for animating each slice
    val pathPortion = remember{
        Animatable(initialValue = 0f)
    }

    //clicked slice in chart
    var clickedItemIndex by remember {
        mutableIntStateOf(EMPTY_INDEX)
    }

    //calculate each slice end point in degrees, for handling click position
    val progressSize = mutableListOf<Float>()

    LaunchedEffect(angleProgress){
        progressSize.add(angleProgress.first())
        for (x in 1 until angleProgress.size){
            progressSize.add(angleProgress[x] + progressSize[x-1])
        }
    }

    val density = LocalDensity.current

    //convert dp values to pixels
    val sliceWidthPx = with(density) {sliceWidthDp.toPx()}
    val slicePaddingPx = with(density) {slicePaddingDp.toPx()}
    val sliceClickPaddingPx = with(density){sliceClickPaddingDp.toPx()}

    //text style
    val textFontSize = with(density) {30.dp.toPx()}
    val textPaint = remember {
        Paint().apply {
            color = textColor.toArgb()
            textSize = textFontSize
            textAlign = Paint.Align.CENTER
        }
    }

    //slice width when clicked
    val selectedSliceWidth = sliceWidthPx + sliceClickPaddingPx

    LaunchedEffect(inputValues){
        pathPortion.animateTo(1f, animationSpec = tween(if (animated) ANIMATION_DURATION else 0))
    }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val canvasSize = min(constraints.maxWidth, constraints.maxHeight)
        val padding = canvasSize * slicePaddingPx / 100f
        val size = Size(canvasSize.toFloat() - padding, canvasSize.toFloat() - padding)
        val canvasSizeDp = with(density) { canvasSize.toDp() }

        Canvas(
            modifier = Modifier
                .size(canvasSizeDp)
                .pointerInput(inputValues){
                     detectTapGestures { offset ->
                         val clickedAngle = touchPointToAngle(
                             width = canvasSize.toFloat(),
                             height = canvasSize.toFloat(),
                             touchX = offset.x,
                             touchY = offset.y,
                             chartDegrees = CHART_DEGREES
                         )

                         progressSize.forEachIndexed { index, item ->
                             if (clickedAngle <= item){
                                 clickedItemIndex = index
                                 return@detectTapGestures
                             }
                         }
                     }
                }
        ) {
            angleProgress.forEachIndexed { index, angle ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = angle * pathPortion.value,
                    useCenter = false,
                    size = size,
                    style = Stroke(width = if (clickedItemIndex == index) selectedSliceWidth else sliceWidthPx),
                    topLeft = Offset(padding / 2, padding / 2)
                )
                startAngle +=angle
            }


            if (clickedItemIndex != EMPTY_INDEX){
                drawIntoCanvas { canvas->
                    canvas.nativeCanvas.drawText(
                        "${proportions[clickedItemIndex].roundToInt()}%",
                        (canvasSize / 2).toFloat(),
                        (canvasSize / 2).toFloat(),
                        textPaint
                    )
                }
            }

        }
    }


}

internal fun List<Float>.toPercent():List<Float>{
    return this.map { item->
        item * 100 / this.sum()
    }
}

internal fun touchPointToAngle(
    width:Float,
    height: Float,
    touchX: Float,
    touchY: Float,
    chartDegrees: Float
):Double{
    val x = touchX - (width * 0.5f)
    val y = touchY - (height *0.5f)
    var angle = Math.toDegrees(atan2(y.toDouble(),x.toDouble()) + Math.PI / 2)
    angle = if(angle < 0) angle + chartDegrees else angle
    return angle
}