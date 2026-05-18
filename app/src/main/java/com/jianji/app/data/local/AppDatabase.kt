package com.jianji.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.model.Category
import com.jianji.app.data.model.Budget

@Database(
    entities = [Transaction::class, Category::class, Budget::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}