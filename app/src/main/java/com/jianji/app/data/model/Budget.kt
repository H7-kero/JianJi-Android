/**
 * 预算数据模型
 * 
 * 作用：定义预算的数据结构
 * 支持总预算和分类预算
 */
package com.jianji.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 预算实体类
 * 
 * 属性说明：
 * - id: 唯一标识
 * - amount: 预算金额
 * - period: 预算周期（monthly=月度，yearly=年度）
 * - categoryId: 关联的分类 ID（null 表示总预算）
 * - year: 预算年份
 * - month: 预算月份（年度预算时为 0）
 */
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 预算金额
    val amount: Double,

    // 预算周期："monthly" 月度，"yearly" 年度
    val period: String,

    // 关联的分类 ID，null 表示总预算（不限制分类）
    val categoryId: Long? = null,

    // 预算年份，如 2026
    val year: Int,

    // 预算月份（1-12），年度预算时为 0
    val month: Int
)
