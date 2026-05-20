package com.jianji.app.ui.record

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes

@Composable
fun CalculatorKeyboard(
    expression: String,
    evaluatedValue: Double,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorKey(
                label = "1",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("1") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "2",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("2") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "3",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("3") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "+",
                isBackspace = false,
                isOperator = true,
                isNumber = false,
                onClick = { onKeyPress("+") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorKey(
                label = "4",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("4") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "5",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("5") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "6",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("6") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "−",
                isBackspace = false,
                isOperator = true,
                isNumber = false,
                onClick = { onKeyPress("-") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorKey(
                label = "7",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("7") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "8",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("8") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "9",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("9") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "·",
                isBackspace = false,
                isOperator = true,
                isNumber = false,
                onClick = { onKeyPress(".") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorKey(
                label = "C",
                isBackspace = true,
                isOperator = false,
                isNumber = false,
                onClick = { onBackspace() },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "0",
                isBackspace = false,
                isOperator = false,
                isNumber = true,
                onClick = { onKeyPress("0") },
                modifier = Modifier.weight(1f)
            )
            CalculatorKey(
                label = "",
                isBackspace = false,
                isOperator = false,
                isNumber = false,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            SaveButton(
                enabled = saveEnabled,
                onClick = { onSave() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalculatorKey(
    label: String,
    isBackspace: Boolean,
    isOperator: Boolean,
    isNumber: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "key_scale"
    )

    val bgColor = when {
        isBackspace -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        isOperator -> GlassColors.iosBlue.copy(alpha = 0.08f)
        isNumber -> GlassColors.glassSurface
        else -> GlassColors.glassSurface
    }

    val contentColor = when {
        isBackspace -> MaterialTheme.colorScheme.onSurfaceVariant
        isOperator -> GlassColors.iosBlue
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(LiquidGlassShapes.small)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (label.isEmpty()) return@Box

        if (isBackspace) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_delete),
                contentDescription = "删除",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) GlassColors.iosBlue
                else GlassColors.iosBlue.copy(alpha = 0.25f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "保存",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}