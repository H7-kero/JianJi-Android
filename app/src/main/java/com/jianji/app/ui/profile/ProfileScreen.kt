package com.jianji.app.ui.profile

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "auto_accounting")

object AutoAccountingPreferences {
    private val KEY_AUTO_ACCOUNTING = booleanPreferencesKey("auto_accounting_enabled")

    fun isAutoAccountingEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_AUTO_ACCOUNTING] ?: false
        }
    }

    suspend fun setAutoAccountingEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_ACCOUNTING] = enabled
        }
    }
}

object FabPreferences {
    private val KEY_FAB_POSITION = stringPreferencesKey("fab_position")

    fun getFabPosition(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_FAB_POSITION] ?: "left"
        }
    }

    suspend fun setFabPosition(context: Context, position: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FAB_POSITION] = position
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/com.jianji.app.service.AutoAccountingService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.split(':').any { it.equals(service, ignoreCase = true) }
}

fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val autoAccountingEnabled by AutoAccountingPreferences
        .isAutoAccountingEnabled(context)
        .collectAsState(initial = false)

    val fabPosition by FabPreferences
        .getFabPosition(context)
        .collectAsState(initial = "left")

    val isServiceEnabled = remember(autoAccountingEnabled) {
        if (autoAccountingEnabled) isAccessibilityServiceEnabled(context) else false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp)
    ) {
        Text(
            text = "我的",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = LiquidGlassShapes.card, clip = false, ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow)
                .clip(LiquidGlassShapes.card)
                .background(Brush.verticalGradient(colors = listOf(GlassColors.glassHighlight, GlassColors.glassCardBackground)))
                .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.06f), shape = LiquidGlassShapes.card)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                AutoAccountingItem(
                    icon = Icons.Default.FlashOn,
                    title = "自动记账",
                    subtitle = if (isServiceEnabled) "已开启，支付后自动弹出确认" else "开启后支付完成自动弹出记账确认",
                    checked = autoAccountingEnabled,
                    onCheckedChange = { checked ->
                        coroutineScope.launch(Dispatchers.IO) {
                            AutoAccountingPreferences.setAutoAccountingEnabled(context, checked)
                        }
                        if (checked && !isAccessibilityServiceEnabled(context)) {
                            openAccessibilitySettings(context)
                        }
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "无障碍设置",
                    subtitle = "管理自动记账无障碍权限",
                    onClick = { openAccessibilitySettings(context) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = LiquidGlassShapes.card, clip = false, ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow)
                .clip(LiquidGlassShapes.card)
                .background(Brush.verticalGradient(colors = listOf(GlassColors.glassHighlight, GlassColors.glassCardBackground)))
                .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.06f), shape = LiquidGlassShapes.card)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = GlassColors.iosBlue, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FAB 位置", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Text("记账按钮在首页的位置", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FabPositionOption(label = "左下角", isSelected = fabPosition == "left", onClick = { coroutineScope.launch(Dispatchers.IO) { FabPreferences.setFabPosition(context, "left") } })
                    FabPositionOption(label = "右下角", isSelected = fabPosition == "right", onClick = { coroutineScope.launch(Dispatchers.IO) { FabPreferences.setFabPosition(context, "right") } })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("使用说明", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = LiquidGlassShapes.card, clip = false, ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow)
                .clip(LiquidGlassShapes.card)
                .background(Brush.verticalGradient(colors = listOf(GlassColors.glassHighlight, GlassColors.glassCardBackground)))
                .border(width = 0.5.dp, color = Color.Black.copy(alpha = 0.06f), shape = LiquidGlassShapes.card)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InstructionItem(number = "1", text = "开启自动记账开关，系统会引导您授予无障碍权限")
                InstructionItem(number = "2", text = "授权后，当您在微信、支付宝或京东完成支付时，会自动弹出记账确认窗口")
                InstructionItem(number = "3", text = "确认信息无误后点击保存，交易将自动记录；点击取消则放弃本次记账")
                InstructionItem(number = "4", text = "可在确认窗口中重新选择分类和子分类，匹配您的实际消费类型")
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun AutoAccountingItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = GlassColors.iosBlue, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = GlassColors.iosBlue, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun InstructionItem(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(26.dp).clip(RoundedCornerShape(13.dp)).background(GlassColors.iosBlue.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Text(number, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GlassColors.iosBlue)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), lineHeight = 20.sp)
    }
}

@Composable
private fun RowScope.FabPositionOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) GlassColors.iosBlue.copy(alpha = 0.10f) else GlassColors.glassCardBackground,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "fab_option_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) GlassColors.iosBlue else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "fab_option_text"
    )
    Box(modifier = Modifier.weight(1f).clip(LiquidGlassShapes.small).background(bgColor).clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
    }
}