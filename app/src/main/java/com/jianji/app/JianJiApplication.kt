/**
 * 应用入口类
 * 
 * 作用：
 * 1. 继承 Application，作为应用的入口点
 * 2. @HiltAndroidApp 注解启用 Hilt 依赖注入框架
 * 
 * 为什么需要这个类？
 * Hilt 需要在应用启动时初始化，这个注解告诉 Hilt 在这里开始工作
 * 所有使用 Hilt 的 Activity、Fragment、ViewModel 都需要这个入口
 */
package com.jianji.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 简记应用类
 * 
 * @HiltAndroidApp 会触发 Hilt 的代码生成，包括：
 * - 应用级别的组件
 * - 依赖注入图
 */
@HiltAndroidApp
class JianJiApplication : Application()
