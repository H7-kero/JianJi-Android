/**
 * 首页界面
 * 
 * 作用：显示今日支出、收入统计和最近交易列表
 * 
 * @Composable 标记这是一个 Compose UI 函数
 * Compose 是 Android 的现代 UI 工具包，用 Kotlin 代码描述界面
 */
package com.jianji.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * 首页界面
 * 
 * @param viewModel 首页 ViewModel，包含业务逻辑和数据
 */
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    // collectAsState() 收集 StateFlow 的状态
    // 当数据变化时，Compose 会自动重组（刷新）界面
    val todayExpense by viewModel.todayExpense.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val transactions by viewModel.todayTransactions.collectAsState()

    // Column 是垂直排列的容器
    Column(
        modifier = Modifier
            .fillMaxSize()           // 填满整个可用空间
            .padding(16.dp)          // 内边距 16dp
    ) {
        // 应用标题
        Text(
            text = "简记",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 今日统计卡片（支出和收入）
        TodaySummaryCard(
            expense = todayExpense,
            income = todayIncome
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 最近交易标题
        Text(
            text = "今日交易",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 交易列表
        if (transactions.isEmpty()) {
            // 没有交易记录时显示提示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无交易记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 使用 LazyColumn 显示列表（只渲染可见项，性能更好）
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // items 遍历交易列表，为每项创建 TransactionItem
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

/**
 * 今日统计卡片
 * 
 * 显示今日支出和收入的卡片
 * @param expense 今日支出金额
 * @param income 今日收入金额
 */
@Composable
fun TodaySummaryCard(expense: Double, income: Double) {
    // Card 是 Material 3 的卡片组件
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 支出部分
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "今日支出",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${String.format("%.2f", expense)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // 分隔线
            Divider(
                modifier = Modifier
                    .height(50.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )

            // 收入部分
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "今日收入",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥${String.format("%.2f", income)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 交易记录项
 * 
 * 显示单条交易的卡片
 * @param transaction 交易记录
 */
@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：分类和备注
            Column {
                Text(
                    text = transaction.category,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                if (transaction.note.isNotEmpty()) {
                    Text(
                        text = transaction.note,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右侧：金额
            val amountText = if (transaction.type == "expense") {
                "-¥${String.format("%.2f", transaction.amount)}"
            } else {
                "+¥${String.format("%.2f", transaction.amount)}"
            }
            
            Text(
                text = amountText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.type == "expense") {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}
