/**
 * 交易记录数据仓库
 * 
 * 作用：封装对交易记录的所有数据操作
 * Repository 模式是 MVVM 架构中的数据层
 * 
 * 为什么需要 Repository？
 * 1. 解耦：ViewModel 不直接操作数据库，而是通过 Repository
 * 2. 可测试：方便 mock 数据进行单元测试
 * 3. 可扩展：以后如果要添加网络同步，只需要改 Repository
 * 
 * @Singleton 表示整个应用只有一个 Repository 实例
 * @Inject 构造函数表示 Hilt 会自动注入所需的 DAO
 */
package com.jianji.app.data.repository

import com.jianji.app.data.local.TransactionDao
import com.jianji.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交易记录仓库类
 * 
 * @param transactionDao 通过依赖注入获取的 DAO 对象
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {

    /**
     * 获取所有交易记录
     * @return 交易列表的数据流，数据变化时自动更新
     */
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    /**
     * 获取今日交易记录
     * 
     * 计算今日的时间范围：
     * - 开始时间：今天 00:00:00
     * - 结束时间：明天 00:00:00
     * 
     * @return 今日所有交易记录
     */
    fun getTodayTransactions(): Flow<List<Transaction>> {
        val (startTime, endTime) = getTodayTimeRange()
        return transactionDao.getTransactionsByDateRange(startTime, endTime)
    }

    /**
     * 获取今日支出
     * @return 今日支出金额的数据流
     */
    fun getTodayExpense(): Flow<Double> {
        val (startTime, endTime) = getTodayTimeRange()
        return transactionDao.getTotalExpense(startTime, endTime)
            .map { it ?: 0.0 }
    }

    /**
     * 获取今日收入
     * @return 今日收入金额的数据流
     */
    fun getTodayIncome(): Flow<Double> {
        val (startTime, endTime) = getTodayTimeRange()
        return transactionDao.getTotalIncome(startTime, endTime)
            .map { it ?: 0.0 }
    }

    /**
     * 获取指定月份的所有交易记录
     * @param year 年份
     * @param month 月份，1-12
     */
    fun getMonthlyTransactions(year: Int, month: Int): Flow<List<Transaction>> {
        val (startTime, endTime) = getMonthTimeRange(year, month)
        return transactionDao.getTransactionsByDateRange(startTime, endTime)
    }

    /**
     * 获取月度支出
     * @param year 年份，如 2026
     * @param month 月份，1-12
     * @return 该月总支出
     */
    fun getMonthlyExpense(year: Int, month: Int): Flow<Double> {
        val (startTime, endTime) = getMonthTimeRange(year, month)
        return transactionDao.getTotalExpense(startTime, endTime)
            .map { it ?: 0.0 }
    }

    fun getExpenseByDateRange(startTime: Long, endTime: Long): Flow<Double> {
        return transactionDao.getTotalExpense(startTime, endTime)
            .map { it ?: 0.0 }
    }

    fun getIncomeByDateRange(startTime: Long, endTime: Long): Flow<Double> {
        return transactionDao.getTotalIncome(startTime, endTime)
            .map { it ?: 0.0 }
    }

    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startTime, endTime)
    }

    /**
     * 添加交易记录
     * @param transaction 要添加的交易记录
     */
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    /**
     * 添加交易记录（别名，兼容旧代码）
     */
    suspend fun addTransaction(transaction: Transaction) {
        insertTransaction(transaction)
    }

    /**
     * 获取指定时间之后的交易记录
     * @param startTime 开始时间戳
     */
    fun getTransactionsSince(startTime: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsSince(startTime)
    }

    /**
     * 删除交易记录
     * @param transaction 要删除的交易记录
     */
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    /**
     * 更新交易记录
     * @param transaction 要更新的交易记录
     */
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    // ===== 私有工具方法 =====

    /**
     * 计算今日的时间范围
     * @return Pair(开始时间, 结束时间)，单位毫秒
     */
    private fun getTodayTimeRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        
        // 设置为今天 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        // 设置为明天 00:00:00
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis
        
        return Pair(startTime, endTime)
    }

    /**
     * 计算指定月份的时间范围
     * @param year 年份
     * @param month 月份（1-12）
     * @return Pair(开始时间, 结束时间)
     */
    private fun getMonthTimeRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        
        // 设置为指定年月的 1 号 00:00:00
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        // 设置为下个月 1 号 00:00:00
        calendar.add(Calendar.MONTH, 1)
        val endTime = calendar.timeInMillis
        
        return Pair(startTime, endTime)
    }
}
