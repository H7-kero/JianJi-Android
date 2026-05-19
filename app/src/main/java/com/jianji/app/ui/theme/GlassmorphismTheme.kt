package com.jianji.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassColors {
    val glassSurface = Color(0xFFFFFFFF)
    val glassSurfaceVariant = Color(0xFFF7F7F9)
    val glassBackground = Color(0xFFF0F0F5)
    val glassBorder = Color(0x1A000000)
    val glassShadow = Color(0x0D000000)
    val glassCardBackground = Color(0xE6FFFFFF)
    val glassNavBackground = Color(0xE6F8F8FA)
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = GlassColors.glassCardBackground,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 1.dp,
    borderAlpha: Float = 0.06f,
    content: @Composable ColumnScope.() -> Unit
) {
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
        content = content
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = GlassColors.glassCardBackground,
    elevation: Dp = 1.dp,
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
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = borderAlpha),
                shape = shape
            )
            .background(containerColor)
    ) {
        content()
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = GlassColors.glassCardBackground,
    elevation: Dp = 0.5.dp,
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
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = borderAlpha),
                shape = shape
            )
            .background(containerColor)
    ) {
        content()
    }
}
