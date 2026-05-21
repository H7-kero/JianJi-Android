package com.jianji.app.ui.record

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    saveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KeyRow(
            keys = listOf("7", "8", "9", "⌫"),
            onKeyPress = { key ->
                if (key == "⌫") onBackspace() else onKeyPress(key)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyRow(
            keys = listOf("4", "5", "6", "+"),
            onKeyPress = { key -> onKeyPress(key) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyRow(
            keys = listOf("1", "2", "3", "-"),
            onKeyPress = { key -> onKeyPress(key) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyRow(
            keys = listOf(".", "0", "*", "/"),
            onKeyPress = { key -> onKeyPress(key) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        SaveButtonRow(
            enabled = saveEnabled,
            onSave = onSave
        )
    }
}

@Composable
private fun KeyRow(
    keys: List<String>,
    onKeyPress: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { key ->
            CalculatorKey(
                label = key,
                onClick = { onKeyPress(key) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalculatorKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
        label = "key_scale"
    )

    val isOperator = label in listOf("+", "-", "*", "/")
    val isBackspace = label == "⌫"
    val isNumber = label in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")

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
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isBackspace) {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "删除",
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = label,
                fontSize = if (isOperator) 20.sp else 22.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(80)
            pressed = false
        }
    }
}

@Composable
private fun SaveButtonRow(
    enabled: Boolean,
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) GlassColors.iosBlue
                else GlassColors.iosBlue.copy(alpha = 0.25f)
            )
            .clickable(enabled = enabled) { onSave() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "保存",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f)
        )
    }
}

fun evaluateExpression(expression: String): Double {
    if (expression.isBlank()) return 0.0

    val cleaned = expression.replace("\\s+".toRegex(), "")
    if (cleaned.isEmpty()) return 0.0

    return try {
        evaluateArithmetic(cleaned)
    } catch (_: Exception) {
        cleaned.toDoubleOrNull() ?: 0.0
    }
}

private fun evaluateArithmetic(expr: String): Double {
    var index = 0

    fun parseFactor(): Double {
        if (index < expr.length && expr[index] == '-') {
            index++
            return -parseFactor()
        }
        val start = index
        var dotCount = 0
        while (index < expr.length && (expr[index].isDigit() || expr[index] == '.')) {
            if (expr[index] == '.') dotCount++
            if (dotCount > 1) break
            index++
        }
        return expr.substring(start, index).toDouble()
    }

    fun parseTerm(): Double {
        var result = parseFactor()
        while (index < expr.length) {
            when (expr[index]) {
                '*' -> { index++; result *= parseFactor() }
                '/' -> { index++; result /= parseFactor() }
                else -> break
            }
        }
        return result
    }

    fun parseExpression(): Double {
        var result = parseTerm()
        while (index < expr.length) {
            when (expr[index]) {
                '+' -> { index++; result += parseTerm() }
                '-' -> { index++; result -= parseTerm() }
                else -> break
            }
        }
        return result
    }

    return parseExpression()
}
