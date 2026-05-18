/**
 * 应用数据库类
 * 
 * 作用：定义整个应用的数据库结构
 * 包含所有表（Entity）和版本信息
 * 
 * @Database 注解告诉 Room 这是一个数据库类
 * entities: 包含的所有实体类（表）
 * version: 数据库版本号，表结构变更时需要递增
 * exportSchema: 是否导出数据库 schema 文件
 */
package com.jianji.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.model.Category
import com.jianji.app.data.model.Budget

/**
 * 应用数据库
 * 
 * 表列表：
 * - transactions: 交易记录表
 * - categories: 分类表
 * - budgets: 预算表
 * 
 * 这是一个抽象类，Room 会在编译时自动生成实现类
 */
@Database(
    entities = [
        Transaction::class,    // 交易记录表
        Category::class,       // 分类表
        Budget::class          // 预算表
    ],
    version = 2,             // 数据库版本，修改表结构时需要+1
    exportSchema = false     // 不导出 schema 文件（简化开发）
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取交易记录 DAO
     * 抽象方法，Room 会自动实现
     */
    abstract fun transactionDao(): TransactionDao

    // 后续可以添加其他 DAO
    // abstract fun categoryDao(): CategoryDao
    // abstract fun budgetDao(): BudgetDao
}