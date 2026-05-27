# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

简记 (JianJi) — 个人记账 Android 应用，支持自动识别微信、支付宝、云闪付支付页面。

## 构建与运行

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 运行单元测试
./gradlew test

# 运行单个单元测试类
./gradlew test --tests "com.jianji.app.ui.home.HomeViewModelTest"

# 运行 Android 仪器测试（需要设备/模拟器）
./gradlew connectedAndroidTest

# 清理构建缓存
./gradlew clean
```

- Gradle 版本: 8.9.1（wrapper 内置）
- Kotlin: 2.2.0
- Java 工具链: 21
- compileSdk: 36, minSdk: 29

## 架构

MVVM + 分层架构，单模块 (`app`)。

```
com.jianji.app/
├── data/
│   ├── local/          # Room 数据库 (AppDatabase, TransactionDao)
│   ├── model/          # 数据实体 (Transaction, Category, Budget, CategoryExpense)
│   └── repository/     # TransactionRepository — 封装 DAO 和 SharedPreferences
├── service/            # AutoAccountingService (AccessibilityService)
├── ui/
│   ├── home/           # 首页: HomeScreen, HomeViewModel, DatePickerBottomSheet, WheelPicker
│   ├── record/         # 记账: RecordBottomSheet, EditTransactionSheet, CalculatorKeyboard, RecordViewModel
│   ├── report/         # 报表: ReportScreen, ReportViewModel
│   ├── profile/        # 我的: ProfileScreen
│   └── theme/          # Glassmorphism 主题: LiquidGlassShader, GlassColors, AnimationPresets
├── ConfirmTransactionActivity.kt  # 自动记账确认弹窗
├── MainActivity.kt                # 入口，HorizontalPager 管理三个 Tab
└── JianJiApplication.kt           # Application 类，持有 database 实例
```

### 关键设计点

- **导航**: 不使用 Jetpack Navigation，而是 `HorizontalPager` + 底部毛玻璃导航栏（3 个 Tab：首页/报表/我的）
- **依赖注入**: 未使用 Hilt，手动在 `MainActivity.onCreate()` 中创建 `TransactionRepository`，通过 ViewModel 工厂传入
- **数据库**: Room + `fallbackToDestructiveMigration()`，当前版本 2
- **预算存储**: SharedPreferences（非 Room），通过 `TransactionRepository` 的 `prefs` 字段
- **UI 风格**: Liquid Glass / Glassmorphism，使用 Haze 库实现毛玻璃效果
- **自动记账**: `AutoAccountingService` 是 Android 无障碍服务，监听微信/支付宝/京东的窗口事件，通过 `TransactionParser` 解析支付页面文本

### 数据流

```
UI (Compose) → ViewModel → Repository → DAO (Room/Flow)
                                    └→ SharedPreferences (预算)
```

所有数据库查询返回 `Flow<>`，Compose 通过 `collectAsState()` 自动响应数据变化。

## 测试

- 单元测试: `app/src/test/` — ViewModel、Repository、Model 测试（JUnit + MockK + Turbine + Truth）
- 仪器测试: `app/src/androidTest/` — DAO 测试、集成测试
- 测试依赖: MockK 1.13.13, Turbine 1.1.0, Truth 1.4.4

## 注意事项

- `AutoAccountingService` 监听的包名硬编码在 `targetPackages` 列表中
- `ConfirmTransactionActivity` 使用透明主题 (`Theme.JianJi.Translucent`) 作为浮窗式确认对话框
- 计算器键盘 (`CalculatorKeyboard`) 自定义实现，非系统键盘
