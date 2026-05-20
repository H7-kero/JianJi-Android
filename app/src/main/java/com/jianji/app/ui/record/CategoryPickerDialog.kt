package com.jianji.app.ui.record

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jianji.app.ui.theme.GlassColors

val categoryEmojiMap = mapOf(
    "餐饮" to "🍜",
    "交通" to "🚗",
    "购物" to "🛍️",
    "娱乐" to "🎮",
    "医疗" to "💊",
    "教育" to "📚",
    "居住" to "🏠",
    "其他" to "💰",
    "工资" to "💰",
    "奖金" to "🎁",
    "投资" to "📈",
    "兼职" to "💼"
)

@Composable
fun CategoryPickerDialog(
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var internalVisible by remember { mutableStateOf(true) }
    var pendingCategory by remember { mutableStateOf<String?>(null) }

    fun triggerDismiss(category: String? = null) {
        pendingCategory = category
        internalVisible = false
    }

    Dialog(
        onDismissRequest = {
            triggerDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = internalVisible,
            enter = scaleIn(
                initialScale = 0.7f,
                transformOrigin = TransformOrigin(0.5f, 0.35f),
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f)
            ) + fadeIn(animationSpec = tween(200)),
            exit = scaleOut(
                targetScale = 0.7f,
                transformOrigin = TransformOrigin(0.5f, 0.35f),
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassColors.glassCardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "选择分类",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    categories.forEach { category ->
                        val bgColor = GlassColors.glassSurface

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .clickable {
                                    triggerDismiss(category)
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "${categoryEmojiMap[category] ?: "📂"}  $category",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = {
                        triggerDismiss()
                    }) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (!internalVisible) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(200)
                val category = pendingCategory
                if (category != null) {
                    onCategorySelected(category)
                } else {
                    onDismiss()
                }
            }
        }
    }
}