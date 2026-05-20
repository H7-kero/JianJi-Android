package com.jianji.app.ui.record

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes

@Composable
fun CategoryPickerDialog(
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                initialScale = 0.88f,
                transformOrigin = TransformOrigin(0.5f, 0.5f),
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(
                targetScale = 0.88f,
                transformOrigin = TransformOrigin(0.5f, 0.5f),
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .shadow(
                        elevation = 10.dp,
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
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "选择分类",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 420.dp)
                    ) {
                        items(categories) { category ->
                            val bgColor by animateColorAsState(
                                targetValue = GlassColors.glassCardBackground,
                                animationSpec = tween(250),
                                label = "cat_bg"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(LiquidGlassShapes.small)
                                    .background(bgColor)
                                    .clickable { onCategorySelected(category) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "${categoryEmojiMap[category] ?: ""} $category",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}