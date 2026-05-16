/**
 * 根目录构建配置文件
 * 作用：声明项目中使用的 Gradle 插件及其版本
 * 
 * 插件说明：
 * - com.android.application: Android 应用插件，用于构建 APK
 * - org.jetbrains.kotlin.android: Kotlin 语言支持
 * - com.google.dagger.hilt.android: Hilt 依赖注入框架
 * - com.google.devtools.ksp: Kotlin 符号处理，用于 Room 编译时生成代码
 */
plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
}