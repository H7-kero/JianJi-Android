package com.jianji.app.data.local

import androidx.room.*
import com.jianji.app.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByDateRange(
        startTime: Long, 
        endTime: Long
    ): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE category = :category 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE type = 'expense' 
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double?>

    @Query("""
        SELECT SUM(amount) 
        FROM transactions 
        WHERE type = 'income' 
        AND timestamp BETWEEN :startTime AND :endTime
    """)
    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double?>
}