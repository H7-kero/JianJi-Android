package com.jianji.app.data.repository

import com.jianji.app.data.local.TransactionDao
import com.jianji.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions()
    }

    fun getTodayTransactions(): Flow<List<Transaction>> {
        val (startTime, endTime) = getTodayTimeRange()
        return transactionDao.getTransactionsByDateRange(startTime, endTime)
    }

    fun getTodayExpense(): Flow<Double> {
        val (startTime, endTime) = getTodayTimeRange()
        return transactionDao.getTotalExpense(startTime, endTime)
            .map { it ?: 0.0 }
    }

    fun getTodayIncome(): Flow<Double> {
        val (startTime, endTime) = getTodayTimeRange()
        return transactionDao.getTotalIncome(startTime, endTime)
            .map { it ?: 0.0 }
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    private fun getTodayTimeRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis
        return Pair(startTime, endTime)
    }
}