# ProGuard 混淆规则
# 发布 APK 时用于压缩和混淆代码

# 保留 Room 实体类
-keep class com.jianji.app.data.model.** { *; }

# 保留 Hilt 相关类
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
