# 简记 (JianJi)

个人记账 Android App，支持自动识别支付页面。

## 功能特性

- 📊 首页概览：今日支出、收入统计
- 📝 快速记账：支持支出/收入记录
- 📈 数据报表：月度/年度统计
- 💰 预算管理：设置月度预算
- 🤖 自动记账：识别微信、支付宝、云闪付支付页面

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **数据库**: Room
- **最低版本**: Android 16 (API 35)

## 项目结构

```
com.jianji.app
├── data                    # 数据层
│   ├── local              # 本地数据库
│   ├── model              # 数据模型
│   └── repository         # 数据仓库
├── di                     # 依赖注入
├── ui                     # 界面层
│   ├── home               # 首页
│   ├── record             # 记账页
│   ├── report             # 报表页
│   └── profile            # 我的页
└── utils                  # 工具类
```

## 使用说明

1. 克隆项目到本地
2. 用 Android Studio 打开
3. 同步 Gradle
4. 运行到模拟器或真机

## 许可证

MIT License
