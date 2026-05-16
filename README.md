# 简记 (JianJi) - 个人记账 Android 应用

一款简洁易用的个人记账应用，支持自动识别微信、支付宝、云闪付支付页面。

## ✨ 功能特性

- 📊 **首页概览**：今日支出、收入统计一目了然
- 📝 **快速记账**：支持支出/收入记录
- 📈 **数据报表**：月度/年度统计，图表展示
- 💰 **预算管理**：设置月度预算，超支提醒
- 🤖 **自动记账**：识别微信、支付宝、云闪付支付页面（开发中）

## 🛠 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose（现代 Android UI 框架）
- **架构**: MVVM + Clean Architecture（分层架构）
- **依赖注入**: Hilt（自动管理对象创建）
- **数据库**: Room（本地数据存储）
- **最低版本**: Android 16 (API 35)

## 📱 快速开始

### 方法一：Android Studio（推荐）

1. **打开项目**
   - 打开 Android Studio
   - 选择 "Open" 或 "Import Project"
   - 选择项目文件夹

2. **等待同步**
   - Android Studio 会自动下载 Gradle 和依赖
   - 进度条走完后，点击右上角的 "Sync Now"

3. **运行应用**
   - 连接模拟器或真机
   - 点击工具栏的绿色三角形 ▶️ 或按 Shift + F10

### 方法二：命令行

```bash
# 进入项目目录
cd JianJi-Android

# 方式1：使用 Android Studio 生成的 wrapper
chmod +x gradlew
./gradlew assembleDebug

# 方式2：先安装 Gradle
gradle wrapper
./gradlew assembleDebug
```

## 📂 项目结构

```
com.jianji.app/
├── data/                      # 数据层
│   ├── local/               # 数据库相关
│   │   ├── AppDatabase.kt   # 数据库配置
│   │   └── TransactionDao.kt # 数据操作方法
│   ├── model/               # 数据模型
│   │   ├── Transaction.kt   # 交易记录
│   │   ├── Category.kt      # 分类
│   │   └── Budget.kt        # 预算
│   └── repository/          # 数据仓库
│       └── TransactionRepository.kt
├── di/                       # 依赖注入
│   └── DatabaseModule.kt     # 数据库配置
├── ui/                       # 界面层
│   └── home/                # 首页模块
│       ├── HomeScreen.kt    # 界面
│       └── HomeViewModel.kt # 逻辑
└── JianJiApplication.kt     # 应用入口
```

## 🧱 架构说明

### MVVM 架构（Model-View-ViewModel）

- **Model（数据层）**: 处理数据存储和获取
  - Room 数据库存储数据
  - Repository 封装数据操作

- **View（界面层）**: 显示数据
  - Compose UI 描述界面
  - 观察 ViewModel 的状态

- **ViewModel（逻辑层）**: 连接界面和数据
  - 管理界面状态
  - 处理用户操作

### 依赖注入（Hilt）

Hilt 就像一个"自动工厂"：
- 自动创建对象
- 自动注入到需要的地方
- 避免手动 `new` 对象

## 🔧 常见问题

### 1. 同步失败怎么办？

- 检查网络连接
- 打开设置：File → Settings → Gradle
- 确保 Gradle 版本正确（项目需要 8.5）
- 点击 "Retry" 重试

### 2. 模拟器无法启动？

- 确保已安装 Android SDK
- 检查 HAXM 或 WHPX 是否启用
- 尝试使用真机调试

### 3. 代码报错找不到类？

- 点击 "Sync Project with Gradle Files"
- 检查 build.gradle.kts 依赖是否完整
- 尝试 File → Invalidate Caches → Restart

## 📝 开发指南

### 添加新页面

1. 在 `app/src/main/java/com/jianji/app/ui/` 下创建新文件夹
2. 创建 `NewScreen.kt` 界面文件
3. 创建 `NewViewModel.kt` 逻辑文件
4. 在 `MainActivity.kt` 中添加导航路由

### 添加新功能

1. 在 `data/model/` 添加数据模型
2. 在 `data/local/` 添加数据库操作
3. 在 `data/repository/` 添加仓库方法
4. 在 ViewModel 中调用仓库

## 📄 许可证

MIT License - 自由使用、修改和分发

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代 UI 工具包
- [Room](https://developer.android.com/jetpack/room) - 数据库解决方案
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) - 依赖注入

---

💡 **提示**: 这是专为 0 代码基础用户设计的记账应用开发教程。如有问题，欢迎提交 Issue！
