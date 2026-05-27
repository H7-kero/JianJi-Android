package com.jianji.app.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

val iosSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

val iosSnappy = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

val glassPressSpring = spring<Float>(
    dampingRatio = 0.6f,
    stiffness = 1000f
)

val iOSEaseInOut = tween<Float>(
    durationMillis = 350,
    easing = FastOutSlowInEasing
)

val iosSpringOffset = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

val iosSnappyOffset = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

val iOSPageEnter: EnterTransition = fadeIn(animationSpec = iOSEaseInOut) +
    slideInVertically(initialOffsetY = { it / 10 }, animationSpec = iosSpringOffset)

val iOSPageExit: ExitTransition = fadeOut(animationSpec = iOSEaseInOut) +
    slideOutVertically(targetOffsetY = { it / 10 }, animationSpec = iosSpringOffset)

val iOSDialogEnter: EnterTransition = scaleIn(initialScale = 0.95f, animationSpec = iosSpring) +
    fadeIn(animationSpec = tween(durationMillis = 200))

val iOSDialogExit: ExitTransition = scaleOut(targetScale = 0.95f, animationSpec = iosSnappy) +
    fadeOut(animationSpec = tween(durationMillis = 150))

val iOSFABEnter: EnterTransition = scaleIn(initialScale = 0.8f, animationSpec = iosSpring) +
    fadeIn(animationSpec = iOSEaseInOut)

val iOSFABExit: ExitTransition = scaleOut(targetScale = 0.8f, animationSpec = iosSnappy) +
    fadeOut(animationSpec = iOSEaseInOut)

val iOSColorTransition = tween<Float>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

val iOSColorSpec = tween<Color>(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

fun Modifier.iosPressEffect(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = iosSnappy,
        label = "ios_press_scale"
    )
    this.scale(pressScale).clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}

fun Modifier.iosPressScale(
    pressedScale: Float = 0.97f
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = glassPressSpring,
        label = "ios_press_scale"
    )
    this.scale(scale).clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {}
    )
}

val glassHighlightAnimation = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow
)

object BlurRadius {
    val extraLarge = 32.dp
    val large = 24.dp
    val medium = 16.dp
    val small = 8.dp
    val xSmall = 4.dp
}
