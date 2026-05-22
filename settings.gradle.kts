/**
 * Gradle 项目设置文件
 * 作用：配置插件仓库和模块依赖
 * 
 * pluginManagement: 插件管理，告诉 Gradle 去哪里下载插件
 * dependencyResolutionManagement: 依赖解析管理，告诉 Gradle 去哪里下载库
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// 项目名称
rootProject.name = "JianJi"

// 包含的模块，目前只有 app 模块
include(":app")
