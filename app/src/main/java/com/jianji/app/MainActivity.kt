package com.jianji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.repository.TransactionRepository
import com.jianji.app.ui.home.HomeScreen
import com.jianji.app.ui.home.HomeViewModel
import com.jianji.app.ui.profile.FabPreferences
import com.jianji.app.ui.profile.ProfileScreen
import com.jianji.app.ui.record.EditTransactionSheet
import com.jianji.app.ui.record.RecordBottomSheet
import com.jianji.app.ui.record.RecordViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jianji.app.ui.report.ReportScreen
import com.jianji.app.ui.report.ReportViewModel
import com.jianji.app.ui.theme.BlurRadius
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import com.jianji.app.ui.theme.iOSColorSpec
import com.jianji.app.ui.theme.iosSpring
import com.jianji.app.ui.theme.iosSnappy
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = applicationContext as JianJiApplication
        val database = app.database
        val transactionDao = database.transactionDao()
        val repository = TransactionRepository(transactionDao, applicationContext)

        setContent {
            MaterialTheme {
                JianJiApp(repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JianJiApp(repository: TransactionRepository) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val homeViewModel: HomeViewModel = viewModel { HomeViewModel(repository) }
    val reportViewModel: ReportViewModel = viewModel { ReportViewModel(repository) }
    val recordViewModel: RecordViewModel = viewModel { RecordViewModel(repository) }

    val hazeState = remember { HazeState() }
    var showEditSheet by remember { mutableStateOf(false) }
    val editingTransaction by homeViewModel.editingTransaction.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRecordSheet by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val fabVisible = pagerState.currentPage != 2

    val fabPosition by FabPreferences
        .getFabPosition(context)
        .collectAsState(initial = "left")

    val navBarHeight = 56

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .navigationBarsPadding()
        ) { page ->
            when (page) {
                0 -> HomeScreen(homeViewModel, hazeState)
                1 -> ReportScreen(reportViewModel)
                2 -> ProfileScreen()
            }
        }

        FloatingGlassNavBar(
            currentPage = pagerState.currentPage,
            onPageSelected = { page ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(page)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 10.dp)
        )

        AnimatedVisibility(
            visible = fabVisible,
            enter = scaleIn(animationSpec = iosSpring) +
                    fadeIn(animationSpec = tween(200)),
            exit = scaleOut(animationSpec = iosSnappy) +
                    fadeOut(animationSpec = tween(150)),
            modifier = Modifier
                .align(if (fabPosition == "left") Alignment.BottomStart else Alignment.BottomEnd)
                .padding(
                    start = if (fabPosition == "left") 48.dp else 0.dp,
                    end = if (fabPosition == "right") 48.dp else 0.dp,
                    bottom = (navBarHeight + 94).dp
                )
        ) {
            FAB(onClick = { showRecordSheet = true })
        }
    }

    if (showRecordSheet) {
        RecordBottomSheet(
            viewModel = recordViewModel,
            onDismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    showRecordSheet = false
                }
            },
            sheetState = sheetState
        )
    }

    LaunchedEffect(editingTransaction) {
        if (editingTransaction != null && !showEditSheet) {
            recordViewModel.loadTransaction(editingTransaction!!)
            showEditSheet = true
        }
    }

    if (showEditSheet && editingTransaction != null) {
        val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        EditTransactionSheet(
            transaction = editingTransaction!!,
            viewModel = recordViewModel,
            onDismiss = {
                showEditSheet = false
                homeViewModel.stopEditing()
            },
            sheetState = editSheetState
        )
    }
}

@Composable
private fun FAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = LiquidGlassShapes.circle,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.circle)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassColors.glassHighlight,
                        GlassColors.glassNavBackground
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = 0.06f),
                shape = LiquidGlassShapes.circle
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "记账",
            tint = GlassColors.iosBlue,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun FloatingGlassNavBar(
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(0, Icons.Default.Home, "首页"),
        Triple(1, Icons.Default.BarChart, "报表"),
        Triple(2, Icons.Default.Person, "我的")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .shadow(
                elevation = 18.dp,
                shape = LiquidGlassShapes.large,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.large)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassColors.glassHighlight,
                        GlassColors.glassNavBackground
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = 0.06f),
                shape = LiquidGlassShapes.large
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            val density = LocalDensity.current
            var rowSize by remember { mutableStateOf(IntSize.Zero) }

            val tabPx = if (rowSize.width > 0) rowSize.width.toFloat() / 3f else 0f
            val indicatorTargetPx = currentPage * tabPx
            val animatedOffset by animateFloatAsState(
                targetValue = indicatorTargetPx,
                animationSpec = iosSpring,
                label = "nav_indicator"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { rowSize = it }
            ) {
                items.forEach { (page, icon, label) ->
                    val selected = currentPage == page

                    val iconTint by animateColorAsState(
                        targetValue = if (selected) GlassColors.iosBlue
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        animationSpec = iOSColorSpec,
                        label = "nav_icon"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (selected) GlassColors.iosBlue
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        animationSpec = iOSColorSpec,
                        label = "nav_text"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onPageSelected(page) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = iconTint,
                                modifier = Modifier.size(if (selected) 22.dp else 20.dp)
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            if (rowSize.width > 0) {
                val indicatorWidth = with(density) { tabPx.toDp() }
                val indicatorHeight = with(density) { rowSize.height.toDp() }
                val indicatorShape = when (currentPage) {
                    0 -> RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 12.dp, bottomEnd = 12.dp)
                    2 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 20.dp, bottomEnd = 20.dp)
                    else -> RoundedCornerShape(12.dp)
                }
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                        .size(width = indicatorWidth, height = indicatorHeight)
                        .clip(indicatorShape)
                        .background(Color.Black.copy(alpha = 0.06f))
                )
            }
        }
    }
}
