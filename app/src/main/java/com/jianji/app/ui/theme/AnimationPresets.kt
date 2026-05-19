package com.jianji.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

val iOSSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

val iOSEaseInOut = tween<Float>(
    durationMillis = 350,
    easing = FastOutSlowInEasing
)

val iOSSnappy = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

val iOSPageEnter = tween<Float>(
    durationMillis = 400,
    easing = FastOutSlowInEasing
)

val iOSPageExit = tween<Float>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

val iOSDialogEnter = tween<Float>(
    durationMillis = 400,
    easing = FastOutSlowInEasing
)

val iOSDialogExit = tween<Float>(
    durationMillis = 250,
    easing = FastOutSlowInEasing
)

val iOSColorTransition = tween<Float>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

fun Modifier.iosPressEffect(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = iOSSnappy,
        label = "press_scale"
    )
    scale(scale).clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}
