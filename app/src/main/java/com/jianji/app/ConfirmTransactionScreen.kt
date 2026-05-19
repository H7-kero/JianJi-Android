package com.jianji.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import com.jianji.app.ui.theme.GlassColors
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
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.Center)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(350)),
                exit = scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(250)
                ) + fadeOut(animationSpec = tween(200))
            ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassColors.glassCardBackground)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {}
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedType == "expense")
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                                else GlassColors.glassSurfaceVariant
                            )
                            .clickable {
                                if (selectedType != "expense") {
                                    selectedType = "expense"
                                    selectedCategory = "其他"
                                    selectedSubCategory = null
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "支出",
                            fontWeight = if (selectedType == "expense") FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedType == "expense") MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedType == "income")
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else GlassColors.glassSurfaceVariant
                            )
                            .clickable {
                                if (selectedType != "income") {
                                    selectedType = "income"
                                    selectedCategory = "其他"
                                    selectedSubCategory = null
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "收入",
                            fontWeight = if (selectedType == "income") FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedType == "income") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¥${formatConfirmAmount(amount)}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedType == "expense") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                if (merchant != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "收款方：$merchant",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedType == "expense") {
                    Text(
                        text = "支付渠道",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        channels.forEach { ch ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedChannel == ch) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        else GlassColors.glassSurfaceVariant
                                    )
                                    .clickable { selectedChannel = ch }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    ch,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedChannel == ch) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedChannel == ch) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                Text(
                    text = "分类",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (showCategorySelector) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.chunked(4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { cat ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (selectedCategory == cat)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                else GlassColors.glassSurfaceVariant
                                            )
                                            .clickable {
                                                selectedCategory = cat
                                                val defaults = mapOf("餐饮" to "早餐", "交通" to "充电")
                                                selectedSubCategory = defaults[cat]
                                                showCategorySelector = false
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            cat,
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedCategory == cat) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                repeat(4 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassColors.glassSurfaceVariant)
                            .clickable { showCategorySelector = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategory,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "修改",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (availableSubCategories != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "子分类",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (showSubCategorySelector) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSubCategories.chunked(4).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { sub ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (selectedSubCategory == sub)
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                    else GlassColors.glassSurfaceVariant
                                                )
                                                .clickable {
                                                    selectedSubCategory = sub
                                                    showSubCategorySelector = false
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                sub,
                                                fontSize = 12.sp,
                                                fontWeight = if (selectedSubCategory == sub) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedSubCategory == sub) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    repeat(4 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(GlassColors.glassSurfaceVariant)
                                .clickable { showSubCategorySelector = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedSubCategory ?: "未选择",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "修改",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassColors.glassSurfaceVariant)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("取消", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
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
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
            }
        }
    }
}

private fun formatConfirmAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}
