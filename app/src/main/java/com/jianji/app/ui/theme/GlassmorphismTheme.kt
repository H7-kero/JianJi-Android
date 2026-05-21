package com.jianji.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassColors {
    val glassBackground = Color(0xFFF5F7FA)
    val glassNavBackground = Color(0xE0FFFFFF)
    val glassCardBackground = Color(0xB8FFFFFF)
    val glassSurface = Color(0xA0FFFFFF)
    val glassSurfaceVariant = Color(0x88FFFFFF)
    val glassHighlight = Color.White.copy(alpha = 0.55f)
    val glassShadow = Color.Black.copy(alpha = 0.08f)
    val iosBlue = Color(0xFF007AFF)
    val separator = Color(0x1A000000)
}

object LiquidGlassShapes {
    val large = RoundedCornerShape(24.dp)
    val card = RoundedCornerShape(20.dp)
    val medium = RoundedCornerShape(16.dp)
    val small = RoundedCornerShape(12.dp)
    val circle = CircleShape
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 2.dp,
    borderAlpha: Float = 0.06f,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardContent: @Composable ColumnScope.() -> Unit = content
    ElevatedCard(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = borderAlpha),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = if (contentColor == Color.Unspecified) contentColorFor(containerColor) else contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        content = {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GlassColors.glassHighlight,
                                containerColor
                            )
                        )
                    )
            ) {
                cardContent()
            }
        }
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    elevation: Dp = 2.dp,
    borderAlpha: Float = 0.06f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassColors.glassHighlight,
                        containerColor
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = borderAlpha),
                shape = shape
            )
    ) {
        content()
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassShapes.medium,
    containerColor: Color = GlassColors.glassSurface,
    elevation: Dp = 1.dp,
    borderAlpha: Float = 0.05f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassColors.glassHighlight,
                        containerColor
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = borderAlpha),
                shape = shape
            )
    ) {
        content()
    }
}
