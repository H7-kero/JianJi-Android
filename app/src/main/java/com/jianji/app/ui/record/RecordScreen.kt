package com.jianji.app.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.ui.theme.GlassColors

@Composable
fun RecordScreen(viewModel: RecordViewModel) {
    val transactionType by viewModel.transactionType.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val note by viewModel.note.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    val categories = if (transactionType == "expense") {
        viewModel.expenseCategories
    } else {
        viewModel.incomeCategories
    }

    val availableSubCategories = selectedCategory?.let {
        viewModel.subCategories[it]
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.resetForm()
            viewModel.resetSavedState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "记一笔",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        TypeSelector(
            selectedType = transactionType,
            onTypeSelected = { viewModel.setTransactionType(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AmountInput(
            amount = amount,
            onAmountChange = { viewModel.setAmount(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (transactionType == "expense") {
            ChannelSelector(
                channels = viewModel.channels,
                selectedChannel = selectedChannel,
                onChannelSelected = { viewModel.selectChannel(it) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        CategorySelector(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )

        if (availableSubCategories != null) {
            Spacer(modifier = Modifier.height(20.dp))
            SubCategorySelector(
                subCategories = availableSubCategories,
                selectedSubCategory = selectedSubCategory,
                onSubCategorySelected = { viewModel.selectSubCategory(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        NoteInput(
            note = note,
            onNoteChange = { viewModel.setNote(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        SaveButton(
            enabled = amount.isNotEmpty() && selectedCategory != null,
            onClick = { viewModel.saveTransaction() }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TypeButton(
            text = "支出",
            isSelected = selectedType == "expense",
            color = Color(0xFFFF3B30),
            onClick = { onTypeSelected("expense") },
            modifier = Modifier.weight(1f)
        )
        TypeButton(
            text = "收入",
            isSelected = selectedType == "income",
            color = Color(0xFF34C759),
            onClick = { onTypeSelected("income") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TypeButton(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.background(color.copy(alpha = 0.12f))
                } else {
                    Modifier.background(GlassColors.glassCardBackground)
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassColors.glassCardBackground)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "金额",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¥",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    placeholder = {
                        Text(
                            text = "0",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Composable
fun ChannelSelector(
    channels: List<String>,
    selectedChannel: String,
    onChannelSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "支付渠道",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        val rows = channels.chunked(4)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { channel ->
                        val isSelected = selectedChannel == channel
                        GlassChip(
                            label = channel,
                            isSelected = isSelected,
                            onClick = { onChannelSelected(channel) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "选择分类",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        CategoryGrid(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )
    }
}

@Composable
fun CategoryGrid(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    val rows = categories.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { category ->
                    GlassChip(
                        label = category,
                        isSelected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SubCategorySelector(
    subCategories: List<String>,
    selectedSubCategory: String?,
    onSubCategorySelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "选择子分类",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        SubCategoryGrid(
            subCategories = subCategories,
            selectedSubCategory = selectedSubCategory,
            onSubCategorySelected = onSubCategorySelected
        )
    }
}

@Composable
fun SubCategoryGrid(
    subCategories: List<String>,
    selectedSubCategory: String?,
    onSubCategorySelected: (String) -> Unit
) {
    val rows = subCategories.chunked(4)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { subCategory ->
                    GlassChip(
                        label = subCategory,
                        isSelected = selectedSubCategory == subCategory,
                        onClick = { onSubCategorySelected(subCategory) },
                        modifier = Modifier.weight(1f),
                        isSmall = true
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun GlassChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSmall: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                } else {
                    Modifier.background(GlassColors.glassCardBackground)
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = if (isSmall) 6.dp else 8.dp,
                vertical = if (isSmall) 8.dp else 10.dp
            )
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isSmall) 14.dp else 16.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = label,
                fontSize = if (isSmall) 12.sp else 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "备注（可选）",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassColors.glassCardBackground)
        ) {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("添加备注...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = "保存",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
