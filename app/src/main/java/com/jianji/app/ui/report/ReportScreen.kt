/**
 * 报表页面 UI
 * 
 * 作用：显示收支统计报表
 */
package com.jianji.app.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 报表页面
 * 
 * @param viewModel 报表 ViewModel
 */
@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val totalExpense = viewModel.getTotalExpense()
    val totalIncome = viewModel.getTotalIncome()
    val expenseByCategory = viewModel.getExpenseByCategory()
    val incomeByCategory = viewModel.getIncomeByCategory()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "报表",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { viewModel.setPeriod(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SummaryCard(
            totalExpense = totalExpense,
            totalIncome = totalIncome
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (expenseByCategory.isNotEmpty()) {
            CategoryBreakdown(
                title = "支出分类",
                data = expenseByCategory,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (incomeByCategory.isNotEmpty()) {
            CategoryBreakdown(
                title = "收入分类",
                data = incomeByCategory,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (expenseByCategory.isEmpty() && incomeByCategory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 时间段选择器
 */
@Composable
fun PeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodButton(
            text = "本周",
            isSelected = selectedPeriod == "week",
            onClick = { onPeriodSelected("week") },
            modifier = Modifier.weight(1f)
        )
        PeriodButton(
            text = "本月",
            isSelected = selectedPeriod == "month",
            onClick = { onPeriodSelected("month") },
            modifier = Modifier.weight(1f)
        )
        PeriodButton(
            text = "本年",
            isSelected = selectedPeriod == "year",
            onClick = { onPeriodSelected("year") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 汇总卡片
 */
@Composable
fun SummaryCard(
    totalExpense: Double,
    totalIncome: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "总支出",
                    amount = totalExpense,
                    color = MaterialTheme.colorScheme.error
                )
                Divider(
                    modifier = Modifier
                        .height(60.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                )
                SummaryItem(
                    label = "总收入",
                    amount = totalIncome,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    amount: Double,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¥${String.format("%.2f", amount)}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 分类明细
 */
@Composable
fun CategoryBreakdown(
    title: String,
    data: Map<String, Double>,
    color: Color
) {
    val total = data.values.sum()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        data.forEach { (category, amount) ->
            val percentage = if (total > 0) (amount / total * 100).toInt() else 0
            CategoryItem(
                category = category,
                amount = amount,
                percentage = percentage,
                color = color
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CategoryItem(
    category: String,
    amount: Double,
    percentage: Int,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "¥${String.format("%.2f", amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100f)
                        .height(8.dp)
                        .background(
                            color = color,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$percentage%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}