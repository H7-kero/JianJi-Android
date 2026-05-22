package com.jianji.app.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import com.jianji.app.util.formatAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    transaction: Transaction,
    viewModel: RecordViewModel,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val transactionType by viewModel.transactionType.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val note by viewModel.note.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    val categories = if (transactionType == "expense") {
        RecordViewModel.expenseCategories
    } else {
        RecordViewModel.incomeCategories
    }

    val availableSubCategories = selectedCategory?.let {
        RecordViewModel.subCategories[it]
    }

    var expression by remember { mutableStateOf(formatAmount(transaction.amount)) }
    val evaluatedValue = remember(expression) { evaluateExpression(expression) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.resetSavedState()
            onDismiss()
        }
    }

    val saveEnabled = evaluatedValue > 0.0 && selectedCategory != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = GlassColors.glassNavBackground.copy(alpha = 0.92f),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "修改记录",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiquidGlassShapes.small)
                    .background(GlassColors.glassSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "时间",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = timeFormat.format(Date(transaction.timestamp)),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TypeToggle(
                selectedType = transactionType,
                onTypeSelected = { viewModel.setTransactionType(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (expression.isEmpty()) "¥ 0" else "¥ ${formatAmount(evaluatedValue)}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (expression.isNotEmpty()) {
                    Text(
                        text = expression,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                CategorySelectorRow(
                    selectedCategory = selectedCategory,
                    onClick = { showCategoryPicker = true }
                )

                if (availableSubCategories != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SubCategoryChipRow(
                        subCategories = availableSubCategories,
                        selectedSubCategory = selectedSubCategory,
                        onSubCategorySelected = { viewModel.selectSubCategory(it) }
                    )
                }

                if (transactionType == "expense") {
                    Spacer(modifier = Modifier.height(8.dp))
                    ChannelChipRow(
                        channels = RecordViewModel.channels,
                        selectedChannel = selectedChannel,
                        onChannelSelected = { viewModel.selectChannel(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { viewModel.setNote(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    placeholder = { Text("添加备注...", fontSize = 14.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CalculatorKeyboard(
                expression = expression,
                evaluatedValue = evaluatedValue,
                onKeyPress = { expression += it },
                onBackspace = {
                    if (expression.isNotEmpty()) {
                        expression = expression.dropLast(1)
                    }
                },
                onSave = {
                    viewModel.setAmount(formatAmount(evaluatedValue))
                    viewModel.updateTransaction(transaction.id)
                },
                saveEnabled = saveEnabled
            )
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories,
            onCategorySelected = { category ->
                viewModel.selectCategory(category)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}
