# 简记 v2 功能迭代 + iOS 26 Liquid Glass 视觉升级 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 11 项功能迭代需求，将 UI 从半透明渐变升级为 Haze + AGSL 液态玻璃效果，重构首页信息架构，补全交易修改等基础功能。

**Architecture:** Haze 1.7.2 负责背景捕获和实时模糊，AGSL RuntimeShader 实现折射/色散/Fresnel/边缘高光后处理。所有玻璃组件统一通过 HazeState 管线渲染。首页新增日期选择浮窗，使用自定义 WheelPicker 组件。交易修改复用记账 UI 组件。

**Tech Stack:** Jetpack Compose (BOM 2024.09.01), Haze 1.7.2, AGSL RuntimeShader (API 33+), Room, DataStore

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `app/build.gradle.kts` | Modify | 添加 Haze 依赖 |
| `ui/theme/GlassmorphismTheme.kt` | Rewrite | Haze + AGSL 液态玻璃组件 |
| `ui/theme/LiquidGlassShader.kt` | Create | AGSL 着色器定义 + RuntimeShader 工具 |
| `ui/theme/AnimationPresets.kt` | Modify | 新增滚轮动画预设 |
| `ui/home/HomeScreen.kt` | Rewrite | 去标题、收支卡片、日期联动、交易点击 |
| `ui/home/HomeViewModel.kt` | Modify | selectedDate、按日查询、编辑状态 |
| `ui/home/DatePickerBottomSheet.kt` | Create | 滚轮时间选择器 + 月历 + 触觉反馈 |
| `ui/home/WheelPicker.kt` | Create | 可复用滚轮选择器组件 |
| `ui/record/EditTransactionSheet.kt` | Create | 交易修改浮窗 |
| `ui/record/RecordBottomSheet.kt` | Modify | 备注输入框优化 |
| `ui/record/RecordViewModel.kt` | Modify | loadTransaction、updateTransaction |
| `ui/report/ReportScreen.kt` | Rewrite | 空状态页面 |
| `ui/profile/ProfileScreen.kt` | Modify | 去掉大标题 |
| `MainActivity.kt` | Modify | HazeState 传递、导航栏圆角修复 |
| `data/repository/TransactionRepository.kt` | Modify | getTransactionsByDateRange |
| `data/local/TransactionDao.kt` | Modify | 新增按日期范围查询（已有，确认可用） |

---

### Task 1: 添加 Haze 依赖

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: 在 build.gradle.kts 中添加 Haze 依赖**

在 `dependencies` 块的 Compose UI 库区域后添加：

```kotlin
    implementation("dev.chrisbanes.haze:haze:1.7.2")
    implementation("dev.chrisbanes.haze:haze-materials:1.7.2")
```

- [ ] **Step 2: 同步 Gradle 确认依赖可解析**

Run: `cd /workspace/JianJi-Android && ./gradlew dependencies --configuration releaseRuntimeClasspath 2>&1 | grep haze | head -5`
Expected: 看到 haze 相关依赖

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "chore: add Haze 1.7.2 dependency for liquid glass effect"
```

---

### Task 2: 创建 AGSL 着色器工具类

**Files:**
- Create: `app/src/main/java/com/jianji/app/ui/theme/LiquidGlassShader.kt`

- [ ] **Step 1: 创建 LiquidGlassShader.kt**

```kotlin
package com.jianji.app.ui.theme

import android.graphics.RuntimeShader
import android.os.Build

object LiquidGlassShader {

    val source = """
        uniform shader composable;
        uniform float2 resolution;
        uniform float refraction;
        uniform float dispersion;
        uniform float fresnelPower;
        uniform float edgeWidth;
        uniform float highlightIntensity;
        uniform float2 lensCenter;
        uniform float cornerRadius;

        float sdfRoundedBox(float2 p, float2 b, float r) {
            float2 q = abs(p) - b + r;
            return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            float2 center = lensCenter;
            float2 fromCenter = uv - center;

            float dist = length(fromCenter);
            float2 dir = normalize(fromCenter + 0.0001);

            float refractedDist = refraction * smoothstep(0.0, 0.5, dist);
            float2 refractedUv = uv + dir * refractedDist * 0.02;

            float aberration = dispersion * 0.003;
            half4 r = composable.eval(refractedUv + float2(aberration, 0.0) * resolution);
            half4 g = composable.eval(refractedUv);
            half4 b = composable.eval(refractedUv - float2(aberration, 0.0) * resolution);

            half4 color = half4(r.r, g.g, b.b, r.a);

            float2 pixelCoord = fragCoord;
            float2 halfRes = resolution * 0.5;
            float2 p = pixelCoord - halfRes;
            float2 boxHalf = halfRes - cornerRadius;
            float sdf = sdfRoundedBox(p, boxHalf, cornerRadius);
            float normalizedSdf = sdf / max(resolution.x, resolution.y);

            float fresnel = pow(1.0 - clamp(abs(normalizedSdf) * 8.0, 0.0, 1.0), fresnelPower);
            color = mix(color, half4(1.0), fresnel * 0.25);

            float edgeSdf = abs(sdf);
            float edgeGlow = 1.0 - smoothstep(0.0, edgeWidth, edgeSdf);
            float topBias = smoothstep(0.0, 0.3, 1.0 - uv.y);
            color = mix(color, half4(1.0), edgeGlow * topBias * highlightIntensity * 0.4);

            float topHighlight = smoothstep(edgeWidth * 2.0, 0.0, sdf) *
                                 smoothstep(0.0, 0.15, uv.y) *
                                 smoothstep(0.15, 0.0, uv.y);
            color = mix(color, half4(1.0), topHighlight * highlightIntensity * 0.5);

            return color;
        }
    """

    fun createShader(): RuntimeShader? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        return try {
            RuntimeShader(source)
        } catch (_: Exception) {
            null
        }
    }

    fun applyShaderParams(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        refraction: Float = 0.3f,
        dispersion: Float = 0.3f,
        fresnelPower: Float = 3.0f,
        edgeWidth: Float = 3.0f,
        highlightIntensity: Float = 0.8f,
        cornerRadius: Float = 20f
    ) {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("refraction", refraction)
        shader.setFloatUniform("dispersion", dispersion)
        shader.setFloatUniform("fresnelPower", fresnelPower)
        shader.setFloatUniform("edgeWidth", edgeWidth)
        shader.setFloatUniform("highlightIntensity", highlightIntensity)
        shader.setFloatUniform("lensCenter", width * 0.5f, height * 0.5f)
        shader.setFloatUniform("cornerRadius", cornerRadius)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/theme/LiquidGlassShader.kt
git commit -m "feat: add AGSL liquid glass shader with refraction, dispersion, fresnel, edge highlight"
```

---

### Task 3: 重写 GlassmorphismTheme.kt 接入 Haze + AGSL

**Files:**
- Rewrite: `app/src/main/java/com/jianji/app/ui/theme/GlassmorphismTheme.kt`

- [ ] **Step 1: 重写 GlassmorphismTheme.kt**

将整个文件替换为 Haze 管线版本。核心变更：
- `GlassColors` 新增 `incomeGreen` 颜色
- `LiquidGlassShapes` 圆角调整为 28/20/16/12
- `GlassCard`/`GlassSurface`/`GlassContainer` 改为接收 `HazeState` 参数
- 内部使用 `hazeEffect` + AGSL 后处理
- 保留 API < 33 的降级路径

```kotlin
package com.jianji.app.ui.theme

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

object GlassColors {
    val glassBackground = Color(0xFFF5F7FA)
    val glassNavBackground = Color(0xE0FFFFFF)
    val glassCardBackground = Color(0xB8FFFFFF)
    val glassSurface = Color(0xA0FFFFFF)
    val glassSurfaceVariant = Color(0x88FFFFFF)
    val glassHighlight = Color.White.copy(alpha = 0.55f)
    val glassShadow = Color.Black.copy(alpha = 0.08f)
    val iosBlue = Color(0xFF007AFF)
    val expenseRed = Color(0xFFFF3B30)
    val incomeGreen = Color(0xFF34C759)
}

object LiquidGlassShapes {
    val large = RoundedCornerShape(28.dp)
    val card = RoundedCornerShape(20.dp)
    val medium = RoundedCornerShape(16.dp)
    val small = RoundedCornerShape(12.dp)
    val circle = CircleShape
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: Shape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    contentColor: Color = Color.Unspecified,
    elevation: Dp = 2.dp,
    borderAlpha: Float = 0.06f,
    cardBody: @Composable ColumnScope.() -> Unit
) {
    val shader = remember { LiquidGlassShader.createShader() }
    val cornerRadiusPx = remember(shape) {
        when (shape) {
            LiquidGlassShapes.card -> 20f
            LiquidGlassShapes.large -> 28f
            LiquidGlassShapes.medium -> 16f
            LiquidGlassShapes.small -> 12f
            else -> 20f
        }
    }

    ElevatedCard(
        modifier = modifier
            .shadow(
                elevation = elevation, shape = shape, clip = false,
                ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow
            )
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(containerColor.copy(alpha = 0.15f))
            )
            .then(
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier.graphicsLayer {
                        LiquidGlassShader.applyShaderParams(
                            shader, size.width, size.height,
                            cornerRadius = cornerRadiusPx
                        )
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier
                }
            )
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.5f), shape = shape),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Transparent,
            contentColor = if (contentColor == Color.Unspecified) contentColorFor(containerColor) else contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        content = {
            val scope = this
            scope.cardBody()
        }
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: Shape = LiquidGlassShapes.card,
    containerColor: Color = GlassColors.glassCardBackground,
    elevation: Dp = 2.dp,
    borderAlpha: Float = 0.06f,
    content: @Composable () -> Unit
) {
    val shader = remember { LiquidGlassShader.createShader() }
    val cornerRadiusPx = remember(shape) {
        when (shape) {
            LiquidGlassShapes.card -> 20f
            LiquidGlassShapes.large -> 28f
            LiquidGlassShapes.medium -> 16f
            LiquidGlassShapes.small -> 12f
            else -> 20f
        }
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation, shape = shape, clip = false,
                ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow
            )
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.ultraThin(containerColor.copy(alpha = 0.15f))
            )
            .then(
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier.graphicsLayer {
                        LiquidGlassShader.applyShaderParams(
                            shader, size.width, size.height,
                            cornerRadius = cornerRadiusPx
                        )
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier.background(containerColor)
                }
            )
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.5f), shape = shape)
    ) {
        content()
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: Shape = LiquidGlassShapes.medium,
    containerColor: Color = GlassColors.glassSurface,
    elevation: Dp = 1.dp,
    borderAlpha: Float = 0.05f,
    content: @Composable () -> Unit
) {
    val shader = remember { LiquidGlassShader.createShader() }
    val cornerRadiusPx = remember(shape) {
        when (shape) {
            LiquidGlassShapes.card -> 20f
            LiquidGlassShapes.large -> 28f
            LiquidGlassShapes.medium -> 16f
            LiquidGlassShapes.small -> 12f
            else -> 16f
        }
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation, shape = shape, clip = false,
                ambientColor = GlassColors.glassShadow, spotColor = GlassColors.glassShadow
            )
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeMaterials.thin(containerColor.copy(alpha = 0.1f))
            )
            .then(
                if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Modifier.graphicsLayer {
                        LiquidGlassShader.applyShaderParams(
                            shader, size.width, size.height,
                            refraction = 0.15f,
                            dispersion = 0.15f,
                            highlightIntensity = 0.5f,
                            cornerRadius = cornerRadiusPx
                        )
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(shader, "composable")
                            .asComposeRenderEffect()
                    }
                } else {
                    Modifier.background(containerColor)
                }
            )
            .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.4f), shape = shape)
    ) {
        content()
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/theme/GlassmorphismTheme.kt
git commit -m "feat: rewrite glass components with Haze + AGSL liquid glass pipeline"
```

---

### Task 4: 创建 WheelPicker 组件

**Files:**
- Create: `app/src/main/java/com/jianji/app/ui/home/WheelPicker.kt`

- [ ] **Step 1: 创建 WheelPicker.kt**

实现可复用的滚轮选择器，支持触觉反馈：

```kotlin
package com.jianji.app.ui.home

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateScroll
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun <T> WheelPicker(
    items: List<T>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    visibleItems: Int = 5,
    label: @Composable (T) -> String
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, selectedIndex - visibleItems / 2))
    val itemHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { itemHeight.toPx() }

    LaunchedEffect(selectedIndex) {
        val targetIndex = selectedIndex - visibleItems / 2
        if (listState.firstVisibleItemIndex != targetIndex || listState.firstVisibleScrollOffset > 5) {
            listState.animateScrollToItem(targetIndex, 0)
        }
    }

    val currentCenterIndex = remember {
        derivedStateOf {
            val centerOffset = listState.firstVisibleScrollOffset / itemHeightPx
            (listState.firstVisibleItemIndex + centerOffset).roundToInt() + visibleItems / 2
        }
    }

    LaunchedEffect(currentCenterIndex.value) {
        val idx = currentCenterIndex.value
        if (idx in items.indices && idx != selectedIndex) {
            onSelectedChange(idx)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }

    Box(modifier = modifier.height(itemHeight * visibleItems)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItems / 2))
        ) {
            items(items.size) { index ->
                val isSelected = index == currentCenterIndex.value
                val distanceFromCenter = kotlin.math.abs(index - currentCenterIndex.value)
                val alpha = if (distanceFromCenter <= visibleItems / 2) {
                    1f - (distanceFromCenter.toFloat() / (visibleItems / 2 + 1))
                } else 0.2f
                val scale = if (isSelected) 1.15f else 0.85f + 0.15f * (1f - distanceFromCenter.toFloat() / (visibleItems / 2 + 1))

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.alpha = alpha
                            this.scaleX = scale
                            this.scaleY = scale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(items[index]),
                        fontSize = if (isSelected) 20.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIdx = currentCenterIndex.value
            if (centerIdx in items.indices) {
                coroutineScope.launch {
                    listState.animateScrollToItem(centerIdx - visibleItems / 2, 0)
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/home/WheelPicker.kt
git commit -m "feat: add WheelPicker component with haptic feedback"
```

---

### Task 5: 创建 DatePickerBottomSheet

**Files:**
- Create: `app/src/main/java/com/jianji/app/ui/home/DatePickerBottomSheet.kt`

- [ ] **Step 1: 创建 DatePickerBottomSheet.kt**

```kotlin
package com.jianji.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBottomSheet(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var innerSelectedDay by remember { mutableStateOf(selectedDate.dayOfMonth) }

    val today = LocalDate.now()
    val currentYear = today.year
    val years = remember { (currentYear - 10..currentYear).toList() }
    val months = remember { (1..12).toList() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = GlassColors.glassNavBackground.copy(alpha = 0.95f),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    currentYearMonth = currentYearMonth.minusMonths(1)
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上个月", tint = MaterialTheme.colorScheme.onBackground)
                }

                Text(
                    text = "${currentYearMonth.year}年${currentYearMonth.monthValue}月",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    enabled = !currentYearMonth.plusMonths(1).isAfter(YearMonth.from(today))
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下个月", tint = if (currentYearMonth.plusMonths(1).isAfter(YearMonth.from(today))) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onBackground)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WheelPicker(
                    items = years,
                    selectedIndex = years.indexOf(currentYearMonth.year).coerceAtLeast(0),
                    onSelectedChange = { idx ->
                        val newYear = years[idx]
                        val maxMonth = if (newYear == today.year) today.monthValue else 12
                        val newMonth = currentYearMonth.monthValue.coerceAtMost(maxMonth)
                        currentYearMonth = YearMonth.of(newYear, newMonth)
                    },
                    modifier = Modifier.weight(1f),
                    label = { "${it}年" }
                )

                WheelPicker(
                    items = months,
                    selectedIndex = currentYearMonth.monthValue - 1,
                    onSelectedChange = { idx ->
                        val newMonth = idx + 1
                        val maxMonth = if (currentYearMonth.year == today.year) today.monthValue else 12
                        if (newMonth <= maxMonth) {
                            currentYearMonth = YearMonth.of(currentYearMonth.year, newMonth)
                        }
                    },
                    modifier = Modifier.weight(0.5f),
                    label = { "${it}月" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val weekDays = DayOfWeek.values().map {
                it.getDisplayName(TextStyle.NARROW, Locale.CHINESE)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            CalendarGrid(
                yearMonth = currentYearMonth,
                selectedDay = innerSelectedDay,
                today = today,
                onDaySelected = { day ->
                    innerSelectedDay = day
                    val newDate = LocalDate.of(currentYearMonth.year, currentYearMonth.monthValue, day)
                    onDateSelected(newDate)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiquidGlassShapes.small)
                    .background(GlassColors.iosBlue.copy(alpha = 0.08f))
                    .clickable {
                        currentYearMonth = YearMonth.from(today)
                        innerSelectedDay = today.dayOfMonth
                        onDateSelected(today)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "今日",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassColors.iosBlue
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDay: Int,
    today: LocalDate,
    onDaySelected: (Int) -> Unit
) {
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
    val daysInMonth = yearMonth.lengthOfMonth()
    val blankCells = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
    val totalCells = blankCells + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < blankCells || cellIndex >= totalCells) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dayNum = cellIndex - blankCells + 1
                        val isToday = today.year == yearMonth.year &&
                                today.monthValue == yearMonth.monthValue &&
                                today.dayOfMonth == dayNum
                        val isSelected = dayNum == selectedDay
                        val isFuture = LocalDate.of(yearMonth.year, yearMonth.monthValue, dayNum).isAfter(today)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> GlassColors.iosBlue.copy(alpha = 0.12f)
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (!isFuture) Modifier.clickable { onDaySelected(dayNum) }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 14.sp,
                                fontWeight = when {
                                    isSelected -> FontWeight.Bold
                                    isToday -> FontWeight.SemiBold
                                    else -> FontWeight.Normal
                                },
                                color = when {
                                    isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                    isSelected -> GlassColors.iosBlue
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/home/DatePickerBottomSheet.kt
git commit -m "feat: add DatePickerBottomSheet with wheel picker, calendar grid, haptic feedback, today button"
```

---

### Task 6: 修改 HomeViewModel 支持日期选择和编辑

**Files:**
- Modify: `app/src/main/java/com/jianji/app/ui/home/HomeViewModel.kt`

- [ ] **Step 1: 重写 HomeViewModel.kt**

```kotlin
package com.jianji.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class HomeViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val todayExpense: StateFlow<Double> = repository.getTodayExpense()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val todayIncome: StateFlow<Double> = repository.getTodayIncome()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val dayExpense: StateFlow<Double> = _selectedDate.flatMapLatest { date ->
        val (start, end) = getDateRange(date)
        repository.getExpenseByDateRange(start, end)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val dayIncome: StateFlow<Double> = _selectedDate.flatMapLatest { date ->
        val (start, end) = getDateRange(date)
        repository.getIncomeByDateRange(start, end)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val transactions: StateFlow<List<Transaction>> = _selectedDate.flatMapLatest { date ->
        val (start, end) = getDateRange(date)
        repository.getTransactionsByDateRange(start, end)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    private val _editingTransaction = MutableStateFlow<Transaction?>(null)
    val editingTransaction: StateFlow<Transaction?> = _editingTransaction.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun startEditing(transaction: Transaction) {
        _editingTransaction.value = transaction
    }

    fun stopEditing() {
        _editingTransaction.value = null
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    private fun getDateRange(date: LocalDate): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.monthValue - 1, date.dayOfMonth, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis
        return Pair(startTime, endTime)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/home/HomeViewModel.kt
git commit -m "feat: HomeViewModel with selectedDate, dayExpense/Income, editing support"
```

---

### Task 7: 扩展 TransactionRepository 按日期查询

**Files:**
- Modify: `app/src/main/java/com/jianji/app/data/repository/TransactionRepository.kt`

- [ ] **Step 1: 在 TransactionRepository 中添加按日期范围查询支出和收入的方法**

在现有方法后添加：

```kotlin
    fun getExpenseByDateRange(startTime: Long, endTime: Long): Flow<Double> {
        return transactionDao.getTotalExpense(startTime, endTime)
            .map { it ?: 0.0 }
    }

    fun getIncomeByDateRange(startTime: Long, endTime: Long): Flow<Double> {
        return transactionDao.getTotalIncome(startTime, endTime)
            .map { it ?: 0.0 }
    }
```

注意：`getTransactionsByDateRange`、`getTotalExpense`、`getTotalIncome` 已存在于 TransactionDao，无需修改 DAO。

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/data/repository/TransactionRepository.kt
git commit -m "feat: add getExpenseByDateRange and getIncomeByDateRange to repository"
```

---

### Task 8: 重写 HomeScreen.kt

**Files:**
- Rewrite: `app/src/main/java/com/jianji/app/ui/home/HomeScreen.kt`

- [ ] **Step 1: 重写 HomeScreen.kt**

核心变更：去标题、收支卡片重构（红/绿色、日期可点击、收入为0隐藏）、日期联动交易列表、交易项可点击编辑

```kotlin
package com.jianji.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import dev.chrisbanes.haze.HazeState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    hazeState: HazeState
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayExpense by viewModel.dayExpense.collectAsState()
    val dayIncome by viewModel.dayIncome.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("M月d日 E", Locale.CHINESE) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        TodaySummaryCard(
            expense = dayExpense,
            income = dayIncome,
            dateText = selectedDate.format(dateFormatter),
            isToday = selectedDate == LocalDate.now(),
            onDateClick = { showDatePicker = true },
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (selectedDate == LocalDate.now()) "今日交易" else "${selectedDate.format(DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE))}交易",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无交易记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(transactions, key = { _, tx -> tx.id }) { _, transaction ->
                    TransactionItem(
                        transaction = transaction,
                        timeFormat = timeFormat,
                        onClick = { viewModel.startEditing(transaction) }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerBottomSheet(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                viewModel.selectDate(date)
            },
            onDismiss = {
                showDatePicker = false
            },
            sheetState = datePickerState
        )
    }
}

@Composable
fun TodaySummaryCard(
    expense: Double,
    income: Double,
    dateText: String,
    isToday: Boolean,
    onDateClick: () -> Unit,
    hazeState: HazeState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = LiquidGlassShapes.card,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.card)
            .hazeEffect(
                state = hazeState,
                style = dev.chrisbanes.haze.materials.HazeMaterials.ultraThin(GlassColors.glassCardBackground.copy(alpha = 0.15f))
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = LiquidGlassShapes.card
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isToday) "今日收支" else "收支",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateText,
                    fontSize = 14.sp,
                    color = GlassColors.iosBlue,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onDateClick() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "支出",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${formatHomeAmount(expense)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassColors.expenseRed
                    )
                }

                if (income > 0) {
                    Spacer(modifier = Modifier.width(24.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "收入",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¥${formatHomeAmount(income)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassColors.incomeGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    timeFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = LiquidGlassShapes.medium,
                clip = false,
                ambientColor = GlassColors.glassShadow,
                spotColor = GlassColors.glassShadow
            )
            .clip(LiquidGlassShapes.medium)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassColors.glassHighlight,
                        GlassColors.glassCardBackground
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = Color.Black.copy(alpha = 0.06f),
                shape = LiquidGlassShapes.medium
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val categoryText = buildString {
                    append(transaction.category)
                    if (transaction.subCategory != null) {
                        append(" · ${transaction.subCategory}")
                    }
                }
                Text(
                    text = categoryText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = timeFormat.format(Date(transaction.timestamp)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.channel != null) {
                        Text(
                            text = transaction.channel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (transaction.note.isNotEmpty()) {
                    Text(
                        text = transaction.note,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            val amountText = if (transaction.type == "expense") {
                "-¥${formatHomeAmount(transaction.amount)}"
            } else {
                "+¥${formatHomeAmount(transaction.amount)}"
            }

            Text(
                text = amountText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.type == "expense") {
                    GlassColors.expenseRed
                } else {
                    GlassColors.incomeGreen
                }
            )
        }
    }
}

private fun formatHomeAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/home/HomeScreen.kt
git commit -m "feat: rewrite HomeScreen - remove title, unified income/expense card, date picker, clickable transactions"
```

---

### Task 9: 创建 EditTransactionSheet

**Files:**
- Create: `app/src/main/java/com/jianji/app/ui/record/EditTransactionSheet.kt`

- [ ] **Step 1: 创建 EditTransactionSheet.kt**

```kotlin
package com.jianji.app.ui.record

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.data.model.Transaction
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    transaction: Transaction,
    viewModel: RecordViewModel,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val transactionType by viewModel.transactionType.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubCategory by viewModel.selectedSubCategory.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val note by viewModel.note.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    val categories = if (transactionType == "expense") {
        RecordViewModel.expenseCategories
    } else {
        RecordViewModel.incomeCategories
    }

    val availableSubCategories = selectedCategory?.let {
        RecordViewModel.subCategories[it]
    }

    var expression by remember { mutableStateOf(formatEditAmount(transaction.amount)) }
    val evaluatedValue = remember(expression) { evaluateExpression(expression) }

    var showCategoryPicker by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            viewModel.resetSavedState()
            onDismiss()
        }
    }

    val saveEnabled = evaluatedValue > 0.0 && selectedCategory != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = GlassColors.glassNavBackground.copy(alpha = 0.92f),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "修改记录",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiquidGlassShapes.small)
                    .background(GlassColors.glassSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "时间",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = timeFormat.format(Date(transaction.timestamp)),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TypeToggle(
                selectedType = transactionType,
                onTypeSelected = { viewModel.setTransactionType(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (expression.isEmpty()) "¥ 0" else "¥ ${formatAmount(evaluatedValue)}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (expression.isNotEmpty()) {
                    Text(
                        text = expression,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                CategorySelectorRow(
                    selectedCategory = selectedCategory,
                    onClick = { showCategoryPicker = true }
                )

                if (availableSubCategories != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SubCategoryChipRow(
                        subCategories = availableSubCategories,
                        selectedSubCategory = selectedSubCategory,
                        onSubCategorySelected = { viewModel.selectSubCategory(it) }
                    )
                }

                if (transactionType == "expense") {
                    Spacer(modifier = Modifier.height(8.dp))
                    ChannelChipRow(
                        channels = RecordViewModel.channels,
                        selectedChannel = selectedChannel,
                        onChannelSelected = { viewModel.selectChannel(it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { viewModel.setNote(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minHeight(48.dp),
                    placeholder = { Text("添加备注...", fontSize = 14.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CalculatorKeyboard(
                expression = expression,
                evaluatedValue = evaluatedValue,
                onKeyPress = { expression += it },
                onBackspace = {
                    if (expression.isNotEmpty()) {
                        expression = expression.dropLast(1)
                    }
                },
                onSave = {
                    viewModel.setAmount(formatAmount(evaluatedValue))
                    viewModel.updateTransaction(transaction.id)
                },
                saveEnabled = saveEnabled
            )
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories,
            onCategorySelected = { category ->
                viewModel.selectCategory(category)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

private fun formatEditAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/record/EditTransactionSheet.kt
git commit -m "feat: add EditTransactionSheet with read-only time, reusable UI components"
```

---

### Task 10: 修改 RecordViewModel 支持编辑

**Files:**
- Modify: `app/src/main/java/com/jianji/app/ui/record/RecordViewModel.kt`

- [ ] **Step 1: 在 RecordViewModel 中添加编辑相关方法**

在 `resetForm()` 方法后添加：

```kotlin
    private val _editingTransactionId = MutableStateFlow<Long?>(null)
    val editingTransactionId: StateFlow<Long?> = _editingTransactionId.asStateFlow()

    fun loadTransaction(transaction: Transaction) {
        _editingTransactionId.value = transaction.id
        _transactionType.value = transaction.type
        _selectedCategory.value = transaction.category
        _selectedSubCategory.value = transaction.subCategory
        _selectedChannel.value = transaction.channel ?: "微信"
        _note.value = transaction.note
    }

    fun updateTransaction(originalId: Long) {
        viewModelScope.launch {
            val amountValue = _amount.value.toDoubleOrNull() ?: return@launch
            val category = _selectedCategory.value ?: return@launch

            val updated = Transaction(
                id = originalId,
                amount = amountValue,
                category = category,
                subCategory = _selectedSubCategory.value,
                channel = _selectedChannel.value,
                type = _transactionType.value,
                note = _note.value,
                source = "manual"
            )

            repository.updateTransaction(updated)
            _isSaved.value = true
        }
    }
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/record/RecordViewModel.kt
git commit -m "feat: add loadTransaction and updateTransaction to RecordViewModel"
```

---

### Task 11: 修改 RecordBottomSheet 备注输入框

**Files:**
- Modify: `app/src/main/java/com/jianji/app/ui/record/RecordBottomSheet.kt`

- [ ] **Step 1: 修改备注输入框**

找到 `OutlinedTextField` 部分，将：

```kotlin
                    OutlinedTextField(
                        value = note,
                        onValueChange = { viewModel.setNote(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        placeholder = { Text("添加备注...", fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
```

替换为：

```kotlin
                    OutlinedTextField(
                        value = note,
                        onValueChange = { viewModel.setNote(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minHeight(48.dp),
                        placeholder = { Text("添加备注...", fontSize = 14.sp) },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/record/RecordBottomSheet.kt
git commit -m "fix: note input field auto-height with maxLines=3 instead of fixed height"
```

---

### Task 12: 重写 ReportScreen 空状态

**Files:**
- Rewrite: `app/src/main/java/com/jianji/app/ui/report/ReportScreen.kt`

- [ ] **Step 1: 重写 ReportScreen.kt**

```kotlin
package com.jianji.app.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jianji.app.ui.theme.GlassColors
import com.jianji.app.ui.theme.LiquidGlassShapes

@Composable
fun ReportScreen(viewModel: ReportViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassColors.glassBackground)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = LiquidGlassShapes.card,
                    clip = false,
                    ambientColor = GlassColors.glassShadow,
                    spotColor = GlassColors.glassShadow
                )
                .clip(LiquidGlassShapes.card)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GlassColors.glassHighlight,
                            GlassColors.glassCardBackground
                        )
                    )
                )
                .border(
                    width = 0.5.dp,
                    color = Color.Black.copy(alpha = 0.06f),
                    shape = LiquidGlassShapes.card
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "报表功能开发中",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "即将支持图表分析和趋势报告",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/report/ReportScreen.kt
git commit -m "feat: replace report screen with empty state placeholder"
```

---

### Task 13: 修改 ProfileScreen 去掉大标题

**Files:**
- Modify: `app/src/main/java/com/jianji/app/ui/profile/ProfileScreen.kt`

- [ ] **Step 1: 删除 "我的" 大标题**

找到并删除 ProfileScreen 中的：

```kotlin
        Text(
            text = "我的",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))
```

替换为：

```kotlin
        Spacer(modifier = Modifier.height(48.dp))
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/ui/profile/ProfileScreen.kt
git commit -m "feat: remove profile page large title"
```

---

### Task 14: 修改 MainActivity — HazeState + 导航栏圆角 + 编辑浮窗

**Files:**
- Modify: `app/src/main/java/com/jianji/app/MainActivity.kt`

- [ ] **Step 1: 添加 HazeState 和编辑浮窗逻辑**

核心变更：
1. 顶层添加 `val hazeState = remember { HazeState() }`
2. 传递 `hazeState` 到 HomeScreen
3. 背景内容标记 `hazeSource`
4. 导航栏指示器圆角改为 16dp
5. 添加编辑浮窗状态
6. 交易金额颜色改为红/绿

在 `JianJiApp` 函数中：

添加 import：
```kotlin
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.jianji.app.ui.record.EditTransactionSheet
import com.jianji.app.ui.record.RecordViewModel
import java.time.LocalDate
```

在 `JianJiApp` 函数体开头添加：
```kotlin
    val hazeState = remember { HazeState() }
    var showEditSheet by remember { mutableStateOf(false) }
    val editingTransaction by homeViewModel.editingTransaction.collectAsState()
```

修改 HorizontalPager 内容，给背景加 hazeSource：
```kotlin
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .navigationBarsPadding()
        ) { page ->
            when (page) {
                0 -> HomeScreen(homeViewModel, hazeState)
                1 -> ReportScreen(reportViewModel)
                2 -> ProfileScreen()
            }
        }
```

在 `if (showRecordSheet)` 块后添加编辑浮窗：
```kotlin
    LaunchedEffect(editingTransaction) {
        if (editingTransaction != null && !showEditSheet) {
            recordViewModel.loadTransaction(editingTransaction!!)
            showEditSheet = true
        }
    }

    if (showEditSheet && editingTransaction != null) {
        val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        EditTransactionSheet(
            transaction = editingTransaction!!,
            viewModel = recordViewModel,
            onDismiss = {
                showEditSheet = false
                homeViewModel.stopEditing()
            },
            sheetState = editSheetState
        )
    }
```

修改导航栏指示器圆角：将 `LiquidGlassShapes.large` 改为 `RoundedCornerShape(16.dp)`：
```kotlin
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                        .size(width = indicatorWidth, height = indicatorHeight)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.06f))
                )
```

修改 FAB 金额颜色和导航栏颜色引用（如果有的话），将 `MaterialTheme.colorScheme.error` 替换为 `GlassColors.expenseRed`，收入蓝色替换为 `GlassColors.incomeGreen`。

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/jianji/app/MainActivity.kt
git commit -m "feat: integrate HazeState, edit sheet, nav bar corner fix, expense/income colors"
```

---

### Task 15: 删除 RenderEffectBlur.kt（如果 GitHub 上仍存在）

**Files:**
- Delete: `app/src/main/java/com/jianji/app/ui/theme/RenderEffectBlur.kt` (on GitHub)

- [ ] **Step 1: 确认 GitHub 上的 RenderEffectBlur.kt 已删除**

如果 GitHub 仓库中仍有此文件，通过 GitHub API 删除它。该文件使用的 `BlurEffect` API 在当前 Compose BOM 中不可用。

- [ ] **Step 2: Commit**

```bash
git rm app/src/main/java/com/jianji/app/ui/theme/RenderEffectBlur.kt
git commit -m "chore: remove RenderEffectBlur.kt (API unavailable in current Compose BOM)"
```

---

### Task 16: 推送所有变更到 GitHub

- [ ] **Step 1: 推送所有文件到 GitHub**

使用 GitHub API 一次性推送所有修改的文件。

- [ ] **Step 2: 通知用户 git pull 并验证编译**

告知用户：
1. 执行 `git pull origin main`
2. 确认本地没有 `RenderEffectBlur.kt`
3. Sync Gradle 后编译运行
