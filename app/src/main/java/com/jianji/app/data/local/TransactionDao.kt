/**
 * 交易记录数据访问对象（DAO）
 * 
 * 作用：定义对交易记录表的所有数据库操作
 * DAO = Data Access Object，数据访问对象
 * 
 * @Dao 注解告诉 Room 这是一个 DAO 接口
 * 所有方法都会被 Room 自动生成实现
 */
package com.jianji.app.data.local

import androidx.room.*
import com.jianji.app.data.model.CategoryExpense
import com.jianji.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /**
     * 插入一条交易记录
     * @Insert 注解表示这是插入操作
     * suspend 表示这是一个挂起函数，需要在协程中调用
     */
    @Insert
    suspend fun insert(transaction: Transaction)

    /**
     * 删除一条交易记录
     * @Delete 注解表示这是删除操作
     */
    @Delete
    suspend fun delete(transaction: Transaction)

    /**
     * 更新交易记录
     * @Update 注解表示这是更新操作
     */
    @Update
    suspend fun update(transaction: Transaction)

    /**
     * 查询所有交易记录，按时间倒序排列（最新的在前）
     * 
     * @Query 注解用于自定义 SQL 查询
     * "SELECT * FROM transactions" 表示查询 transactions 表的所有字段
     * "ORDER BY timestamp DESC" 表示按时间戳倒序排列
     * 
     * 返回 Flow<List<Transaction>> 表示这是一个数据流
     * 当数据库数据变化时，Flow 会自动 emit 新数据
     * UI 层可以收集这个 Flow 实现自动刷新
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * 按日期范围查询交易记录
     * 
     * @param startTime 开始时间（毫秒时间戳）
     * @param endTime 结束时间（毫秒时间戳）
     * @return 该时间范围内的所有交易记录
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByDateRange(
        startTime: Long, 
        endTime: Long
    ): Flow<List<Transaction>>

    /**
     * 按分类查询交易记录
     * 
     * @param category 分类名称，如"餐饮"
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE category = :category 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    /**
     * 查询指定日期范围内的总支出
     * 
     * COALESCE(SUM(amount), 0) 表示：
     * - 如果没有记录，返回 0 而不是 null
     * - SUM(amount) 计算金额总和
     * 
     * type = 'expense' 只统计支出
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'expense' 
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double>

    /**
     * 查询指定日期范围内的总收入
     * type = 'income' 只统计收入
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) 
        FROM transactions 
        WHERE type = 'income' 
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double>

    /**
     * 按分类统计支出
     * 用于报表页面显示各类别支出占比
     */
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE type = 'expense' 
        AND timestamp BETWEEN :startTime AND :endTime
        GROUP BY category
        ORDER BY total DESC
    """)
    fun getExpenseByCategory(
        startTime: Long, 
        endTime: Long
    ): Flow<List<CategoryExpense>>
}