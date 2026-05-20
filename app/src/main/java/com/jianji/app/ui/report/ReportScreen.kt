package com.jianji.app.ui.report

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jianji.app.data.model.Transaction
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val daySummaries by viewModel.daySummaries.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()

    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf<DaySummary?>(null) }

    val today = Calendar.getInstance()

    val firstDayOfWeek: Int = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        cal.get(Calendar.DAY_OF_WEEK)
    }
    val daysInMonth: Int = remember(year, month) {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val blankCells: Int = remember(firstDayOfWeek) {
        if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "报表",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("本月支出", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "¥${formatAmount(monthlyExpense)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("本月收入", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "¥${formatAmount(monthlyIncome)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassColors.iosBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月", tint = MaterialTheme.colorScheme.onBackground)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${year}年",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { showYearPicker = true }
                )
                Text(
                    text = "${month}月",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { showMonthPicker = true }
                )
            }

            IconButton(onClick = { viewModel.goToNextMonth() }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "下个月", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val totalCells = blankCells + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        if (cellIndex < blankCells || cellIndex >= totalCells) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val dayNum = cellIndex - blankCells + 1
                            val summary = daySummaries.getOrNull(dayNum - 1)
                            val isToday = today.get(Calendar.YEAR) == year &&
                                    today.get(Calendar.MONTH) + 1 == month &&
                                    today.get(Calendar.DAY_OF_MONTH) == dayNum

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        if (summary != null && summary.transactions.isNotEmpty()) {
                                            selectedDay = summary
                                        }
                                    },
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (summary != null) {
                                        if (summary.expense > 0) {
                                            Text(
                                                text = "-${formatAmount(summary.expense)}",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.error,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 11.sp
                                            )
                                        }
                                        if (summary.income > 0) {
                                            Text(
                                                text = "+${formatAmount(summary.income)}",
                                                fontSize = 9.sp,
                                                color = GlassColors.iosBlue,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(84.dp))
    }

    if (showYearPicker) {
        YearPickerDialog(
            selectedYear = year,
            onYearSelected = {
                viewModel.selectYear(it)
                showYearPicker = false
            },
            onDismiss = { showYearPicker = false }
        )
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = month,
            onMonthSelected = {
                viewModel.selectMonth(it)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (selectedDay != null) {
        DayDetailDialog(
            year = year,
            month = month,
            daySummary = selectedDay!!,
            onDismiss = { selectedDay = null }
        )
    }
}

@Composable
fun YearPickerDialog(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 10..currentYear + 1).toList()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(initialScale = 0.92f, animationSpec = tween(350)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(targetScale = 0.92f, animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.5f)
                    .shadow(elevation = 10.dp, shape = LiquidGlassShapes.card, clip = false, ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow)
                    .clip(LiquidGlassShapes.card)
                    .background(Brush.verticalGradient(colors = listOf(GlassColors.glassHighlight, GlassColors.glassCardBackground)))
                    .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.06f), shape = LiquidGlassShapes.card)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("选择年份", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(years) { year ->
                            val isSelected = year == selectedYear
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(LiquidGlassShapes.small).background(if (isSelected) GlassColors.iosBlue.copy(alpha = 0.08f) else Color.Transparent).clickable { onYearSelected(year) }.padding(14.dp)
                            ) {
                                Text("${year}年", fontSize = 17.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) GlassColors.iosBlue else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthPickerDialog(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(initialScale = 0.92f, animationSpec = tween(350)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(targetScale = 0.92f, animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .shadow(elevation = 10.dp, shape = LiquidGlassShapes.card, clip = false, ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow)
                    .clip(LiquidGlassShapes.card)
                    .background(Brush.verticalGradient(colors = listOf(GlassColors.glassHighlight, GlassColors.glassCardBackground)))
                    .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.06f), shape = LiquidGlassShapes.card)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("选择月份", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    val rows = months.chunked(4)
                    rows.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { monthName ->
                                val monthNum = months.indexOf(monthName) + 1
                                val isSelected = monthNum == selectedMonth
                                Box(modifier = Modifier.weight(1f).clip(LiquidGlassShapes.small).background(if (isSelected) GlassColors.iosBlue.copy(alpha = 0.10f) else GlassColors.glassSurfaceVariant).clickable { onMonthSelected(monthNum) }.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                                    Text(monthName, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) GlassColors.iosBlue else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DayDetailDialog(year: Int, month: Int, daySummary: DaySummary, onDismiss: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(initialScale = 0.92f, animationSpec = tween(350)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(targetScale = 0.92f, animationSpec = tween(250)) + fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.65f)
                    .shadow(elevation = 10.dp, shape = LiquidGlassShapes.card, clip = false, ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow)
                    .clip(LiquidGlassShapes.card)
                    .background(Brush.verticalGradient(colors = listOf(GlassColors.glassHighlight, GlassColors.glassCardBackground)))
                    .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.06f), shape = LiquidGlassShapes.card)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${year}年${month}月${daySummary.day}日", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        TextButton(onClick = onDismiss) { Text("关闭") }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        if (daySummary.expense > 0) {
                            item {
                                Text("支出 ¥${formatAmount(daySummary.expense)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            val expenseTxs = daySummary.transactions.filter { it.type == "expense" }
                            items(expenseTxs) { tx -> TransactionDetailItem(transaction = tx, timeFormat = timeFormat) }
                            if (daySummary.income > 0) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                        if (daySummary.income > 0) {
                            item {
                                Text("收入 ¥${formatAmount(daySummary.income)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlassColors.iosBlue)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            val incomeTxs = daySummary.transactions.filter { it.type == "income" }
                            items(incomeTxs) { tx -> TransactionDetailItem(transaction = tx, timeFormat = timeFormat) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDetailItem(transaction: Transaction, timeFormat: SimpleDateFormat) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .shadow(elevation = 2.dp, shape = LiquidGlassShapes.small, clip = false, ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow)
            .clip(LiquidGlassShapes.small)
            .background(Brush.verticalGradient(colors = listOf(GlassColors.glassHighlight, GlassColors.glassSurfaceVariant)))
            .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.06f), shape = LiquidGlassShapes.small)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                val desc = buildString { append(transaction.category); if (transaction.subCategory != null) { append(" · ${transaction.subCategory}") } }
                Text(desc, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (transaction.channel != null) { Text(transaction.channel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text(timeFormat.format(Date(transaction.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (transaction.note.isNotEmpty()) { Text(transaction.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(if (transaction.type == "expense") "-¥${formatAmount(transaction.amount)}" else "+¥${formatAmount(transaction.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (transaction.type == "expense") MaterialTheme.colorScheme.error else GlassColors.iosBlue)
        }
    }
}