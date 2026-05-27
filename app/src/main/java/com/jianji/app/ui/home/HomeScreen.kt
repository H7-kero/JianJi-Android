package com.jianji.app.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.CategoryExpense
import com.jianji.app.data.model.Transaction
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import com.jianji.app.ui.theme.glassPressSpring
import com.jianji.app.util.formatAmount
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private val categoryEmojis = mapOf(
    "餐饮" to "🍜", "交通" to "🚗", "购物" to "🛍", "娱乐" to "🎮",
    "医疗" to "🏥", "教育" to "📚", "居住" to "🏠", "其他" to "⋯",
    "工资" to "💰", "奖金" to "🎁", "投资" to "📈", "兼职" to "💼"
)

private val categoryColors = mapOf(
    "餐饮" to Color(0xFFFF3B30).copy(alpha = 0.12f),
    "交通" to Color(0xFF007AFF).copy(alpha = 0.12f),
    "购物" to Color(0xFFFF9500).copy(alpha = 0.12f),
    "娱乐" to Color(0xFFAF52DE).copy(alpha = 0.12f),
    "医疗" to Color(0xFFFF2D55).copy(alpha = 0.12f),
    "教育" to Color(0xFF5856D6).copy(alpha = 0.12f),
    "居住" to Color(0xFF34C759).copy(alpha = 0.12f),
    "其他" to Color(0xFFE5E5EA).copy(alpha = 0.5f),
    "工资" to Color(0xFF34C759).copy(alpha = 0.12f),
    "奖金" to Color(0xFFFFCC00).copy(alpha = 0.12f),
    "投资" to Color(0xFF007AFF).copy(alpha = 0.12f),
    "兼职" to Color(0xFF34C759).copy(alpha = 0.12f)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    hazeState: HazeState
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayExpense by viewModel.dayExpense.collectAsState()
    val dayIncome by viewModel.dayIncome.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M月d日 E", Locale.CHINESE) }
    val shortDateFormatter = remember { DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE) }
    val today = remember { LocalDate.now() }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = selectedDate.format(dateFormatter),
                    fontSize = 14.sp,
                    color = GlassColors.iosBlue,
                    modifier = Modifier.clickable { showDatePicker = true }
                )
                Text(
                    text = "今日收支",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassColors.iosBlue.copy(alpha = 0.1f))
                    .clickable { showDatePicker = true },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📅", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExpenseIncomeCard(
            expense = dayExpense,
            income = dayIncome,
            monthlyBudget = monthlyBudget,
            monthlyExpense = monthlyExpense,
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(16.dp))

        CategoryOverviewCard(
            categoryExpenses = categoryExpenses,
            hazeState = hazeState,
            modifier = Modifier.clickable { }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (selectedDate == today) "今日交易" else "${selectedDate.format(shortDateFormatter)}交易",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无交易记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(transactions, key = { _, tx -> tx.id }) { _, transaction ->
                    TransactionItem(
                        transaction = transaction,
                        timeFormat = timeFormat,
                        hazeState = hazeState,
                        onClick = { viewModel.startEditing(transaction) }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerBottomSheet(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                viewModel.selectDate(date)
            },
            onDismiss = {
                showDatePicker = false
            },
            sheetState = datePickerState
        )
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun ExpenseIncomeCard(
    expense: Double,
    income: Double,
    monthlyBudget: Double,
    monthlyExpense: Double,
    hazeState: HazeState
) {
    val progress = if (monthlyBudget > 0) (monthlyExpense / monthlyBudget).coerceIn(0.0, 1.0) else 0.0
    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress"
    )
    val progressPercent = (progress * 100).toInt().coerceIn(0, 999)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = LiquidGlassShapes.large,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.large)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(GlassColors.glassCardBackground.copy(alpha = 0.15f))
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = LiquidGlassShapes.large
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "支出",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${formatAmount(expense)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassColors.expenseRed
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "收入",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${formatAmount(income)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassColors.incomeGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.04f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress.toFloat())
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GlassColors.expenseRed,
                                    Color(0xFFFF6B6B)
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "月预算 ¥${formatAmount(monthlyBudget)} · 已用 $progressPercent%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun CategoryOverviewCard(
    categoryExpenses: List<CategoryExpense>,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = LiquidGlassShapes.medium,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.medium)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(GlassColors.glassCardBackground.copy(alpha = 0.15f))
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = LiquidGlassShapes.medium
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "分类概览",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryExpenses.take(3).forEach { categoryExpense ->
                    CategoryChip(
                        category = categoryExpense.category,
                        amount = categoryExpense.total,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⋯", fontSize = 20.sp)
                        Text(
                            text = "全部",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "→",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassColors.iosBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    val bgColor = categoryColors[category] ?: Color.Black.copy(alpha = 0.04f)
    val emoji = categoryEmojis[category] ?: "⋯"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = category,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¥${formatAmount(amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun TransactionItem(
    transaction: Transaction,
    timeFormat: SimpleDateFormat,
    hazeState: HazeState,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = glassPressSpring,
        label = "tx_press"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale)
            .shadow(
                elevation = 2.dp,
                shape = LiquidGlassShapes.medium,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.medium)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(GlassColors.glassCardBackground.copy(alpha = 0.15f))
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = LiquidGlassShapes.medium
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val categoryText = buildString {
                    append(transaction.category)
                    if (transaction.subCategory != null) {
                        append(" · ${transaction.subCategory}")
                    }
                }
                Text(
                    text = categoryText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = timeFormat.format(Date(transaction.timestamp)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.channel != null) {
                        Text(
                            text = transaction.channel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (transaction.note.isNotEmpty()) {
                    Text(
                        text = transaction.note,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            val amountText = if (transaction.type == "expense") {
                "-¥${formatAmount(transaction.amount)}"
            } else {
                "+¥${formatAmount(transaction.amount)}"
            }

            Text(
                text = amountText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.type == "expense") {
                    GlassColors.expenseRed
                } else {
                    GlassColors.incomeGreen
                }
            )
        }
    }
}
