package com.jianji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.repository.TransactionRepository
import com.jianji.app.ui.home.HomeScreen
import com.jianji.app.ui.home.HomeViewModel
import com.jianji.app.ui.profile.FabPreferences
import com.jianji.app.ui.profile.ProfileScreen
import com.jianji.app.ui.record.RecordBottomSheet
import com.jianji.app.ui.record.RecordViewModel
import com.jianji.app.ui.report.ReportScreen
import com.jianji.app.ui.report.ReportViewModel
import com.jianji.app.ui.theme.GlassColors
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = com.jianji.app.data.local.AppDatabase.getDatabase(this)
        val transactionDao = database.transactionDao()
        val repository = TransactionRepository(transactionDao)

        setContent {
            MaterialTheme {
                JianJiApp(repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JianJiApp(repository: TransactionRepository) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val homeViewModel = remember { HomeViewModel(repository) }
    val reportViewModel = remember { ReportViewModel(repository) }
    val recordViewModel = remember { RecordViewModel(repository) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showRecordSheet by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val fabVisible = pagerState.currentPage != 2

    val fabPosition by FabPreferences
        .getFabPosition(context)
        .collectAsState(initial = "left")

    val navBarHeight = 72

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navBarHeight.dp)
                .navigationBarsPadding()
        ) { page ->
            when (page) {
                0 -> HomeScreen(homeViewModel)
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
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)) +
                    fadeIn(animationSpec = tween(200)),
            exit = scaleOut(animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)) +
                    fadeOut(animationSpec = tween(150)),
            modifier = Modifier
                .align(if (fabPosition == "left") Alignment.BottomStart else Alignment.BottomEnd)
                .padding(
                    start = if (fabPosition == "left") 24.dp else 0.dp,
                    end = if (fabPosition == "right") 24.dp else 0.dp,
                    bottom = (navBarHeight + 46).dp
                )
        ) {
            FAB(
                onClick = { showRecordSheet = true }
            )
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
}

@Composable
private fun FAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(CircleShape)
            .background(GlassColors.glassNavBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "记账",
            tint = MaterialTheme.colorScheme.primary,
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
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(GlassColors.glassNavBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { (page, icon, label) ->
                val selected = currentPage == page

                val capsuleBackground by animateColorAsState(
                    targetValue = if (selected) Color.Black.copy(alpha = 0.06f)
                    else Color.Transparent,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "nav_bg"
                )

                val iconTint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "nav_icon"
                )

                val textColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "nav_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(capsuleBackground)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPageSelected(page) }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconTint,
                            modifier = Modifier.size(if (selected) 24.dp else 22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}