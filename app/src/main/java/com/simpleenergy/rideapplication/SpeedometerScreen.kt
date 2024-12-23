package com.simpleenergy.rideapplication

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simpleenergy.rideapplication.AppConstants.EMPTY_STRING
import com.simpleenergy.rideapplication.AppConstants.GREATER
import com.simpleenergy.rideapplication.AppConstants.LESSER
import com.simpleenergy.rideapplication.AppConstants.MOTOR_ON
import com.simpleenergy.rideapplication.AppConstants.ODO
import com.simpleenergy.rideapplication.AppConstants.SIMPLE_ENERGY
import com.simpleenergy.rideapplication.AppConstants.TRAVEL_KM
import com.simpleenergy.rideapplication.AppConstants.TRIP
import com.simpleenergy.rideapplication.AppConstants.TRIP_KM
import com.simpleenergy.rideapplication.ui.theme.padding16Dp
import com.simpleenergy.rideapplication.ui.theme.padding8Dp

import kotlinx.coroutines.delay

@Composable
fun SpeedometerScreen(modifier: Modifier) {
    var speed by rememberSaveable { mutableFloatStateOf(0f) }
    var isIncreasing by rememberSaveable { mutableStateOf(false) }


    val animatedSpeed = animateFloatAsState(
        targetValue = speed,
        animationSpec = tween(durationMillis = 50, easing = FastOutSlowInEasing), label =EMPTY_STRING
    )


    Column(
        modifier = modifier
            .background(Color.Black)
            .fillMaxSize()
    ) {

        MotorText(speed)

        SpeedoMeterContent(animatedSpeed)

        IndicatorContent(modifier = Modifier.weight(.1f).fillMaxWidth())

        BottomInfoContent(Modifier.weight(.2f).fillMaxWidth())

    }
    LaunchedEffect(Unit) {
        while (true) {
            if (isIncreasing) {
                speed += 1f
                if (speed >= 120f) {
                    speed = 120f
                    isIncreasing = false
                }
            } else {
                speed -= 1f
                if (speed <= 0f) {
                    speed = 0f
                    delay(1000)
                    isIncreasing = true
                }
            }
            delay(50)
        }
    }

}

@Composable
private fun ColumnScope.SpeedoMeterContent(animatedSpeed: State<Float>) {
    Box(
        modifier = Modifier.Companion.weight(.6f),
        contentAlignment = Alignment.Center
    ) {

        CoreContent(modifier = Modifier, animatedSpeed.value.toInt())
    }
}

@Composable
private fun ColumnScope.MotorText(speed: Float) {
    Text(
        text = if (speed != 0f) MOTOR_ON else EMPTY_STRING,
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding8Dp)
            .weight(.1f),
        style = TextStyle(
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    )
}


@Composable
fun IndicatorContent(modifier: Modifier) {
    Row(
        modifier = modifier.padding(horizontal = padding16Dp, vertical = padding8Dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        var leftClicked by remember { mutableStateOf(false) }
        var rightClicked by remember { mutableStateOf(false) }
        AnimatedArrowButton(modifier =modifier,"left", leftClicked) {
            println("left clicked fired right value$rightClicked")

            if (rightClicked) {
                println("left right fired")
                rightClicked = false
            }
            leftClicked = !leftClicked

        }
        AnimatedArrowButton(modifier =modifier,side = "right", rightClicked) {
            println("right clicked fired left value$leftClicked")

            if (leftClicked) {
                leftClicked = false
                println(" right left fired")
            }
            rightClicked = !rightClicked

        }


    }
}


@Composable
fun AnimatedArrowButton(
    modifier :Modifier,
    side: String,
    leftClicked: Boolean,
    function: () -> Unit) {


    // Infinite transition for the bounce effect
    val infiniteTransition = rememberInfiniteTransition(label = EMPTY_STRING)
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = EMPTY_STRING
    )

    // Adjust scale dynamically based on animation state
    val dynamicScale =
        if (leftClicked) scale else 1f


    // Animate color of the icon
    val color by animateColorAsState(
        targetValue = if (leftClicked) Color.Green else Color.White, // Change color
        animationSpec = tween(durationMillis = 1000), label = EMPTY_STRING
    )

    IconButton(onClick = function) {

       val icon = if (side == "left") R.drawable.leftindicator else R.drawable.rightindicator
        Icon(
            painter = painterResource(id = icon) ,
            contentDescription = "Arrow Icon",
            tint = color, // Apply animated color
            modifier = modifier
                .size(48.dp) // Icon size
                .scale(dynamicScale) // Apply animated scale
        )
    }
}

@Composable
fun BottomInfoContent(modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = padding16Dp, vertical = padding8Dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = TRIP,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = TRIP_KM,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row {
            Text(text = LESSER, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = SIMPLE_ENERGY,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = GREATER, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = ODO, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = TRAVEL_KM,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Preview(showSystemUi = true, showBackground = true, device = Devices.AUTOMOTIVE_1024p)
@Composable
fun PreviewSpeedMeterScreen() {
    SpeedometerScreen(modifier = Modifier)
}











