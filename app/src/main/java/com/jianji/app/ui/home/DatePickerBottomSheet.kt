package com.jianji.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var innerSelectedDay by remember { mutableStateOf(selectedDate.dayOfMonth) }

    val today = LocalDate.now()
    val currentYear = today.year
    val years = remember { (currentYear - 10..currentYear).toList() }
    val months = remember { (1..12).toList() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = GlassColors.glassNavBackground.copy(alpha = 0.95f),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    currentYearMonth = currentYearMonth.minusMonths(1)
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上个月", tint = MaterialTheme.colorScheme.onBackground)
                }

                Text(
                    text = "${currentYearMonth.year}年${currentYearMonth.monthValue}月",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    enabled = !currentYearMonth.plusMonths(1).isAfter(YearMonth.from(today))
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "下个月",
                        tint = if (currentYearMonth.plusMonths(1).isAfter(YearMonth.from(today)))
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WheelPicker(
                    items = years,
                    selectedIndex = years.indexOf(currentYearMonth.year).coerceAtLeast(0),
                    onSelectedChange = { idx ->
                        val newYear = years[idx]
                        val maxMonth = if (newYear == today.year) today.monthValue else 12
                        val newMonth = currentYearMonth.monthValue.coerceAtMost(maxMonth)
                        currentYearMonth = YearMonth.of(newYear, newMonth)
                    },
                    modifier = Modifier.weight(1f),
                    label = { "${it}年" }
                )

                WheelPicker(
                    items = months,
                    selectedIndex = currentYearMonth.monthValue - 1,
                    onSelectedChange = { idx ->
                        val newMonth = idx + 1
                        val maxMonth = if (currentYearMonth.year == today.year) today.monthValue else 12
                        if (newMonth <= maxMonth) {
                            currentYearMonth = YearMonth.of(currentYearMonth.year, newMonth)
                        }
                    },
                    modifier = Modifier.weight(0.5f),
                    label = { "${it}月" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val weekDays = DayOfWeek.values().map {
                it.getDisplayName(TextStyle.NARROW, Locale.CHINESE)
            }
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

            CalendarGrid(
                yearMonth = currentYearMonth,
                selectedDay = innerSelectedDay,
                today = today,
                onDaySelected = { day ->
                    innerSelectedDay = day
                    val newDate = LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, day)
                    onDateSelected(newDate)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiquidGlassShapes.small)
                    .background(GlassColors.iosBlue.copy(alpha = 0.08f))
                    .clickable {
                        currentYearMonth = YearMonth.from(today)
                        innerSelectedDay = today.dayOfMonth
                        onDateSelected(today)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "今日",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassColors.iosBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDay: Int,
    today: LocalDate,
    onDaySelected: (Int) -> Unit
) {
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
    val daysInMonth = yearMonth.lengthOfMonth()
    val blankCells = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
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
                        val isToday = today.year == yearMonth.year &&
                                today.monthValue == yearMonth.monthValue &&
                                today.dayOfMonth == dayNum
                        val isSelected = dayNum == selectedDay
                        val isFuture = LocalDate.of(yearMonth.year, yearMonth.monthValue, dayNum).isAfter(today)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> GlassColors.iosBlue.copy(alpha = 0.12f)
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (!isFuture) Modifier.clickable { onDaySelected(dayNum) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 14.sp,
                                fontWeight = when {
                                    isSelected -> FontWeight.Bold
                                    isToday -> FontWeight.SemiBold
                                    else -> FontWeight.Normal
                                },
                                color = when {
                                    isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                    isSelected -> GlassColors.iosBlue
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
