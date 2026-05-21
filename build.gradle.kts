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
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
}
