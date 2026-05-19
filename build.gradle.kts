/**
 * 根目录构建配置文件
 * 作用：配置项目级别的构建设置和插件
 * 
 * 主要配置项：
 * - plugins: 声明项目使用的构建插件
 * - allprojects: 所有模块的公共配置
 * - dependencies: 项目级依赖（较少使用）
 */
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
}

allprojects {
    // 配置项目的 Maven 仓库
    repositories {
        // Google Maven 仓库（Android 官方库）
        google()
        // Maven Central（第三方库）
        mavenCentral()
    }
}