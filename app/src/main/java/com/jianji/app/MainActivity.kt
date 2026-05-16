/**
 * 主 Activity
 * 
 * 作用：应用的入口界面，包含底部导航和页面切换
 * 
 * @AndroidEntryPoint 标记这个 Activity 可以使用 Hilt 依赖注入
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
import com.jianji.app.ui.home.HomeScreen
import com.jianji.app.ui.home.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主 Activity
 * 
 * ComponentActivity 是新版 Android 推荐的 Activity 基类
 * 支持 Compose 和其他现代 Android 特性
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置 Compose 内容
        setContent {
            // 使用 Material 3 主题
            MaterialTheme {
                JianJiApp()
            }
        }
    }
}

/**
 * 简记应用主界面
 * 
 * 包含：
 * - 底部导航栏（首页、记账、报表、我的）
 * - 页面导航（使用 Jetpack Navigation）
 */
@Composable
fun JianJiApp() {
    // 创建导航控制器，管理页面跳转
    val navController = rememberNavController()

    // Scaffold 是 Material 3 提供的页面布局框架
    // 包含顶部栏、底部栏、内容区域等
    Scaffold(
        bottomBar = {
            // 底部导航栏
            BottomNavigationBar(navController)
        }
    ) { padding ->
        // 导航宿主，管理各个页面
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,  // 默认显示首页
            modifier = Modifier.padding(padding)   // 避开底部导航栏
        ) {
            // 首页
            composable(Screen.Home.route) {
                // 获取 HomeViewModel 实例（Hilt 自动注入）
                val viewModel: HomeViewModel = viewModel()
                HomeScreen(viewModel)
            }
            // 记账页（占位）
            composable(Screen.Record.route) {
                PlaceholderScreen("记账")
            }
            // 报表页（占位）
            composable(Screen.Report.route) {
                PlaceholderScreen("报表")
            }
            // 我的页（占位）
            composable(Screen.Profile.route) {
                PlaceholderScreen("我的")
            }
        }
    }
}

/**
 * 底部导航栏
 * 
 * @param navController 导航控制器，用于页面跳转
 */
@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {
    // 导航项列表：页面、图标、标签
    val items = listOf(
        Triple(Screen.Home, Icons.Default.Home, "首页"),
        Triple(Screen.Record, Icons.Default.AddCircle, "记账"),
        Triple(Screen.Report, Icons.Default.BarChart, "报表"),
        Triple(Screen.Profile, Icons.Default.Person, "我的")
    )

    // Material 3 底部导航栏组件
    NavigationBar {
        // 获取当前导航状态
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // 遍历所有导航项
        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                // 图标
                icon = { Icon(icon, contentDescription = label) },
                // 标签
                label = { Text(label) },
                // 是否选中：当前页面是否匹配
                selected = currentDestination?.hierarchy?.any { 
                    it.route == screen.route 
                } == true,
                // 点击事件
                onClick = {
                    navController.navigate(screen.route) {
                        // 弹出到起始页面，避免页面堆叠
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true  // 保存页面状态
                        }
                        launchSingleTop = true   // 避免重复创建页面
                        restoreState = true      // 恢复页面状态
                    }
                }
            )
        }
    }
}

/**
 * 页面定义
 * 
 * sealed class 表示密封类，所有子类都在这里定义
 * 用于定义导航页面的路由和标签
 */
sealed class Screen(val route: String, val label: String) {
    data object Home : Screen("home", "首页")
    data object Record : Screen("record", "记账")
    data object Report : Screen("report", "报表")
    data object Profile : Screen("profile", "我的")
}

/**
 * 占位页面
 * 
 * 用于显示尚未开发的页面
 * @param name 页面名称
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