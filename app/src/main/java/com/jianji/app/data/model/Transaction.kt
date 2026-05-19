/**
 * 交易记录数据模型
 * 
 * 作用：定义一笔交易（支出或收入）的数据结构
 * 这个类会被 Room 数据库映射为数据库表
 * 
 * @Entity: Room 注解，表示这是一个数据库表
 * tableName: 表名为 "transactions"
 */
package com.jianji.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 交易记录实体类
 * 
 * 属性说明：
 * - id: 唯一标识，自动递增
 * - amount: 金额（元）
 * - category: 分类（餐饮、交通、购物等）
 * - type: 类型（expense=支出，income=收入）
 * - note: 备注（可选）
 * - merchant: 商户名称（可选，自动记账时填充）
 * - timestamp: 时间戳（毫秒）
 * - source: 来源（manual=手动，auto=自动记账）
 */
@Entity(tableName = "transactions")
data class Transaction(
    // 主键，自动递增
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 交易金额，Double 类型支持小数（如 35.50）
    val amount: Double,

    // 分类名称，如"餐饮"、"交通"、"购物"
    val category: String,

    // 子分类名称，如"早餐"、"午餐"（可选）
    val subCategory: String? = null,

    // 支付渠道，如"微信"、"支付宝"、"京东"、"其他"
    val channel: String? = null,

    // 交易类型："expense" 表示支出，"income" 表示收入
    val type: String,

    // 备注，默认为空字符串
    val note: String = "",

    // 商户名称，可为 null（手动记账时可能没有）
    val merchant: String? = null,

    // 时间戳，默认为当前时间（System.currentTimeMillis() 返回毫秒）
    val timestamp: Long = System.currentTimeMillis(),

    // 记录来源："manual" 手动记账，"auto" 自动记账
    val source: String = "manual"
)
