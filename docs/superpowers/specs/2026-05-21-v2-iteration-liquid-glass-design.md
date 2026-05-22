# 简记 v2 功能迭代 + iOS 26 Liquid Glass 视觉升级设计文档

## 概述

本次迭代包含 11 项功能需求，核心目标：
1. 将 UI 视觉从半透明渐变模拟升级为 Haze + AGSL 着色器实现的 iOS 26 Liquid Glass 效果
2. 重构首页信息架构，合并日历功能，统一收支展示
3. 补全交易修改、空状态等基础功能

---

## 一、液态玻璃视觉升级（需求 9）

### 1.1 技术栈

| 组件 | 版本 | 用途 |
|------|------|------|
| Haze | 1.7.2 | 背景捕获 + 实时模糊（Backdrop Blur） |
| Haze Materials | 1.7.2 | 预设玻璃材质（ultraThin/thin/thick） |
| AGSL RuntimeShader | API 33+ | 折射/色散/Fresnel/边缘高光 |

### 1.2 实现架构

```
┌─────────────────────────────────────────┐
│              HazeState                   │
│  ┌─────────────────────────────────┐    │
│  │  hazeSource (背景内容层)         │    │
│  │  - LazyColumn / 背景图          │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │  hazeEffect (玻璃层)            │    │
│  │  - 模糊半径 25dp               │    │
│  │  - 饱和度增强 1.8x             │    │
│  │  - AGSL 着色器后处理            │    │
│  │    ├─ 折射扭曲 (refraction)     │    │
│  │    ├─ RGB 色散偏移 (dispersion) │    │
│  │    ├─ Fresnel 反射 (fresnel)    │    │
│  │    └─ 边缘高光 (edge highlight) │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

### 1.3 AGSL 着色器管线

着色器接收 Haze 模糊后的图像，依次执行：

1. **折射扭曲**：基于 SDF 距离场计算偏移向量，实现透镜效果
2. **色散**：RGB 三通道分别偏移采样，产生色差
3. **Fresnel 反射**：Schlick 近似，边缘区域叠加白色反射
4. **边缘高光**：双向 SDF 边缘照明，顶部高光条

### 1.4 组件改造

所有 `GlassCard`/`GlassSurface`/`GlassContainer` 组件改造为 Haze 管线：

- `GlassCard`：ElevatedCard + hazeEffect + AGSL 后处理
- `GlassSurface`：Box + hazeEffect + AGSL 后处理
- `GlassContainer`：Box + hazeEffect + AGSL 后处理（轻量参数）
- `FloatingGlassNavBar`：Box + hazeEffect + AGSL 后处理
- `FAB`：Box + hazeEffect + AGSL 后处理
- `RecordBottomSheet`：ModalBottomSheet + window.blurBehindRadius

### 1.5 iOS 26 设计规范适配

| 属性 | 值 | 说明 |
|------|-----|------|
| 模糊半径 | 25dp | 背景高斯模糊 |
| 饱和度 | 1.8x | 模糊后色彩增强 |
| 白色叠加 | 0.15 alpha | 半透明白色 tint |
| 边框 | 0.5dp, white 0.5 alpha | 高光边框 |
| 折射强度 | 0.3 | 轻微透镜扭曲 |
| 色散偏移 | 0.003 | RGB 通道偏移 |
| Fresnel 指数 | 3.0 | Schlick 近似幂次 |
| 圆角系统 | 28/20/16/12 dp | 大/卡/中/小 |

### 1.6 降级策略

- API 33+：完整 Liquid Glass 效果（模糊+折射+色散+Fresnel+高光）
- API 31-32：仅 Haze 背景模糊 + 边缘高光（无 AGSL 着色器）
- API 29-30：半透明渐变回退（当前效果）

---

## 二、首页改造

### 2.1 去掉大标题（需求 1）

- 删除 HomeScreen 顶部的 "简记" 32sp 标题
- 顶部改为状态栏沉浸 + 16dp 顶部留白
- ProfileScreen 的 "我的" 标题同步删除
- ReportScreen 的 "报表" 标题同步删除

### 2.2 今日收支卡片重构（需求 2）

**当前**：左右两列 "今日支出" / "今日收入"，蓝色收入

**改为**：
- 卡片标题 "今日收支"，右侧显示日期 "5月21日 周三"（可点击）
- 支出金额红色 `#FF3B30`，收入金额绿色 `#34C759`
- 收入为 0 时隐藏收入行，支出独占整行
- 日期文字带下划线指示可点击

布局：
```
┌──────────────────────────────────┐
│  今日收支          5月21日 周三 ▾ │
│                                  │
│  支出  ¥128                      │
│  收入  ¥50        （收入为0时隐藏）│
└──────────────────────────────────┘
```

### 2.3 日历合并进首页（需求 3）

**当前**：报表页（Tab 2）包含完整日历视图 + 年月选择器

**改为**：
- 点击今日收支卡片的日期，弹出 ModalBottomSheet 日历浮窗
- 浮窗内使用 **滚轮式时间选择器**（WheelPicker）选择年月
- 年月在同一个滚轮组中，左侧年、右侧月，不再分开选择
- 浮窗顶部左右箭头快速切换月份
- 滚轮滚动时触发系统触觉反馈（HapticFeedback）
- 下方展示月历网格，选中日期高亮

浮窗布局：
```
┌──────────────────────────────────┐
│  ◀  2026年 5月  ▶               │
│                                  │
│  ┌─────┐  ┌─────┐               │
│  │2026 │  │  5  │  ← 滚轮选择器  │
│  │2025 │  │  4  │    带触觉反馈   │
│  │2024 │  │  3  │               │
│  └─────┘  └─────┘               │
│                                  │
│  一 二 三 四 五 六 日            │
│            1  2  3               │
│   4  5  6  7  8  9 10           │
│  11 12 13 14 15 16 17           │
│  18 19 20 21 22 23 24           │
│  25 26 27 28 29 30 31           │
│                                  │
│  [ 今日 ]                        │
└──────────────────────────────────┘
```

### 2.4 日期联动交易列表（需求 4）

- HomeViewModel 新增 `selectedDate: StateFlow<LocalDate>` 状态，默认今天
- 新增 `getTransactionsByDate(date: LocalDate)` 方法
- 选择日期后，交易列表展示该日期的记录
- 卡片标题日期同步更新为选中日期
- Repository 新增 `getTransactionsByDateRange(start, end)` 查询方法
- TransactionDao 新增对应 SQL 查询

### 2.5 "今日"按钮（需求 5）

- 日历浮窗底部提供 "今日" 文字按钮
- 点击后：滚轮跳回当前年月 + 日历高亮今天 + 交易列表刷新为今日
- 按钮使用 iOS Blue `#007AFF`，液态玻璃风格

### 2.6 不可选未来日期（需求 6）

- 月历网格中未来日期灰色显示（alpha 0.3），不可点击
- 滚轮选择器不可滚动到未来月份
- 当月最大可选日期 = 今天

### 2.7 备注输入框优化（需求 8）

**当前问题**：`OutlinedTextField` 固定 `height(48.dp)` 导致文字被切割

**修复**：
- 移除固定高度约束
- 改为 `minHeight(48.dp)` + 自适应高度
- `maxLines = 3`，允许最多 3 行备注
- 调整 `TextFieldDefaults.container` 的内边距
- 确保单行时高度 48dp，多行时自然扩展

---

## 三、交易记录修改（需求 7）

### 3.1 交互流程

1. 首页交易列表项增加点击事件
2. 点击后弹出修改浮窗（ModalBottomSheet）
3. 浮窗预填充已有数据：类型、金额、分类、子分类、渠道、备注
4. 时间字段只读显示，不可修改
5. 修改后点击保存，调用 `repository.updateTransaction()`

### 3.2 数据流

```
TransactionItem onClick
  → showEditSheet = true, loadTransaction(tx)
  → EditBottomSheet 预填充
  → 用户修改字段
  → saveEdit() → repository.updateTransaction()
  → 列表自动刷新（Flow 响应式）
```

### 3.3 ViewModel 变更

RecordViewModel 新增：
- `loadTransaction(transaction: Transaction)`：加载已有交易到表单
- `updateTransaction()`：更新交易记录
- `editingTransactionId: StateFlow<Long?>`：正在编辑的交易 ID

### 3.4 UI 复用

修改浮窗复用 RecordBottomSheet 的 UI 组件：
- TypeToggle、CategorySelectorRow、SubCategoryChipRow、ChannelChipRow
- CalculatorKeyboard（金额可重新输入）
- 新增时间只读行：显示 "2026-05-21 14:30"，灰色文字

---

## 四、导航栏圆角修复（需求 10）

### 4.1 问题分析

当前导航栏外框圆角 `LiquidGlassShapes.large` = 24dp，内部选中指示器也使用 24dp 圆角。但指示器在 8dp 水平内边距 + 5dp 垂直内边距的容器内，24dp 圆角与外框 r 角视觉不对齐。

### 4.2 修复方案

指示器圆角改为与导航栏外框 r 角视觉对齐。具体计算：
- 导航栏外框：24dp 圆角
- 内边距：horizontal 8dp, vertical 5dp
- 指示器有效圆角 = 外框圆角 - 内边距 = 24 - 8 = 16dp（水平方向）
- 但垂直方向 24 - 5 = 19dp
- 取较小值保证视觉一致：指示器圆角改为 `RoundedCornerShape(16.dp)`

同时，指示器应使用液态玻璃效果（Haze + 半透明），而非当前的纯黑色 `Color.Black.copy(alpha = 0.06f)`。

---

## 五、报表页空状态（需求 11）

### 5.1 改造

报表页删除日历视图和月度统计卡片，改为空状态页面。

### 5.2 空状态 UI

```
┌──────────────────────────────────┐
│                                  │
│         📊 (图标)                │
│                                  │
│      报表功能开发中              │
│   即将支持图表分析和趋势报告     │
│                                  │
└──────────────────────────────────┘
```

- 居中显示，液态玻璃风格卡片
- 图标使用 Material Icons `BarChart`
- 主标题 18sp SemiBold
- 副标题 14sp Regular，onSurfaceVariant 色

---

## 六、文件变更清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `build.gradle.kts` | 修改 | 添加 Haze 依赖 |
| `GlassmorphismTheme.kt` | 重写 | Haze + AGSL 着色器管线 |
| `LiquidGlassShader.agsl` | 新增 | AGSL 着色器源码 |
| `HomeScreen.kt` | 重写 | 去标题、收支卡片、日期联动 |
| `HomeViewModel.kt` | 修改 | selectedDate、按日查询 |
| `TransactionRepository.kt` | 修改 | getTransactionsByDateRange |
| `TransactionDao.kt` | 修改 | 新增按日期范围查询 SQL |
| `DatePickerBottomSheet.kt` | 新增 | 滚轮时间选择器 + 月历 + 触觉反馈 |
| `WheelPicker.kt` | 新增 | 可复用滚轮选择器组件 |
| `EditTransactionSheet.kt` | 新增 | 交易修改浮窗 |
| `RecordViewModel.kt` | 修改 | loadTransaction、updateTransaction |
| `RecordBottomSheet.kt` | 修改 | 备注输入框优化 |
| `MainActivity.kt` | 修改 | HazeState 传递、导航栏圆角修复 |
| `ReportScreen.kt` | 重写 | 空状态页面 |
| `ProfileScreen.kt` | 修改 | 去掉 "我的" 大标题 |
| `AnimationPresets.kt` | 微调 | 新增滚轮动画预设 |

---

## 七、依赖新增

```kotlin
// build.gradle.kts 新增
implementation("dev.chrisbanes.haze:haze:1.7.2")
implementation("dev.chrisbanes.haze:haze-materials:1.7.2")
```

AGSL 着色器无需额外依赖，使用 Android 系统 API `android.graphics.RuntimeShader`。
