package com.jianji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jianji.app.data.local.AppDatabase
import com.jianji.app.data.repository.TransactionRepository
import com.jianji.app.ui.home.HomeScreen
import com.jianji.app.ui.home.HomeViewModel
import com.jianji.app.ui.record.RecordScreen
import com.jianji.app.ui.record.RecordViewModel
import com.jianji.app.ui.profile.ProfileScreen
import com.jianji.app.ui.report.ReportScreen
import com.jianji.app.ui.report.ReportViewModel
import com.jianji.app.ui.theme.GlassColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this)
        val transactionDao = database.transactionDao()
        val repository = TransactionRepository(transactionDao)

        setContent {
            MaterialTheme {
                JianJiApp(repository)
            }
        }
    }
}

@Composable
fun JianJiApp(repository: TransactionRepository) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        bottomBar = {
            FloatingGlassNavBar(navController)
        },
        containerColor = GlassColors.glassBackground
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(300, delayMillis = 100)
                ) + slideInHorizontally(
                    initialOffsetX = { it / 8 },
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            targetOffsetX = { -it / 8 },
                            animationSpec = tween(250)
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) +
                        slideInHorizontally(
                            initialOffsetX = { -it / 8 },
                            animationSpec = tween(300)
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) +
                        slideOutHorizontally(
                            targetOffsetX = { it / 8 },
                            animationSpec = tween(300)
                        )
            }
        ) {
            composable(Screen.Home.route) {
                val viewModel = HomeViewModel(repository)
                HomeScreen(viewModel)
            }
            composable(Screen.Record.route) {
                val viewModel = RecordViewModel(repository)
                RecordScreen(viewModel)
            }
            composable(Screen.Report.route) {
                val viewModel = ReportViewModel(repository)
                ReportScreen(viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun FloatingGlassNavBar(navController: androidx.navigation.NavController) {
    val items = listOf(
        NavItem(Screen.Home, Icons.Default.Home, "首页"),
        NavItem(Screen.Record, Icons.Default.AddCircle, "记账"),
        NavItem(Screen.Report, Icons.Default.BarChart, "报表"),
        NavItem(Screen.Profile, Icons.Default.Person, "我的")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(GlassColors.glassNavBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any {
                    it.route == item.screen.route
                } == true

                val indicatorWidth by animateFloatAsState(
                    targetValue = if (selected) 1f else 0f,
                    animationSpec = tween(300),
                    label = "nav_indicator"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) Color.Black.copy(alpha = 0.06f)
                            else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = if (selected) 14.dp else 6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavItem(
    val screen: Screen,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "首页")
    data object Record : Screen("record", "记账")
    data object Report : Screen("report", "报表")
    data object Profile : Screen("profile", "我的")
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$name 页面开发中...", fontSize = 18.sp)
    }
}
