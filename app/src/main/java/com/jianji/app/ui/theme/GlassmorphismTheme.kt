package com.jianji.app.ui.theme

import android.graphics.RenderEffect
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

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: Shape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 2.dp,
    borderAlpha: Float = 0.06f,
    cardBody: @Composable ColumnScope.() -> Unit
) {
    val shader = remember { LiquidGlassShader.createShader() }
    val cornerRadiusPx = remember(shape) {
        when (shape) {
            LiquidGlassShapes.card -> 20f
            LiquidGlassShapes.large -> 28f
            LiquidGlassShapes.medium -> 16f
            LiquidGlassShapes.small -> 12f
            else -> 20f
        }
    }

    ElevatedCard(
        modifier = modifier
            .shadow(
                elevation = elevation, shape = shape, clip = false,
                ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow
            )
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(containerColor.copy(alpha = 0.15f))
            )
            .then(
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier.graphicsLayer {
                        LiquidGlassShader.applyShaderParams(
                            shader, size.width, size.height,
                            cornerRadius = cornerRadiusPx
                        )
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier
                }
            )
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.5f), shape = shape),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Transparent,
            contentColor = if (contentColor == Color.Unspecified) contentColorFor(containerColor) else contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        content = {
            val scope = this
            scope.cardBody()
        }
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
    borderAlpha: Float = 0.06f,
    content: @Composable () -> Unit
) {
    val shader = remember { LiquidGlassShader.createShader() }
    val cornerRadiusPx = remember(shape) {
        when (shape) {
            LiquidGlassShapes.card -> 20f
            LiquidGlassShapes.large -> 28f
            LiquidGlassShapes.medium -> 16f
            LiquidGlassShapes.small -> 12f
            else -> 20f
        }
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation, shape = shape, clip = false,
                ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow
            )
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(containerColor.copy(alpha = 0.15f))
            )
            .then(
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier.graphicsLayer {
                        LiquidGlassShader.applyShaderParams(
                            shader, size.width, size.height,
                            cornerRadius = cornerRadiusPx
                        )
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier.background(containerColor)
                }
            )
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.5f), shape = shape)
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
    borderAlpha: Float = 0.05f,
    content: @Composable () -> Unit
) {
    val shader = remember { LiquidGlassShader.createShader() }
    val cornerRadiusPx = remember(shape) {
        when (shape) {
            LiquidGlassShapes.card -> 20f
            LiquidGlassShapes.large -> 28f
            LiquidGlassShapes.medium -> 16f
            LiquidGlassShapes.small -> 12f
            else -> 16f
        }
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation, shape = shape, clip = false,
                ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow
            )
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.thin(containerColor.copy(alpha = 0.1f))
            )
            .then(
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier.graphicsLayer {
                        LiquidGlassShader.applyShaderParams(
                            shader, size.width, size.height,
                            refraction = 0.15f,
                            dispersion = 0.15f,
                            highlightIntensity = 0.5f,
                            cornerRadius = cornerRadiusPx
                        )
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier.background(containerColor)
                }
            )
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.4f), shape = shape)
    ) {
        content()
    }
}
