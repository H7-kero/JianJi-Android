# 简记 (JianJi) Android - Code Wiki

> 一款简洁易用的个人记账应用，支持自动识别微信、支付宝、京东支付页面并自动记账。

---

## 目录

- [1. 项目概览](#1-项目概览)
- [2. 技术栈与依赖](#2-技术栈与依赖)
- [3. 项目架构](#3-项目架构)
- [4. 目录结构](#4-目录结构)
- [5. 核心模块详解](#5-核心模块详解)
  - [5.1 应用入口层](#51-应用入口层)
  - [5.2 数据层 (data)](#52-数据层-data)
  - [5.3 服务层 (service)](#53-服务层-service)
  - [5.4 UI 层 (ui)](#54-ui-层-ui)
  - [5.5 工具层 (util)](#55-工具层-util)
- [6. 关键类与函数说明](#6-关键类与函数说明)
- [7. 数据流与依赖关系](#7-数据流与依赖关系)
- [8. 数据库设计](#8-数据库设计)
- [9. 自动记账流程](#9-自动记账流程)
- [10. UI 主题系统](#10-ui-主题系统)
- [11. 项目构建与运行](#11-项目构建与运行)
- [12. 测试](#12-测试)

---

## 1. 项目概览

| 属性 | 值 |
|---|---|
| **项目名称** | 简记 (JianJi) |
| **包名** | `com.jianji.app` |
| **仓库** | [H7-kero/jianji-android](https://github.com/H7-kero/jianji-android) |
| **语言** | Kotlin |
| **最低 SDK** | Android 10 (API 29) |
| **目标 SDK** | Android 16 (API 36) |
| **编译 SDK** | 36 |
| **版本** | 1.0.0 (versionCode: 1) |
| **许可证** | MIT License |

### 核心功能

- 📊 **首页概览**：今日支出/收入统计，按日期查看交易记录
- 📝 **快速记账**：支持支出/收入记录，内置计算器键盘
- 📈 **数据报表**：月度统计，按日汇总收支
- 💰 **预算管理**：设置月度预算，超支提醒
- 🤖 **自动记账**：通过无障碍服务识别微信、支付宝、京东支付页面，自动弹出确认窗口

---

## 2. 技术栈与依赖

### 构建工具

| 工具 | 版本 |
|---|---|
| AGP (Android Gradle Plugin) | 8.9.1 |
| Kotlin | 2.2.0 |
| KSP (Kotlin Symbol Processing) | 2.2.0-2.0.2 |
| Java Toolchain | 21 |
| Gradle JVM Args | -Xmx2048m |

### 核心依赖

| 分类 | 库 | 版本 | 用途 |
|---|---|---|---|
| **UI 框架** | Jetpack Compose BOM | 2024.09.01 | 统一管理 Compose 版本 |
| | Compose UI | (BOM) | 声明式 UI 框架 |
| | Material 3 | (BOM) | Material Design 3 组件 |
| | Material Icons Extended | (BOM) | 扩展图标库 |
| | Haze | 1.7.2 | 毛玻璃模糊效果 |
| | Haze Materials | 1.7.2 | 毛玻璃预设材质 |
| **导航** | Navigation Compose | 2.8.1 | 页面导航 |
| **生命周期** | Lifecycle Runtime KTX | 2.8.4 | 生命周期感知 |
| | Lifecycle ViewModel Compose | 2.8.4 | ViewModel 集成 |
| | Lifecycle Runtime Compose | 2.8.4 | Compose 生命周期 |
| **数据库** | Room Runtime | 2.6.1 | 本地数据持久化 |
| | Room KTX | 2.6.1 | 协程支持 |
| | Room Compiler (KSP) | 2.6.1 | 注解处理器 |
| **偏好存储** | DataStore Preferences | 1.0.0 | 键值对偏好设置 |
| **协程** | Kotlin Coroutines Android | 1.8.1 | 异步编程 |
| **基础库** | Core KTX | 1.15.0 | Android 核心扩展 |
| | Activity Compose | 1.9.2 | Activity Compose 集成 |
| | Material Components | 1.12.0 | XML 主题资源 |

### 测试依赖

| 库 | 版本 | 用途 |
|---|---|---|
| JUnit | 4.13.2 | 单元测试框架 |
| MockK | 1.13.13 | Kotlin Mock 框架 |
| Coroutines Test | 1.8.1 | 协程测试 |
| Turbine | 1.1.0 | Flow 测试 |
| Truth | 1.4.4 | 断言库 |
| Room Testing | 2.6.1 | Room 数据库测试 |

---

## 3. 项目架构

本项目采用 **MVVM + Clean Architecture** 分层架构，以 Jetpack Compose 为 UI 框架，Room 为本地数据存储。

```
┌─────────────────────────────────────────────────────┐
│                    UI 层 (Compose)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │HomeScreen │  │ReportScreen│ │ProfileScreen│       │
│  └─────┬────┘  └─────┬────┘  └──────────┘           │
│        │              │                               │
│  ┌─────┴────┐  ┌─────┴──────┐                        │
│  │HomeVM    │  │ReportVM    │  RecordVM              │
│  └─────┬────┘  └─────┬──────┘  └─────┬─────┐        │
└────────┼──────────────┼───────────────┼──────────────┘
         │              │               │
┌────────┼──────────────┼───────────────┼──────────────┐
│        └──────────────┼───────────────┘               │
│               数据层 (Repository)                      │
│        ┌──────────────┴──────────────┐                │
│        │   TransactionRepository     │                │
│        └──────────────┬──────────────┘                │
│                       │                               │
│        ┌──────────────┴──────────────┐                │
│        │      TransactionDao         │                │
│        └──────────────┬──────────────┘                │
│                       │                               │
│        ┌──────────────┴──────────────┐                │
│        │     AppDatabase (Room)      │                │
│        └─────────────────────────────┘                │
└───────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────┐
│              服务层 (Accessibility)                     │
│  ┌────────────────────────────────────────────┐       │
│  │  AutoAccountingService                     │       │
│  │    ├── 监听微信/支付宝/京东窗口事件          │       │
│  │    ├── 提取屏幕文本                         │       │
│  │    └── TransactionParser 解析交易信息        │       │
│  └────────────────────────────────────────────┘       │
└───────────────────────────────────────────────────────┘
```

### 架构特点

1. **单向数据流**：UI 观察 ViewModel 中的 `StateFlow`，ViewModel 通过 Repository 获取数据
2. **响应式编程**：Room 返回 `Flow`，数据变化自动传递到 UI 层
3. **关注点分离**：UI、业务逻辑、数据访问各层职责明确
4. **无 DI 框架**：项目未使用 Hilt/Dagger，依赖通过构造函数手动传递

---

## 4. 目录结构

```
JianJi-Android/
├── app/
│   ├── build.gradle.kts              # App 模块构建配置
│   ├── proguard-rules.pro            # 混淆规则
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # 应用清单
│       │   ├── java/com/jianji/app/
│       │   │   ├── JianJiApplication.kt       # Application 入口
│       │   │   ├── MainActivity.kt            # 主 Activity + 导航
│       │   │   ├── ConfirmTransactionActivity.kt  # 自动记账确认
│       │   │   ├── ConfirmTransactionScreen.kt    # 确认页面 UI
│       │   │   ├── data/                      # 数据层
│       │   │   │   ├── TransactionParser.kt   # 交易信息解析器
│       │   │   │   ├── local/
│       │   │   │   │   ├── AppDatabase.kt     # Room 数据库
│       │   │   │   │   └── TransactionDao.kt  # 数据访问对象
│       │   │   │   ├── model/
│       │   │   │   │   ├── Transaction.kt     # 交易记录实体
│       │   │   │   │   ├── Category.kt        # 分类实体
│       │   │   │   │   ├── Budget.kt          # 预算实体
│       │   │   │   │   └── CategoryExpense.kt # 分类支出统计
│       │   │   │   └── repository/
│       │   │   │       └── TransactionRepository.kt  # 数据仓库
│       │   │   ├── service/
│       │   │   │   └── AutoAccountingService.kt  # 无障碍自动记账服务
│       │   │   ├── ui/                        # UI 层
│       │   │   │   ├── home/
│       │   │   │   │   ├── HomeScreen.kt      # 首页界面
│       │   │   │   │   ├── HomeViewModel.kt   # 首页逻辑
│       │   │   │   │   ├── DatePickerBottomSheet.kt  # 日期选择器
│       │   │   │   │   └── WheelPicker.kt     # 滚轮选择器
│       │   │   │   ├── record/
│       │   │   │   │   ├── RecordBottomSheet.kt     # 记账底部弹窗
│       │   │   │   │   ├── RecordViewModel.kt       # 记账逻辑
│       │   │   │   │   ├── CalculatorKeyboard.kt    # 计算器键盘
│       │   │   │   │   ├── CategoryPickerDialog.kt  # 分类选择弹窗
│       │   │   │   │   └── EditTransactionSheet.kt  # 编辑交易弹窗
│       │   │   │   ├── report/
│       │   │   │   │   ├── ReportScreen.kt    # 报表界面
│       │   │   │   │   └── ReportViewModel.kt # 报表逻辑
│       │   │   │   ├── profile/
│       │   │   │   │   └── ProfileScreen.kt   # 个人设置界面
│       │   │   │   └── theme/
│       │   │   │       ├── GlassmorphismTheme.kt   # 毛玻璃主题组件
│       │   │   │       ├── AnimationPresets.kt     # 动画预设
│       │   │   │       └── LiquidGlassShader.kt    # 液态玻璃着色器
│       │   │   └── util/
│       │   │       └── AmountFormatter.kt     # 金额格式化
│       │   └── res/
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       └── xml/
│       │           └── accessibility_service_config.xml
│       ├── test/                       # 单元测试
│       └── androidTest/               # 仪器测试
├── build.gradle.kts                   # 根项目构建配置
├── settings.gradle.kts                # 项目设置
├── gradle.properties                  # Gradle 全局配置
└── docs/                              # 文档
```

---

## 5. 核心模块详解

### 5.1 应用入口层

#### `JianJiApplication`

应用级 `Application` 子类，负责初始化全局单例数据库。

```kotlin
class JianJiApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}
```

- 使用 `lazy` 委托实现数据库的延迟初始化
- 通过 `AppDatabase.getDatabase()` 的双重检查锁保证单例

#### `MainActivity`

应用主入口，承担以下职责：

1. **初始化依赖链**：创建 `AppDatabase` → `TransactionDao` → `TransactionRepository`
2. **Compose 宿主**：通过 `setContent` 设置 Compose 内容
3. **页面导航**：使用 `HorizontalPager` 实现三页面滑动切换（首页/报表/我的）
4. **底部导航栏**：`FloatingGlassNavBar` 毛玻璃风格浮动导航
5. **FAB 记账按钮**：支持左下/右下位置切换
6. **弹窗管理**：管理记账 `RecordBottomSheet` 和编辑 `EditTransactionSheet`

关键 Composable 函数：

| 函数 | 说明 |
|---|---|
| `JianJiApp(repository)` | 顶层 Composable，组装所有页面和导航 |
| `FAB(onClick)` | 毛玻璃风格浮动按钮 |
| `FloatingGlassNavBar(currentPage, onPageSelected)` | 底部浮动导航栏 |

#### `ConfirmTransactionActivity`

自动记账确认页面，由 `AutoAccountingService` 启动。接收 Intent 中的交易信息（type, amount, channel, category 等），展示 `ConfirmTransactionScreen` 供用户确认或修改。

### 5.2 数据层 (data)

#### 5.2.1 数据模型 (model)

##### `Transaction`

核心数据实体，映射数据库 `transactions` 表。

| 字段 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `id` | `Long` | 0 (自增) | 主键 |
| `amount` | `Double` | - | 金额 |
| `category` | `String` | - | 分类（餐饮、交通等） |
| `subCategory` | `String?` | null | 子分类（早餐、午餐等） |
| `channel` | `String?` | null | 支付渠道（微信、支付宝等） |
| `type` | `String` | - | 类型：`expense` / `income` |
| `note` | `String` | "" | 备注 |
| `merchant` | `String?` | null | 商户名称 |
| `timestamp` | `Long` | 当前时间 | 时间戳（毫秒） |
| `source` | `String` | "manual" | 来源：`manual` / `auto` |

数据库索引：`timestamp`、`type+timestamp`、`category`

##### `Category`

分类实体，映射 `categories` 表。

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `Long` | 主键（自增） |
| `name` | `String` | 分类名称 |
| `type` | `String` | 类型（expense/income） |
| `icon` | `String` | 图标标识 |
| `color` | `Long` | 颜色值 |
| `sortOrder` | `Int` | 排序顺序 |
| `isSystem` | `Boolean` | 是否系统预设 |

##### `Budget`

预算实体，映射 `budgets` 表。

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `Long` | 主键（自增） |
| `amount` | `Double` | 预算金额 |
| `period` | `String` | 周期 |
| `categoryId` | `Long?` | 关联分类 ID |
| `year` | `Int` | 年份 |
| `month` | `Int` | 月份 |

##### `CategoryExpense`

分类支出统计 DTO，用于 Room 聚合查询结果。

| 字段 | 类型 | 说明 |
|---|---|---|
| `category` | `String` | 分类名称 |
| `total` | `Double` | 该分类总支出 |

#### 5.2.2 数据库 (local)

##### `AppDatabase`

Room 数据库定义，版本 2，包含三张表：`transactions`、`categories`、`budgets`。

- 使用 `fallbackToDestructiveMigration()`：数据库版本升级时销毁重建
- 单例模式：双重检查锁 (`synchronized`) + `@Volatile`
- 数据库名称：`jianji_database`
- 当前仅暴露 `transactionDao()`，`CategoryDao` 和 `BudgetDao` 尚未实现

##### `TransactionDao`

交易记录数据访问对象，定义所有 SQL 操作。

| 方法 | 返回类型 | SQL 操作 | 说明 |
|---|---|---|---|
| `insert(transaction)` | `suspend Unit` | INSERT | 插入交易记录 |
| `delete(transaction)` | `suspend Unit` | DELETE | 删除交易记录 |
| `update(transaction)` | `suspend Unit` | UPDATE | 更新交易记录 |
| `getAllTransactions()` | `Flow<List<Transaction>>` | SELECT * | 查询全部（按时间倒序） |
| `getTransactionsByDateRange(start, end)` | `Flow<List<Transaction>>` | SELECT WHERE | 按日期范围查询 |
| `getTransactionsByCategory(category)` | `Flow<List<Transaction>>` | SELECT WHERE | 按分类查询 |
| `getTotalExpense(start, end)` | `Flow<Double?>` | SELECT SUM | 指定日期范围总支出 |
| `getTotalIncome(start, end)` | `Flow<Double?>` | SELECT SUM | 指定日期范围总收入 |
| `getExpenseByCategory(start, end)` | `Flow<List<CategoryExpense>>` | SELECT GROUP BY | 按分类统计支出 |
| `getTransactionsSince(startTime)` | `Flow<List<Transaction>>` | SELECT WHERE | 查询指定时间后的记录 |

> 所有查询方法返回 `Flow`，实现数据的响应式更新。

#### 5.2.3 数据仓库 (repository)

##### `TransactionRepository`

封装所有交易数据操作，是 ViewModel 与 DAO 之间的中间层。

**核心方法：**

| 方法 | 说明 |
|---|---|
| `getAllTransactions()` | 获取所有交易记录 |
| `getTodayTransactions()` | 获取今日交易 |
| `getTodayExpense()` / `getTodayIncome()` | 获取今日支出/收入 |
| `getMonthlyTransactions(year, month)` | 获取月度交易 |
| `getMonthlyExpense(year, month)` | 获取月度支出 |
| `getExpenseByDateRange(start, end)` | 按日期范围获取支出 |
| `getIncomeByDateRange(start, end)` | 按日期范围获取收入 |
| `getTransactionsByDateRange(start, end)` | 按日期范围获取交易列表 |
| `insertTransaction(transaction)` | 插入交易 |
| `addTransaction(transaction)` | 插入交易（别名） |
| `deleteTransaction(transaction)` | 删除交易 |
| `updateTransaction(transaction)` | 更新交易 |
| `getCategoryExpenseByDateRange(start, end)` | 获取分类支出统计 |
| `getMonthlyBudget(yearMonth)` | 获取月预算 |
| `setMonthlyBudget(yearMonth, budget)` | 设置月预算 |

**预算存储**：使用 `SharedPreferences`（键名 `jianji_prefs`），默认预算 3000 元。

**私有工具方法：**

| 方法 | 说明 |
|---|---|
| `getTodayTimeRange()` | 计算今日 00:00:00 ~ 明日 00:00:00 的时间戳 |
| `getMonthTimeRange(year, month)` | 计算指定月份的时间戳范围 |

#### 5.2.4 交易解析器 (TransactionParser)

用于自动记账场景，从支付页面文本中解析交易信息。

##### `ParsedTransaction`

解析结果数据类：

| 字段 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `type` | `String` | "expense" | 交易类型 |
| `amount` | `Double` | 0.0 | 金额 |
| `channel` | `String` | "微信" | 支付渠道 |
| `category` | `String` | "其他" | 分类 |
| `subCategory` | `String?` | null | 子分类 |
| `merchant` | `String?` | null | 商户 |

##### `TransactionParser` 对象

单例解析器，核心方法 `parse(packageName, screenText)`：

1. **`detectChannel(packageName, text)`**：根据包名或文本关键词识别支付渠道
   - `com.tencent.mm` → 微信
   - `com.eg.android.AlipayGphone` → 支付宝
   - `com.jingdong.app.mall` → 京东
2. **`extractAmount(text)`**：通过正则提取金额
   - 支持格式：`¥35.50`、`35.50元`、`付款35.50`、`支付35.50`、`消费35.50`
   - 金额范围：0 < amount < 1,000,000
3. **`extractMerchant(text)`**：通过正则提取商户名称
   - 支持格式：`收款方：xxx`、`付款给xxx`、`- xxx ¥`
4. **`detectCategory(text, merchant)`**：基于关键词匹配分类
   - 支持 8 大分类：餐饮、交通、购物、娱乐、医疗、教育、居住、其他
   - 餐饮/交通支持子分类识别

### 5.3 服务层 (service)

#### `AutoAccountingService`

继承 `AccessibilityService`，监听支付应用的无障碍事件。

**监听目标应用：**

| 包名 | 应用 |
|---|---|
| `com.tencent.mm` | 微信 |
| `com.eg.android.AlipayGphone` | 支付宝 |
| `com.jingdong.app.mall` | 京东 |

**工作流程：**

1. `onServiceConnected()`：配置服务参数，监听窗口状态变化和内容变化事件
2. `onAccessibilityEvent(event)`：
   - 过滤目标应用的事件
   - 递归提取屏幕文本 (`extractText`)
   - 防抖处理：3 秒内不重复处理，相同金额 6 秒内不重复
   - 调用 `TransactionParser.parse()` 解析交易信息
   - 启动 `ConfirmTransactionActivity` 展示确认弹窗

**配置文件**：`res/xml/accessibility_service_config.xml`
- 监听事件：`typeWindowStateChanged | typeWindowContentChanged`
- 反馈类型：`feedbackGeneric`
- 标志：`flagIncludeNotImportantViews | flagReportViewIds`

### 5.4 UI 层 (ui)

#### 5.4.1 首页模块 (home)

##### `HomeViewModel`

管理首页状态，核心状态流：

| 状态 | 类型 | 说明 |
|---|---|---|
| `selectedDate` | `StateFlow<LocalDate>` | 当前选中日期 |
| `todayExpense` | `StateFlow<Double>` | 今日支出 |
| `todayIncome` | `StateFlow<Double>` | 今日收入 |
| `dayExpense` | `StateFlow<Double>` | 选中日期支出 |
| `dayIncome` | `StateFlow<Double>` | 选中日期收入 |
| `transactions` | `StateFlow<List<Transaction>>` | 选中日期交易列表 |
| `categoryExpenses` | `StateFlow<List<CategoryExpense>>` | 选中日期分类支出 |
| `monthlyExpense` | `StateFlow<Double>` | 本月支出 |
| `monthlyBudget` | `StateFlow<Double>` | 本月预算 |
| `editingTransaction` | `StateFlow<Transaction?>` | 正在编辑的交易 |

关键方法：

| 方法 | 说明 |
|---|---|
| `selectDate(date)` | 切换选中日期，自动刷新相关数据流 |
| `startEditing(transaction)` | 开始编辑交易 |
| `stopEditing()` | 停止编辑 |
| `deleteTransaction(transaction)` | 删除交易 |

> 使用 `flatMapLatest` 实现日期切换时自动重新订阅数据流。

##### `HomeScreen`

首页界面 Composable，展示：
- 日期选择器（`DatePickerBottomSheet`）
- 今日/当月收支概览卡片
- 分类支出统计
- 交易记录列表（支持滑动删除、点击编辑）

##### `DatePickerBottomSheet` / `WheelPicker`

自定义日期选择器组件，使用滚轮式选择器实现年/月/日的选择。

#### 5.4.2 记账模块 (record)

##### `RecordViewModel`

管理记账表单状态：

| 状态 | 类型 | 说明 |
|---|---|---|
| `transactionType` | `StateFlow<String>` | 交易类型 (expense/income) |
| `amount` | `StateFlow<String>` | 金额字符串 |
| `selectedCategory` | `StateFlow<String?>` | 选中分类 |
| `selectedSubCategory` | `StateFlow<String?>` | 选中子分类 |
| `selectedChannel` | `StateFlow<String>` | 选中支付渠道 |
| `note` | `StateFlow<String>` | 备注 |
| `isSaved` | `StateFlow<Boolean>` | 是否已保存 |
| `editingTransactionId` | `StateFlow<Long?>` | 编辑中的交易 ID |

**预定义分类：**

- 支出：餐饮、交通、购物、娱乐、医疗、教育、居住、其他
- 收入：工资、奖金、投资、兼职、其他
- 渠道：微信、支付宝、现金、银行卡、京东

**智能默认值**：`getDefaultSubCategory()` 根据当前时间自动推荐子分类（5-10点→早餐，11-13点→午餐等）

关键方法：

| 方法 | 说明 |
|---|---|
| `saveTransaction()` | 保存新交易记录 |
| `updateTransaction(originalId)` | 更新已有交易记录 |
| `loadTransaction(transaction)` | 加载交易到表单（编辑模式） |
| `resetForm()` | 重置表单 |

##### `RecordBottomSheet`

记账底部弹窗，包含：
- 支出/收入切换
- 计算器键盘 (`CalculatorKeyboard`)
- 分类选择 (`CategoryPickerDialog`)
- 金额输入与显示

##### `CalculatorKeyboard`

自定义计算器键盘组件，支持数字输入和基本运算。

##### `EditTransactionSheet`

编辑交易记录的底部弹窗，复用 `RecordViewModel`。

#### 5.4.3 报表模块 (report)

##### `ReportViewModel`

管理报表页面状态：

| 状态 | 类型 | 说明 |
|---|---|---|
| `selectedYear` | `StateFlow<Int>` | 选中年份 |
| `selectedMonth` | `StateFlow<Int>` | 选中月份 |
| `daySummaries` | `StateFlow<List<DaySummary>>` | 每日汇总数据 |
| `monthlyExpense` | `StateFlow<Double>` | 月度总支出 |
| `monthlyIncome` | `StateFlow<Double>` | 月度总收入 |

##### `DaySummary`

单日收支汇总数据类：

| 字段 | 类型 | 说明 |
|---|---|---|
| `day` | `Int` | 日期（1-31） |
| `expense` | `Double` | 当日支出 |
| `income` | `Double` | 当日收入 |
| `transactions` | `List<Transaction>` | 当日交易列表 |

关键方法：

| 方法 | 说明 |
|---|---|
| `selectYear(year)` | 切换年份 |
| `selectMonth(month)` | 切换月份 |
| `goToPreviousMonth()` | 上一个月 |
| `goToNextMonth()` | 下一个月 |

##### `ReportScreen`

报表界面，展示月度收支日历视图和统计数据。

#### 5.4.4 个人设置模块 (profile)

##### `ProfileScreen`

个人设置界面，包含：

1. **自动记账设置**
   - `AutoAccountingPreferences`：DataStore 存储开关状态
   - 开启时自动引导用户授予无障碍权限
   - `isAccessibilityServiceEnabled()`：检测无障碍服务是否启用
   - `openAccessibilitySettings()`：跳转系统无障碍设置

2. **FAB 位置设置**
   - `FabPreferences`：DataStore 存储位置偏好（left/right）
   - 支持左下角/右下角切换

3. **使用说明**
   - 4 步图文引导

#### 5.4.5 主题模块 (theme)

##### `GlassColors`

毛玻璃主题颜色定义：

| 颜色 | 值 | 用途 |
|---|---|---|
| `glassBackground` | `#F5F7FA` | 页面背景 |
| `glassNavBackground` | `#E0FFFFFF` | 导航栏背景 |
| `glassCardBackground` | `#B8FFFFFF` | 卡片背景 |
| `glassSurface` | `#A0FFFFFF` | 表面背景 |
| `glassSurfaceVariant` | `#88FFFFFF` | 变体表面 |
| `glassHighlight` | 白色 55% | 高光 |
| `glassShadow` | 黑色 8% | 阴影 |
| `iosBlue` | `#007AFF` | iOS 风格蓝色 |
| `expenseRed` | `#FF3B30` | 支出红色 |
| `incomeGreen` | `#34C759` | 收入绿色 |

##### `LiquidGlassShapes`

统一圆角形状定义：

| 形状 | 圆角 |
|---|---|
| `large` | 28.dp |
| `card` | 20.dp |
| `medium` | 16.dp |
| `small` | 12.dp |
| `circle` | CircleShape |

##### `GlassCard` / `GlassSurface` / `GlassContainer`

三个毛玻璃风格 Composable 组件，层次递减：
- `GlassCard`：最厚，使用 `HazeMaterials.ultraThin` + RuntimeShader
- `GlassSurface`：中等，使用 `HazeMaterials.ultraThin`
- `GlassContainer`：最薄，使用 `HazeMaterials.thin`

##### `LiquidGlassShader`

Android RuntimeShader (AGSL) 实现，模拟 iOS Liquid Glass 效果：
- 折射 (refraction)
- 色散 (dispersion)
- 菲涅尔效果 (fresnel)
- 边缘高光 (edge highlight)
- 顶部高光 (top highlight)
- 仅在 Android 13+ (API 33) 生效

##### `AnimationPresets`

iOS 风格动画预设：

| 预设 | 说明 |
|---|---|
| `iosSpring` | 中等弹性弹簧动画 |
| `iosSnappy` | 无弹性快速弹簧动画 |
| `iOSEaseInOut` | 350ms 缓入缓出 |
| `iOSPageEnter/Exit` | 页面进入/退出动画 |
| `iOSDialogEnter/Exit` | 对话框动画 |
| `iOSFABEnter/Exit` | FAB 动画 |
| `iosPressEffect` | iOS 风格按压缩放效果 |
| `BlurRadius` | 模糊半径预设 |

### 5.5 工具层 (util)

#### `AmountFormatter`

金额格式化工具函数：

```kotlin
fun formatAmount(amount: Double): String
```

- 整数金额：显示为整数（如 `35`）
- 小数金额：保留两位小数（如 `35.50`）

---

## 6. 关键类与函数说明

### 类依赖关系图

```
JianJiApplication
  └── AppDatabase (单例)
        └── TransactionDao

MainActivity
  ├── TransactionRepository(transactionDao, context)
  ├── HomeViewModel(repository)
  ├── ReportViewModel(repository)
  ├── RecordViewModel(repository)
  └── FabPreferences (DataStore)

ConfirmTransactionActivity
  ├── AppDatabase.getDatabase(this)
  ├── TransactionRepository(transactionDao)
  └── ConfirmTransactionScreen(...)

AutoAccountingService
  ├── TransactionParser.parse(packageName, screenText)
  │     ├── detectChannel()
  │     ├── extractAmount()
  │     ├── extractMerchant()
  │     └── detectCategory()
  └── ConfirmTransactionActivity (启动)
```

### 关键函数调用链

#### 手动记账流程

```
用户点击 FAB
  → showRecordSheet = true
  → RecordBottomSheet 显示
  → 用户输入金额/选择分类
  → RecordViewModel.saveTransaction()
  → TransactionRepository.insertTransaction()
  → TransactionDao.insert()
  → Room 写入数据库
  → Flow 自动通知 UI 刷新
```

#### 自动记账流程

```
用户在微信/支付宝完成支付
  → AutoAccountingService.onAccessibilityEvent()
  → extractText(node) 递归提取屏幕文本
  → TransactionParser.parse(packageName, screenText)
  → ParsedTransaction 生成
  → showConfirmDialog(parsed)
  → ConfirmTransactionActivity 启动
  → ConfirmTransactionScreen 显示
  → 用户确认/修改
  → TransactionRepository.insertTransaction()
```

#### 编辑交易流程

```
用户点击交易记录
  → HomeViewModel.startEditing(transaction)
  → editingTransaction 状态更新
  → LaunchedEffect 检测到 editingTransaction != null
  → RecordViewModel.loadTransaction(transaction)
  → EditTransactionSheet 显示
  → 用户修改后保存
  → RecordViewModel.updateTransaction(id)
  → TransactionRepository.updateTransaction()
  → TransactionDao.update()
```

---

## 7. 数据流与依赖关系

### 数据流向

```
┌──────────────┐     Flow      ┌──────────────────┐    Flow    ┌───────────┐
│  Room DB     │ ──────────→  │  TransactionDao   │ ────────→ │ Repository │
│  (SQLite)    │              │                   │           │           │
└──────────────┘              └──────────────────┘           └─────┬─────┘
                                                                   │
                              Flow                                 │
                              ┌────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        ┌──────────┐   ┌──────────┐   ┌──────────┐
        │HomeVM    │   │ReportVM  │   │RecordVM  │
        │          │   │          │   │          │
        │todayExps │   │daySums   │   │save()    │
        │dayExps   │   │monthExps │   │update()  │
        │transacts │   │monthIncm │   │delete()  │
        └────┬─────┘   └────┬─────┘   └────┬─────┘
             │              │              │
             ▼              ▼              ▼
        ┌──────────┐   ┌──────────┐   ┌──────────┐
        │HomeScreen│   │ReportScr │   │RecordBtm │
        └──────────┘   └──────────┘   └──────────┘
```

### SharedPreferences 使用

| 文件 | 键 | 用途 |
|---|---|---|
| `jianji_prefs` | `monthly_budget_{yyyy-MM}` | 月度预算金额 |
| DataStore `auto_accounting` | `auto_accounting_enabled` | 自动记账开关 |
| DataStore `auto_accounting` | `fab_position` | FAB 位置偏好 |

---

## 8. 数据库设计

### ER 图

```
┌─────────────────────┐
│    transactions      │
├─────────────────────┤
│ PK id: Long (自增)   │
│    amount: Double    │
│    category: String  │
│    subCategory: Str? │
│    channel: String?  │
│    type: String      │
│    note: String      │
│    merchant: String? │
│    timestamp: Long   │
│    source: String    │
└─────────────────────┘
│
│  索引:
│  ├── idx_timestamp (timestamp)
│  ├── idx_type_timestamp (type, timestamp)
│  └── idx_category (category)

┌─────────────────────┐
│    categories        │
├─────────────────────┤
│ PK id: Long (自增)   │
│    name: String      │
│    type: String      │
│    icon: String      │
│    color: Long       │
│    sortOrder: Int    │
│    isSystem: Boolean │
└─────────────────────┘

┌─────────────────────┐
│    budgets           │
├─────────────────────┤
│ PK id: Long (自增)   │
│    amount: Double    │
│    period: String    │
│    categoryId: Long? │
│    year: Int         │
│    month: Int        │
└─────────────────────┘
```

> 当前版本 (v2) 中 `categories` 和 `budgets` 表已定义但尚未有对应的 DAO 实现，预算功能通过 SharedPreferences 临时实现。

---

## 9. 自动记账流程

```
                    ┌─────────────────────┐
                    │ 用户在支付应用完成支付 │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ AccessibilityService │
                    │ 监听窗口变化事件      │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ 过滤目标应用包名      │
                    │ (微信/支付宝/京东)    │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ 递归提取屏幕文本      │
                    │ (extractText)        │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ 防抖检查 (3s/6s)     │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ TransactionParser    │
                    │ .parse()            │
                    │ ├── 识别支付渠道      │
                    │ ├── 提取金额          │
                    │ ├── 提取商户          │
                    │ └── 匹配分类          │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ ConfirmTransaction   │
                    │ Activity 启动        │
                    │ (半透明确认窗口)      │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ 用户确认/修改/取消    │
                    └──────────┬──────────┘
                          ┌────┴────┐
                          │         │
                     确认保存    取消放弃
                          │         │
                    ┌─────▼─────┐   │
                    │ 写入数据库 │   │
                    │ (source=  │   │
                    │  "auto")  │   │
                    └───────────┘   └──→ 什么都不做
```

---

## 10. UI 主题系统

本项目采用 **Liquid Glass（液态玻璃）** 设计语言，模仿 iOS 风格的毛玻璃效果。

### 主题层次

```
GlassCard (最厚)
  ├── HazeMaterials.ultraThin
  ├── RuntimeShader (Liquid Glass)
  ├── elevation: 2dp
  └── border: 0.5dp 白色 50%

GlassSurface (中等)
  ├── HazeMaterials.ultraThin
  ├── RuntimeShader (Liquid Glass)
  ├── elevation: 2dp
  └── border: 0.5dp 白色 50%

GlassContainer (最薄)
  ├── HazeMaterials.thin
  ├── RuntimeShader (Liquid Glass, 低折射)
  ├── elevation: 1dp
  └── border: 0.5dp 白色 40%
```

### 兼容性

- **Android 13+ (API 33)**：完整 Liquid Glass 效果（RuntimeShader + Haze）
- **Android 10-12 (API 29-32)**：降级为纯色背景（`Modifier.background(containerColor)`）

### 动画体系

所有动画遵循 iOS Human Interface Guidelines 的运动设计原则：
- 弹簧动画（Spring）用于自然交互反馈
- 缓入缓出（Ease In Out）用于页面切换
- 按压缩放（Press Scale）用于触摸反馈

---

## 11. 项目构建与运行

### 环境要求

| 要求 | 版本 |
|---|---|
| Android Studio | 最新稳定版 |
| JDK | 21 |
| Android SDK | API 36 |
| Min SDK | API 29 (Android 10) |
| Gradle | 项目自带 wrapper |

### 构建命令

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 运行 Android 仪器测试
./gradlew connectedAndroidTest

# 清理构建
./gradlew clean
```

### 运行步骤

1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 连接模拟器或真机（Android 10+）
4. 点击运行按钮或 `Shift + F10`

### 权限说明

| 权限 | 用途 |
|---|---|
| `WRITE_EXTERNAL_STORAGE` | 外部存储写入 |
| `READ_EXTERNAL_STORAGE` | 外部存储读取 |
| `BIND_ACCESSIBILITY_SERVICE` | 无障碍服务绑定（自动记账） |

---

## 12. 测试

### 测试框架

- **JUnit 4**：单元测试基础框架
- **MockK**：Kotlin Mock 框架，用于模拟依赖
- **Turbine**：Flow 测试工具
- **Truth**：Google 断言库，提供更可读的断言语法
- **Room Testing**：Room 数据库测试支持

### 测试目录

```
app/src/
├── test/           # 单元测试 (JVM)
│   └── java/com/jianji/app/
└── androidTest/    # 仪器测试 (Android)
    └── java/com/jianji/app/
```

### 运行测试

```bash
# 单元测试
./gradlew test

# 仪器测试（需要连接设备）
./gradlew connectedAndroidTest
```
