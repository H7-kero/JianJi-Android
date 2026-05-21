package com.jianji.app.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = LiquidGlassShapes.card,
                    clip = false,
                    ambientColor = GlassColors.glassShadow,
                    spotColor = GlassColors.glassShadow
                )
                .clip(LiquidGlassShapes.card)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GlassColors.glassHighlight,
                            GlassColors.glassCardBackground
                        )
                    )
                )
                .border(
                    width = 0.5.dp,
                    color = Color.Black.copy(alpha = 0.06f),
                    shape = LiquidGlassShapes.card
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "报表功能开发中",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "即将支持图表分析和趋势报告",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
