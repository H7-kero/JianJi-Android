package com.jianji.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val todayExpense by viewModel.todayExpense.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val transactions by viewModel.todayTransactions.collectAsState()

    var hasLoaded by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        hasLoaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp)
    ) {
        Text(
            text = "简记",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        TodaySummaryCard(expense = todayExpense, income = todayIncome)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "今日交易",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "今天还没有交易记录",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(transactions, key = { _, tx -> tx.id }) { _, tx ->
                    TransactionItem(transaction = tx, timeFormat = timeFormat)
                }
            }
        }
    }
}

@Composable
fun TodaySummaryCard(expense: Double, income: Double) {
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
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "今日支出",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "¥${formatHomeAmount(expense)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(54.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "今日收入",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "¥${formatHomeAmount(income)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassColors.iosBlue
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, timeFormat: SimpleDateFormat) {
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    MaterialTheme.colorScheme.error
                } else {
                    GlassColors.iosBlue
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