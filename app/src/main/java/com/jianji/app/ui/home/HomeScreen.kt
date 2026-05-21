package com.jianji.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M月d日 E", Locale.CHINESE) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        TodaySummaryCard(
            expense = dayExpense,
            income = dayIncome,
            dateText = selectedDate.format(dateFormatter),
            isToday = selectedDate == LocalDate.now(),
            onDateClick = { showDatePicker = true },
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (selectedDate == LocalDate.now()) "今日交易" else "${selectedDate.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE))}交易",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
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
private fun TodaySummaryCard(
    expense: Double,
    income: Double,
    dateText: String,
    isToday: Boolean,
    onDateClick: () -> Unit,
    hazeState: HazeState
) {
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
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(GlassColors.glassCardBackground.copy(alpha = 0.15f))
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = LiquidGlassShapes.card
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isToday) "今日收支" else "收支",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    color = GlassColors.iosBlue,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onDateClick() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        text = "¥${formatHomeAmount(expense)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassColors.expenseRed
                    )
                }

                if (income > 0) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "收入",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥${formatHomeAmount(income)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassColors.incomeGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = LiquidGlassShapes.medium,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.medium)
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
                shape = LiquidGlassShapes.medium
            )
            .clickable { onClick() }
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
                "-¥${formatHomeAmount(transaction.amount)}"
            } else {
                "+¥${formatHomeAmount(transaction.amount)}"
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

private fun formatHomeAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}
