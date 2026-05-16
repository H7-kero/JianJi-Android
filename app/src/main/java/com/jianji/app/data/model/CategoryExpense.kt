package com.jianji.app.data.model

import androidx.room.ColumnInfo

/**
 * 分类支出统计数据类
 * 用于 Room 聚合查询结果
 */
data class CategoryExpense(
    val category: String,
    @ColumnInfo(name = "total")
    val total: Double
)
