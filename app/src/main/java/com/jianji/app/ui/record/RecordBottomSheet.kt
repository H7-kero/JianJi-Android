package com.jianji.app.ui.record

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import com.jianji.app.ui.theme.iosSpring

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordBottomSheet(
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

    var expression by remember { mutableStateOf("") }
    val evaluatedValue = remember(expression) { evaluateExpression(expression) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            expression = ""
            viewModel.resetForm()
            viewModel.resetSavedState()
            onDismiss()
        }
    }

    val amountFormatted = formatAmount(evaluatedValue)
    val saveEnabled = evaluatedValue > 0.0 && selectedCategory != null

    fun handleKeyPress(key: String) {
        expression += key
    }

    fun handleBackspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }
    }

    fun handleSave() {
        viewModel.setAmount(amountFormatted)
        viewModel.saveTransaction()
    }

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
                AnimatedVisibility(
                    visible = animationStarted,
                    enter = fadeIn(animationSpec = tween(350, delayMillis = 0)) +
                            slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            )
                ) {
                    Text(
                        text = "记一笔",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                AnimatedVisibility(
                    visible = animationStarted,
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 40)) +
                            scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f))
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(350, delayMillis = 60)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
            ) {
                TypeToggle(
                    selectedType = transactionType,
                    onTypeSelected = { viewModel.setTransactionType(it) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(350, delayMillis = 100)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (expression.isEmpty()) "¥ 0" else "¥ $amountFormatted",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    if (expression.isNotEmpty()) {
                        Text(
                            text = expression,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(350, delayMillis = 140)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
            ) {
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
                            .height(48.dp),
                        placeholder = { Text("添加备注...", fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(350, delayMillis = 200)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
            ) {
                CalculatorKeyboard(
                    expression = expression,
                    evaluatedValue = evaluatedValue,
                    onKeyPress = ::handleKeyPress,
                    onBackspace = ::handleBackspace,
                    onSave = ::handleSave,
                    saveEnabled = saveEnabled
                )
            }
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

@Composable
private fun TypeToggle(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TypeChip(
            text = "支出",
            isSelected = selectedType == "expense",
            selectedColor = Color(0xFFFF3B30),
            onClick = { onTypeSelected("expense") },
            modifier = Modifier.weight(1f)
        )
        TypeChip(
            text = "收入",
            isSelected = selectedType == "income",
            selectedColor = Color(0xFF34C759),
            onClick = { onTypeSelected("income") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TypeChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor.copy(alpha = 0.12f)
        else GlassColors.glassCardBackground,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "type_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "type_text"
    )

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(LiquidGlassShapes.small)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun CategorySelectorRow(
    selectedCategory: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LiquidGlassShapes.small)
            .background(GlassColors.glassCardBackground)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "分类",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (selectedCategory != null) {
                Text(
                    text = "${categoryEmojiMap[selectedCategory] ?: ""} $selectedCategory",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlassColors.iosBlue
                )
            } else {
                Text(
                    text = "点击选择",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        Text(
            text = "▸",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubCategoryChipRow(
    subCategories: List<String>,
    selectedSubCategory: String?,
    onSubCategorySelected: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subCategories) { subCategory ->
            SelectableChip(
                label = subCategory,
                isSelected = selectedSubCategory == subCategory,
                onClick = {
                    onSubCategorySelected(if (selectedSubCategory == subCategory) null else subCategory)
                }
            )
        }
    }
}

@Composable
private fun ChannelChipRow(
    channels: List<String>,
    selectedChannel: String,
    onChannelSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels) { channel ->
            SelectableChip(
                label = channel,
                isSelected = selectedChannel == channel,
                onClick = { onChannelSelected(channel) }
            )
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) GlassColors.iosBlue.copy(alpha = 0.10f)
        else GlassColors.glassCardBackground,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "chip_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) GlassColors.iosBlue
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "chip_text"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

private fun formatAmount(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.2f", value)
    }
}