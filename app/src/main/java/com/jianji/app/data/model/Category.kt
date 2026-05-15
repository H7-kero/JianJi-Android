/**
 * 分类数据模型
 * 
 * 作用：定义收支分类的数据结构
 * 例如：餐饮、交通、购物、工资等
 */
package com.jianji.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分类实体类
 * 
 * 属性说明：
 * - id: 唯一标识
 * - name: 分类名称
 * - type: 分类类型（expense=支出分类，income=收入分类）
 * - icon: 图标名称（用于显示图标）
 * - color: 颜色（ARGB 格式，如 0xFFFF5722 表示橙色）
 * - sortOrder: 排序顺序（数字越小越靠前）
 * - isSystem: 是否为系统预设（预设分类用户不能删除）
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 分类名称，如"餐饮"、"交通"
    val name: String,

    // 分类类型："expense" 支出分类，"income" 收入分类
    val type: String,

    // 图标名称，用于显示对应的图标
    val icon: String = "category",

    // 颜色值，ARGB 格式，默认黑色
    val color: Long = 0xFF000000,

    // 排序顺序，数字越小越靠前
    val sortOrder: Int = 0,

    // 是否为系统预设分类
    val isSystem: Boolean = false
)
