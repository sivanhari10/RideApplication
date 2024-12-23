package com.simpleenergy.rideapplication

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.simpleenergy.rideapplication.AppConstants.EMPTY_STRING
import com.simpleenergy.rideapplication.AppConstants.getDataSets
import com.simpleenergy.rideapplication.ui.theme.padding16Dp
import kotlin.math.pow
import kotlin.random.Random


@Composable
fun DrawSpeedContent(
    dataPoints: List<Float>,
    modifier: Modifier,
    iterations: Int = 10,
    scaleFactor: Float = .92f,
    speedValue: Int,
    isPortrait: Boolean
) {
    val speed by rememberUpdatedState(newValue = speedValue)
    val context = LocalContext.current

    val maxDataPoint = 100f
    val minDataPoint = dataPoints.minOrNull() ?: 0f
    val snowflakes = remember { List(100) { generateRandomSnowflake() } }

    val infiniteTransition = rememberInfiniteTransition(label = EMPTY_STRING)
    val glowAlpha = getGlowAlpha(infiniteTransition)
    val offsetY = getOffsetY(infiniteTransition)

    val alpha = calculateAlpha(speed)
    val customTypeface = getCustomTypeface(context)

    Canvas(modifier = modifier.fillMaxSize()) {
        val chartWidth = size.width
        val chartHeight = size.height
        val yRange = maxDataPoint - minDataPoint
        val normalizedPoints = normalizeDataPoints(dataPoints, minDataPoint, yRange, chartHeight)
        val adjustedWidth = if (isPortrait) chartWidth else chartWidth * 0.8f

        val brush = createGradientBrush(speed, center, chartWidth, chartHeight, alpha)
        val brush1 = createGradientBrushWithAlpha(speed, center, chartWidth, chartHeight, alpha)

        drawSnowflakes(snowflakes, brush1, offsetY, size)
        drawGlowEffect(brush1, glowAlpha, center)
        drawChartLines(
            iterations, scaleFactor, adjustedWidth, chartHeight, chartWidth,
            normalizedPoints, brush1
        )
        drawTextOverlay(chartWidth, chartHeight, speed, customTypeface)
        drawBackgroundEffect(brush, size)
    }
}

private fun createGradientBrushWithAlpha(
    speed: Int,
    center: Offset,
    chartWidth: Float,
    chartHeight: Float,
    alpha: Float
): Brush {
    return Brush.radialGradient(
        colors = when (speed) {
            in 0..50 -> listOf(
                Color.Green.copy(alpha = alpha),
                Color.Black.copy(alpha = alpha)
            )
            in 51..90 -> listOf(
                Color.Yellow.copy(alpha = alpha),
                Color.Black.copy(alpha = alpha)
            )
            else -> listOf(
                Color.Red.copy(alpha = alpha),
                Color.Black.copy(alpha = alpha)
            )
        },
        center = center,
        radius = maxOf(chartWidth, chartHeight) / 2
    )
}


@Composable
private fun getGlowAlpha(infiniteTransition: InfiniteTransition) =
    infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 50, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = EMPTY_STRING
    ).value

@Composable
private fun getOffsetY(infiniteTransition: InfiniteTransition) =
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = EMPTY_STRING
    ).value

private fun calculateAlpha(speed: Int): Float {
    return when {
        speed in 0..90 -> (speed / 120f).coerceIn(0.1f, 1f)
        else -> 1f
    }
}

private fun getCustomTypeface(context: Context): android.graphics.Typeface =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        android.graphics.Typeface.create(
            context.resources.getFont(R.font.prospekt),
            android.graphics.Typeface.NORMAL
        )
    } else {
        android.graphics.Typeface.createFromAsset(
            context.assets,
            "fonts/prospekt.ttf"
        )
    }

private fun normalizeDataPoints(
    dataPoints: List<Float>,
    minDataPoint: Float,
    yRange: Float,
    chartHeight: Float
): List<Float> {
    return dataPoints.map {
        (it - minDataPoint) / yRange * chartHeight
    }
}

private fun createGradientBrush(
    speed: Int,
    center: Offset,
    chartWidth: Float,
    chartHeight: Float,
    alpha: Float
): Brush {
    val colors = when (speed) {
        in 0..50 -> listOf(Color.Green, Color.Black)
        in 51..90 -> listOf(Color.Yellow, Color.Black)
        else -> listOf(Color.Red, Color.Black)
    }
    return Brush.radialGradient(
        colors = colors.map { it.copy(alpha = alpha) },
        center = center,
        radius = maxOf(chartWidth, chartHeight) / 2
    )
}

private fun DrawScope.drawSnowflakes(
    snowflakes: List<Snowflake>,
    brush: Brush,
    offsetY: Float,
    size: Size
) {
    snowflakes.forEach { snowflake ->
        drawSnowflake(brush, snowflake, offsetY % size.height)
    }
}

private fun DrawScope.drawGlowEffect(
    brush: Brush,
    glowAlpha: Float,
    center: Offset
) {
    drawCircle(
        brush = brush,
        radius = glowAlpha,
        center = center,
        alpha = .1f,
    )
}

private fun DrawScope.drawChartLines(
    iterations: Int,
    scaleFactor: Float,
    adjustedWidth: Float,
    chartHeight: Float,
    chartWidth: Float,
    normalizedPoints: List<Float>,
    brush: Brush,
) {
    for (layer in 0 until iterations) {
        val scale = scaleFactor.pow(layer.toFloat())
        val layerWidth = adjustedWidth * scale
        val layerHeight = chartHeight * scale
        val xOffset = (chartWidth - layerWidth) / 2
        val yOffset = (chartHeight - layerHeight) / 2

        for (i in 0 until normalizedPoints.size - 1) {
            val startX = xOffset + i * (layerWidth / 10)
            val startY = yOffset + layerHeight - (normalizedPoints[i] * scale)
            val endX = xOffset + (i + 1) * (layerWidth / 10)
            val endY = yOffset + layerHeight - (normalizedPoints[i + 1] * scale)

            drawLineWithMirroring(startX, startY, endX, endY, chartHeight, chartWidth, brush)
        }
    }
}

private fun DrawScope.drawLineWithMirroring(
    startX: Float, startY: Float, endX: Float, endY: Float,
    chartHeight: Float, chartWidth: Float, brush: Brush
) {
    // Original line
    drawLine(brush, Offset(startX, startY), Offset(endX, endY), strokeWidth = 10f)

    // Vertical mirror
    drawLine(brush, Offset(startX, chartHeight - startY), Offset(endX, chartHeight - endY), strokeWidth = 10f)

    // Horizontal mirror
    drawLine(brush, Offset(chartWidth - startX, startY), Offset(chartWidth - endX, endY), strokeWidth = 10f)

    // Four-sided mirror
    drawLine(
        brush,
        Offset(chartWidth - startX, chartHeight - startY),
        Offset(chartWidth - endX, chartHeight - endY),
        strokeWidth = 10f
    )
}

private fun DrawScope.drawTextOverlay(
    chartWidth: Float,
    chartHeight: Float,
    speed: Int,
    customTypeface: android.graphics.Typeface
) {
    drawContext.canvas.nativeCanvas.apply {
        val boldPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 150f
            isFakeBoldText = true
            typeface = customTypeface
        }

        val x = chartWidth / 2
        val yBold = chartHeight / 2 - (boldPaint.descent() + boldPaint.ascent()) / 2
        drawText("$speed", x, yBold, boldPaint)

        val smallPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 40f
            typeface = android.graphics.Typeface.SANS_SERIF
        }

        val ySmall = yBold + boldPaint.descent() + 10
        drawText("Km/h", x, ySmall, smallPaint)
    }
}

private fun DrawScope.drawBackgroundEffect(
    brush: Brush,
    size: Size
) {
    drawRect(
        brush = brush,
        blendMode = BlendMode.Color,
        size = size
    )
}


@Composable
fun CoreContent(modifier: Modifier, speed: Int) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Column(
        modifier = modifier.padding(padding16Dp)
    ) {

        DrawSpeedContent(
            dataPoints = getDataSets(isPortrait),
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp),
            speedValue = speed,
            isPortrait = isPortrait
        )


    }
}


data class Snowflake(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speed: Float
)

fun generateRandomSnowflake(): Snowflake {
    return Snowflake(
        x = Random.nextFloat(),
        y = Random.nextFloat() * 1000f,
        radius = Random.nextFloat() * 2f + 2f, // Snowflake size
        speed = Random.nextFloat() * 1.2f + 1f  // Falling speed
    )
}

fun DrawScope.drawSnowflake(brush: Brush, snowflake: Snowflake, offsetY: Float) {
    val newY = (snowflake.y + offsetY * snowflake.speed) % size.height
    drawCircle(
        brush = brush,
        radius = snowflake.radius,
        center = Offset(snowflake.x * size.width, newY)
    )
}









