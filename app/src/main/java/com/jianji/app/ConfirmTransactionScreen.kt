package com.jianji.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import kotlinx.coroutines.launch

@Composable
fun ConfirmTransactionScreen(
    type: String,
    amount: Double,
    channel: String,
    category: String,
    subCategory: String?,
    merchant: String?,
    repository: TransactionRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var selectedType by remember { mutableStateOf(type) }
    var selectedChannel by remember { mutableStateOf(channel) }
    var selectedCategory by remember { mutableStateOf(category) }
    var selectedSubCategory by remember { mutableStateOf(subCategory) }
    var showCategorySelector by remember { mutableStateOf(false) }
    var showSubCategorySelector by remember { mutableStateOf(false) }

    val expenseCategories = listOf("餐饮", "交通", "购物", "娱乐", "医疗", "教育", "居住", "其他")
    val incomeCategories = listOf("工资", "奖金", "投资", "兼职", "其他")

    val subCategories = mapOf(
        "餐饮" to listOf("早餐", "午餐", "晚餐", "宵夜", "饮料", "零食", "水果", "其他"),
        "交通" to listOf("充电", "停车费", "过路费", "地铁", "打车", "自行车", "其他")
    )

    val channels = listOf("微信", "支付宝", "京东", "其他")

    val categories = if (selectedType == "expense") expenseCategories else incomeCategories
    val availableSubCategories = subCategories[selectedCategory]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {},
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "确认记账",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "expense",
                        onClick = {
                            if (selectedType != "expense") {
                                selectedType = "expense"
                                selectedCategory = "其他"
                                selectedSubCategory = null
                            }
                        },
                        label = { Text("支出") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedType == "income",
                        onClick = {
                            if (selectedType != "income") {
                                selectedType = "income"
                                selectedCategory = "其他"
                                selectedSubCategory = null
                            }
                        },
                        label = { Text("收入") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "¥${formatAmount(amount)}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedType == "expense") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                if (merchant != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "收款方：$merchant",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (selectedType == "expense") {
                    Text(
                        text = "支付渠道",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        channels.forEach { ch ->
                            FilterChip(
                                selected = selectedChannel == ch,
                                onClick = { selectedChannel = ch },
                                label = { Text(ch, fontSize = 13.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "分类",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (showCategorySelector) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.chunked(4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = {
                                            selectedCategory = cat
                                            val defaults = mapOf("餐饮" to "早餐", "交通" to "充电")
                                            selectedSubCategory = defaults[cat]
                                            showCategorySelector = false
                                        },
                                        label = { Text(cat, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(4 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategorySelector = true }
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = selectedCategory,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "修改",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (availableSubCategories != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "子分类",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (showSubCategorySelector) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            availableSubCategories.chunked(4).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { sub ->
                                        FilterChip(
                                            selected = selectedSubCategory == sub,
                                            onClick = {
                                                selectedSubCategory = sub
                                                showSubCategorySelector = false
                                            },
                                            label = { Text(sub, fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(4 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSubCategorySelector = true }
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = selectedSubCategory ?: "未选择",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "修改",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消", fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val transaction = Transaction(
                                    amount = amount,
                                    category = selectedCategory,
                                    subCategory = selectedSubCategory,
                                    channel = if (selectedType == "expense") selectedChannel else null,
                                    type = selectedType,
                                    merchant = merchant,
                                    source = "auto"
                                )
                                repository.insertTransaction(transaction)
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}
