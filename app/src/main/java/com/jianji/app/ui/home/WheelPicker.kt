package com.jianji.app.ui.home

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun <T> WheelPicker(
    items: List<T>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    visibleItems: Int = 5,
    label: @Composable (T) -> String
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, selectedIndex - visibleItems / 2))
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    LaunchedEffect(selectedIndex) {
        val targetIndex = selectedIndex - visibleItems / 2
        if (listState.firstVisibleItemIndex != targetIndex || listState.firstVisibleScrollOffset > 5) {
            listState.animateScrollToItem(targetIndex, 0)
        }
    }

    val currentCenterIndex = remember {
        derivedStateOf {
            val centerOffset = listState.firstVisibleScrollOffset / itemHeightPx
            (listState.firstVisibleItemIndex + centerOffset).roundToInt() + visibleItems / 2
        }
    }

    LaunchedEffect(currentCenterIndex.value) {
        val idx = currentCenterIndex.value
        if (idx in items.indices && idx != selectedIndex) {
            onSelectedChange(idx)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }

    Box(modifier = modifier.height(itemHeight * visibleItems)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItems / 2))
        ) {
            items(items.size) { index ->
                val isSelected = index == currentCenterIndex.value
                val distanceFromCenter = abs(index - currentCenterIndex.value)
                val alpha = if (distanceFromCenter <= visibleItems / 2) {
                    1f - (distanceFromCenter.toFloat() / (visibleItems / 2 + 1))
                } else 0.2f
                val scale = if (isSelected) 1.15f else 0.85f + 0.15f * (1f - distanceFromCenter.toFloat() / (visibleItems / 2 + 1))

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.alpha = alpha
                            this.scaleX = scale
                            this.scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(items[index]),
                        fontSize = if (isSelected) 20.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .padding(horizontal = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIdx = currentCenterIndex.value
            if (centerIdx in items.indices) {
                coroutineScope.launch {
                    listState.animateScrollToItem(centerIdx - visibleItems / 2, 0)
                }
            }
        }
    }
}
