package com.jianji.app.ui.record

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jianji.app.ui.theme.GlassColors

val categoryEmojiMap = mapOf(
    "餐饮" to "\uD83C\uDF5C",
    "交通" to "\uD83D\uDE97",
    "购物" to "\uD83D\uDECD\uFE0F",
    "娱乐" to "\uD83C\uDFAE",
    "医疗" to "\uD83D\uDC8A",
    "教育" to "\uD83D\uDCDA",
    "居住" to "\uD83C\uDFE0",
    "其他" to "\uD83D\uDCB0",
    "工资" to "\uD83D\uDCB0",
    "奖金" to "\uD83C\uDF81",
    "投资" to "\uD83D\uDCC8",
    "兼职" to "\uD83D\uDCBC"
)

@Composable
fun CategoryPickerDialog(
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)) +
                    fadeIn(animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
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
                                    onCategorySelected(category)
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "${categoryEmojiMap[category] ?: "\uD83D\uDCC2"}  $category",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onDismiss) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}