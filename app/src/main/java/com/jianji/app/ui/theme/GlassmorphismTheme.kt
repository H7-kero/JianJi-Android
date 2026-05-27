package com.jianji.app.ui.theme

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

object GlassColors {
    val glassBackground = Color(0xFFF5F7FA)
    val glassNavBackground = Color(0xE0FFFFFF)
    val glassCardBackground = Color(0xB8FFFFFF)
    val glassSurface = Color(0xA0FFFFFF)
    val glassSurfaceVariant = Color(0x88FFFFFF)
    val glassHighlight = Color.White.copy(alpha = 0.55f)
    val glassShadow = Color.Black.copy(alpha = 0.08f)
    val iosBlue = Color(0xFF007AFF)
    val expenseRed = Color(0xFFFF3B30)
    val incomeGreen = Color(0xFF34C759)
}

object LiquidGlassShapes {
    val large = RoundedCornerShape(28.dp)
    val card = RoundedCornerShape(20.dp)
    val medium = RoundedCornerShape(16.dp)
    val small = RoundedCornerShape(12.dp)
    val circle = CircleShape
}

@Composable
internal fun rememberGlassShader(shape: Shape): Pair<RuntimeShader?, Float> {
    val shader = remember { LiquidGlassShader.createShader() }
    val cornerRadius = remember(shape) {
        when (shape) {
            LiquidGlassShapes.card -> 20f
            LiquidGlassShapes.large -> 28f
            LiquidGlassShapes.medium -> 16f
            LiquidGlassShapes.small -> 12f
            else -> 20f
        }
    }
    return shader to cornerRadius
}

@OptIn(ExperimentalHazeMaterialsApi::class)
internal fun Modifier.glassModifier(
    hazeState: HazeState,
    hazeStyle: HazeStyle,
    shape: Shape,
    elevation: Dp,
    borderAlpha: Float,
    shader: RuntimeShader? = null,
    cornerRadius: Float = 20f,
    refraction: Float = 0.3f,
    dispersion: Float = 0.3f,
    highlightIntensity: Float = 0.8f
): Modifier {
    return this
        .shadow(
            elevation = elevation, shape = shape, clip = false,
            ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow
        )
        .clip(shape)
        .hazeEffect(state = hazeState, style = hazeStyle)
        .then(
            if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Modifier.graphicsLayer {
                    LiquidGlassShader.applyShaderParams(
                        shader, size.width, size.height,
                        refraction = refraction,
                        dispersion = dispersion,
                        highlightIntensity = highlightIntensity,
                        cornerRadius = cornerRadius
                    )
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
            } else {
                Modifier
            }
        )
        .border(width = 0.5.dp, color = Color.White.copy(alpha = borderAlpha), shape = shape)
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: Shape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 2.dp,
    borderAlpha: Float = 0.5f,
    content: @Composable ColumnScope.() -> Unit
) {
    val (shader, cornerRadius) = rememberGlassShader(shape)
    val hazeStyle = HazeMaterials.ultraThin(containerColor.copy(alpha = 0.15f))

    ElevatedCard(
        modifier = modifier.glassModifier(
            hazeState = hazeState,
            hazeStyle = hazeStyle,
            shape = shape,
            elevation = elevation,
            borderAlpha = borderAlpha,
            shader = shader,
            cornerRadius = cornerRadius
        ),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Transparent,
            contentColor = if (contentColor == Color.Unspecified) contentColorFor(containerColor) else contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: Shape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    elevation: Dp = 2.dp,
    borderAlpha: Float = 0.5f,
    content: @Composable () -> Unit
) {
    val (shader, cornerRadius) = rememberGlassShader(shape)
    val hazeStyle = HazeMaterials.ultraThin(containerColor.copy(alpha = 0.15f))

    Box(
        modifier = modifier.glassModifier(
            hazeState = hazeState,
            hazeStyle = hazeStyle,
            shape = shape,
            elevation = elevation,
            borderAlpha = borderAlpha,
            shader = shader,
            cornerRadius = cornerRadius
        )
    ) {
        content()
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: Shape = LiquidGlassShapes.medium,
    containerColor: Color = GlassColors.glassSurface,
    elevation: Dp = 1.dp,
    borderAlpha: Float = 0.4f,
    content: @Composable () -> Unit
) {
    val (shader, cornerRadius) = rememberGlassShader(shape)
    val hazeStyle = HazeMaterials.thin(containerColor.copy(alpha = 0.1f))

    Box(
        modifier = modifier.glassModifier(
            hazeState = hazeState,
            hazeStyle = hazeStyle,
            shape = shape,
            elevation = elevation,
            borderAlpha = borderAlpha,
            shader = shader,
            cornerRadius = cornerRadius,
            refraction = 0.15f,
            dispersion = 0.15f,
            highlightIntensity = 0.5f
        )
    ) {
        content()
    }
}
