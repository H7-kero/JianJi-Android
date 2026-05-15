/**
 * Gradle 项目设置文件
 * 作用：配置插件仓库和模块依赖
 * 
 * pluginManagement: 插件管理，告诉 Gradle 去哪里下载插件
 * dependencyResolutionManagement: 依赖解析管理，告诉 Gradle 去哪里下载库
 */
pluginManagement {
    repositories {
        google()          // Google 的 Maven 仓库，有 Android 相关库
        mavenCentral()    // Maven 中央仓库，最常用的开源库仓库
        gradlePluginPortal() // Gradle 插件仓库
    }
}

dependencyResolutionManagement {
    // 强制所有模块使用统一的仓库配置，防止子模块私自添加仓库
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// 项目名称
rootProject.name = "JianJi"

// 包含的模块，目前只有 app 模块
include(":app")
