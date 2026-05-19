/**
 * 主 Activity
 * 
 * 作用：应用的入口界面，包含底部导航和页面切换
 */
package com.jianji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.jianji.app.ui.report.ReportScreen
import com.jianji.app.ui.report.ReportViewModel

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 创建数据库实例
        val database = AppDatabase.getDatabase(this)
        val transactionDao = database.transactionDao()
        val repository = TransactionRepository(transactionDao)
        
        // 设置 Compose 内容
        setContent {
            MaterialTheme {
                JianJiApp(repository)
            }
        }
    }
}

/**
 * 简记应用主界面
 */
@Composable
fun JianJiApp(repository: TransactionRepository) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
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
                PlaceholderScreen("我的")
            }
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {
    val items = listOf(
        Triple(Screen.Home, Icons.Default.Home, "首页"),
        Triple(Screen.Record, Icons.Default.AddCircle, "记账"),
        Triple(Screen.Report, Icons.Default.BarChart, "报表"),
        Triple(Screen.Profile, Icons.Default.Person, "我的")
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentDestination?.hierarchy?.any { 
                    it.route == screen.route 
                } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * 页面定义
 */
sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "首页")
    data object Record : Screen("record", "记账")
    data object Report : Screen("report", "报表")
    data object Profile : Screen("profile", "我的")
}

/**
 * 占位页面
 */
@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$name 页面开发中...", fontSize = 18.sp)
    }
}
