/**
 * App 模块构建配置文件
 * 作用：配置 Android 应用构建设置和依赖库
 * 
 * 主要配置项：
 * - android: SDK 版本、应用信息、构建类型
 * - dependencies: 项目依赖的第三方库
 */
plugins {
    id("com.android.application")      // Android 应用插件
    id("org.jetbrains.kotlin.android") // Kotlin 插件
    id("com.google.dagger.hilt.android") // Hilt 依赖注入插件
    id("com.google.devtools.ksp")      // KSP 代码生成插件（Room 需要）
}

android {
    // 应用命名空间，用于生成 R 类和 BuildConfig
    namespace = "com.jianji.app"
    
    // 编译 SDK 版本（Android 16）
    compileSdk = 36

    defaultConfig {
        // 应用包名，唯一标识应用
        applicationId = "com.jianji.app"
        
        // 最低支持的 Android 版本（Android 10）
        minSdk = 29
        
        // 目标 Android 版本（Android 16）
        targetSdk = 36
        
        // 版本号，每次发布要递增
        versionCode = 1
        
        // 版本名称，用户看到的版本
        versionName = "1.0.0"
    }

    // 构建类型配置
    buildTypes {
        release {
            // 是否启用代码混淆（发布时开启，保护代码）
            isMinifyEnabled = false
            // ProGuard 混淆规则文件
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Java 编译选项
    compileOptions {
        // 使用 Java 21 语法
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    // Kotlin 配置
    kotlin {
        // 使用 JVM 21 工具链
        jvmToolchain(21)
    }
    
    // 构建特性
    buildFeatures {
        // 启用 Jetpack Compose
        compose = true
    }
    
    // Compose 编译器选项
    composeOptions {
        // Compose 编译器版本，必须与 Kotlin 2.0.x 兼容
        kotlinCompilerExtensionVersion = "1.6.1"
    }
}

// 项目依赖
dependencies {
    // ===== 基础库 =====
    // Android 核心库
    implementation("androidx.core:core-ktx:1.15.0")
    
    // 生命周期库，支持 ViewModel 和 LiveData
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    
    // Activity 的 Compose 支持
    implementation("androidx.activity:activity-compose:1.9.2")
    
    // ===== Compose UI 库 =====
    // Compose BOM（Bill of Materials），统一管理 Compose 版本
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    
    // Compose UI 核心
    implementation("androidx.compose.ui:ui")
    // Compose 图形库
    implementation("androidx.compose.ui:ui-graphics")
    // Compose 预览工具
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Material 3 设计组件
    implementation("androidx.compose.material3:material3")
    // Material 图标扩展
    implementation("androidx.compose.material:material-icons-extended")
    
    // Compose 导航
    implementation("androidx.navigation:navigation-compose:2.8.1")
    
    // ViewModel 的 Compose 支持
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    // 生命周期运行时 Compose 支持
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    
    // ===== Hilt 依赖注入 =====
    // Hilt 核心库
    implementation("com.google.dagger:hilt-android:2.51.1")
    // Hilt 编译器（KSP 版本）
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    // Hilt 导航 Compose 支持
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // ===== Room 数据库 =====
    // Room 运行时
    implementation("androidx.room:room-runtime:2.6.1")
    // Room Kotlin 扩展（支持协程）
    implementation("androidx.room:room-ktx:2.6.1")
    // Room 编译器（KSP 版本）
    ksp("androidx.room:room-compiler:2.6.1")
    
    // ===== DataStore 偏好设置 =====
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // ===== Kotlin 协程 =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // ===== 测试库 =====
    // JUnit 单元测试
    testImplementation("junit:junit:4.13.2")
    // Android 测试扩展
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    // Espresso UI 测试
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    // Compose UI 测试
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // Compose 调试工具
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
