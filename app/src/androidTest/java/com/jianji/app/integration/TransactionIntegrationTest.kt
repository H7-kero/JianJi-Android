package com.jianji.app.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jianji.app.data.local.AppDatabase
import com.jianji.app.data.local.TransactionDao
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TransactionDao
    private lateinit var repository: TransactionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.transactionDao()
        repository = TransactionRepository(dao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun fullFlow_insertReadUpdateDelete() = runTest {
        val timestamp = System.currentTimeMillis()

        val transaction = Transaction(
            amount = 35.0,
            category = "餐饮",
            subCategory = "午餐",
            channel = "微信",
            type = "expense",
            note = "午饭",
            timestamp = timestamp
        )

        repository.insertTransaction(transaction)

        repository.getAllTransactions().test {
            val inserted = awaitItem().first()
            assertThat(inserted.amount).isEqualTo(35.0)
            assertThat(inserted.category).isEqualTo("餐饮")
            assertThat(inserted.note).isEqualTo("午饭")
            cancel()
        }

        repository.getAllTransactions().test {
            val original = awaitItem().first()
            val updated = original.copy(amount = 40.0, note = "午饭加饮料")
            repository.updateTransaction(updated)
            cancel()
        }

        repository.getAllTransactions().test {
            val afterUpdate = awaitItem().first()
            assertThat(afterUpdate.amount).isEqualTo(40.0)
            assertThat(afterUpdate.note).isEqualTo("午饭加饮料")
            cancel()
        }

        repository.getAllTransactions().test {
            val toDelete = awaitItem().first()
            repository.deleteTransaction(toDelete)
            cancel()
        }

        repository.getAllTransactions().test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun todaySummary_expenseAndIncomeCalculated() = runTest {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 12)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val todayTimestamp = cal.timeInMillis

        repository.insertTransaction(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = todayTimestamp))
        repository.insertTransaction(Transaction(amount = 50.0, category = "交通", type = "expense", timestamp = todayTimestamp + 1000))
        repository.insertTransaction(Transaction(amount = 5000.0, category = "工资", type = "income", timestamp = todayTimestamp + 2000))

        repository.getTodayExpense().test {
            assertThat(awaitItem()).isEqualTo(80.0)
            cancel()
        }

        repository.getTodayIncome().test {
            assertThat(awaitItem()).isEqualTo(5000.0)
            cancel()
        }
    }

    @Test
    fun monthlyTransactions_filtersByMonth() = runTest {
        val mayTimestamp = 1747000000000L
        val juneTimestamp = 1750000000000L

        repository.insertTransaction(Transaction(amount = 100.0, category = "餐饮", type = "expense", timestamp = mayTimestamp))
        repository.insertTransaction(Transaction(amount = 200.0, category = "交通", type = "expense", timestamp = juneTimestamp))

        repository.getMonthlyTransactions(2026, 5).test {
            val result = awaitItem()
            assertThat(result.all { it.timestamp in mayTimestamp..juneTimestamp }).isTrue()
            cancel()
        }
    }

    @Test
    fun emptyDatabase_returnsZeroExpenseAndIncome() = runTest {
        repository.getTodayExpense().test {
            assertThat(awaitItem()).isEqualTo(0.0)
            cancel()
        }

        repository.getTodayIncome().test {
            assertThat(awaitItem()).isEqualTo(0.0)
            cancel()
        }
    }

    @Test
    fun multipleInserts_allPersisted() = runTest {
        val now = System.currentTimeMillis()
        val transactions = (1..10).map { i ->
            Transaction(amount = i * 10.0, category = "餐饮", type = "expense", timestamp = now + i)
        }

        transactions.forEach { repository.insertTransaction(it) }

        repository.getAllTransactions().test {
            assertThat(awaitItem()).hasSize(10)
            cancel()
        }
    }
}
