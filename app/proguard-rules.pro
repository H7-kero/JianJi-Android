# ProGuard rules for JianJi

# Keep Room entities
-keep class com.jianji.app.data.model.** { *; }

# Keep Hilt classes
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
