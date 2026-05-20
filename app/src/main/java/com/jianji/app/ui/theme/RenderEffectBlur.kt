package com.jianji.app.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun GlassBlurBox(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    shape: RoundedCornerShape = LiquidGlassShapes.card,
    content: @Composable BoxScope.() -> Unit
) {
    val blurEffect = remember(blurRadius.value) {
        BlurEffect(
            radiusX = blurRadius.value,
            radiusY = blurRadius.value,
            edgeTreatment = TileMode.Decal
        ).asComposeRenderEffect()
    }

    Box(
        modifier = modifier
            .clip(shape)
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    renderEffect = blurEffect
                }
            },
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Dp = BlurRadius.medium,
    shape: RoundedCornerShape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    elevation: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        elevation = elevation,
        content = content
    )
}

val supportsRenderEffect: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S