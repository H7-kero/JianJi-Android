/**
 * 记账页面 UI
 * 
 * 作用：提供记账功能的用户界面
 */
package com.jianji.app.ui.record

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 记账页面
 * 
 * @param viewModel 记账 ViewModel
 */
@Composable
fun RecordScreen(viewModel: RecordViewModel) {
    val transactionType by viewModel.transactionType.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState()
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "记一笔",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
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

        CategorySelector(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )

        if (availableSubCategories != null) {
            Spacer(modifier = Modifier.height(16.dp))
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
    }
}

/**
 * 类型选择器（支出/收入）
 */
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
            color = MaterialTheme.colorScheme.error,
            onClick = { onTypeSelected("expense") },
            modifier = Modifier.weight(1f)
        )
        TypeButton(
            text = "收入",
            isSelected = selectedType == "income",
            color = MaterialTheme.colorScheme.primary,
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
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 金额输入框
 */
@Composable
fun AmountInput(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "金额",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¥",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    placeholder = {
                        Text(
                            text = "0.00",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

/**
 * 分类选择器
 */
@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "选择分类",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
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
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { category ->
                    CategoryItem(
                        category = category,
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
fun CategoryItem(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * 子分类选择器
 */
@Composable
fun SubCategorySelector(
    subCategories: List<String>,
    selectedSubCategory: String?,
    onSubCategorySelected: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "选择子分类（可选）",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilterChip(
            selected = selectedSubCategory == null,
            onClick = { onSubCategorySelected(null) },
            label = { Text("不选择子分类") },
            leadingIcon = if (selectedSubCategory == null) {
                { Icon(Icons.Default.Check, contentDescription = null) }
            } else null
        )
        Spacer(modifier = Modifier.height(8.dp))
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
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { subCategory ->
                    SubCategoryItem(
                        subCategory = subCategory,
                        isSelected = selectedSubCategory == subCategory,
                        onClick = { onSubCategorySelected(subCategory) },
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
fun SubCategoryItem(
    subCategory: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subCategory,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * 备注输入框
 */
@Composable
fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "备注（可选）",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("添加备注...") },
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * 保存按钮
 */
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
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "保存",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}