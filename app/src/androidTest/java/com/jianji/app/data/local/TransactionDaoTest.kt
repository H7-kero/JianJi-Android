package com.jianji.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jianji.app.data.model.Transaction
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TransactionDao

    private val now = System.currentTimeMillis()
    private val todayStart = run {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    private val todayEnd = todayStart + 24 * 60 * 60 * 1000L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.transactionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_andGetAllTransactions() = runTest {
        val transaction = Transaction(amount = 50.0, category = "餐饮", type = "expense", timestamp = now)
        dao.insert(transaction)

        dao.getAllTransactions().test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].amount).isEqualTo(50.0)
            assertThat(result[0].category).isEqualTo("餐饮")
            awaitComplete()
        }
    }

    @Test
    fun insert_multipleTransactions_returnsAll() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = now))
        dao.insert(Transaction(amount = 5000.0, category = "工资", type = "income", timestamp = now + 1000))

        dao.getAllTransactions().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            awaitComplete()
        }
    }

    @Test
    fun delete_removesTransaction() = runTest {
        val transaction = Transaction(id = 1, amount = 50.0, category = "餐饮", type = "expense", timestamp = now)
        dao.insert(transaction)
        dao.delete(transaction)

        dao.getAllTransactions().test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun update_modifiesTransaction() = runTest {
        val original = Transaction(amount = 50.0, category = "餐饮", type = "expense", timestamp = now)
        dao.insert(original)

        dao.getAllTransactions().test {
            val inserted = awaitItem().first()
            val updated = inserted.copy(amount = 100.0, category = "交通")
            dao.update(updated)
            cancel()
        }

        dao.getAllTransactions().test {
            val result = awaitItem().first()
            assertThat(result.amount).isEqualTo(100.0)
            assertThat(result.category).isEqualTo("交通")
            awaitComplete()
        }
    }

    @Test
    fun getTransactionsByDateRange_returnsOnlyInRange() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = todayStart + 1000))
        dao.insert(Transaction(amount = 5000.0, category = "工资", type = "income", timestamp = 0L))

        dao.getTransactionsByDateRange(todayStart, todayEnd).test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].category).isEqualTo("餐饮")
            awaitComplete()
        }
    }

    @Test
    fun getTransactionsByDateRange_emptyRange_returnsEmpty() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = now))

        dao.getTransactionsByDateRange(0L, 1L).test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun getTotalExpense_returnsSumOfExpenses() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = todayStart + 1000))
        dao.insert(Transaction(amount = 50.0, category = "交通", type = "expense", timestamp = todayStart + 2000))
        dao.insert(Transaction(amount = 5000.0, category = "工资", type = "income", timestamp = todayStart + 3000))

        dao.getTotalExpense(todayStart, todayEnd).test {
            assertThat(awaitItem()).isEqualTo(80.0)
            awaitComplete()
        }
    }

    @Test
    fun getTotalExpense_noExpenses_returnsNull() = runTest {
        dao.insert(Transaction(amount = 5000.0, category = "工资", type = "income", timestamp = todayStart + 1000))

        dao.getTotalExpense(todayStart, todayEnd).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun getTotalIncome_returnsSumOfIncome() = runTest {
        dao.insert(Transaction(amount = 5000.0, category = "工资", type = "income", timestamp = todayStart + 1000))
        dao.insert(Transaction(amount = 500.0, category = "奖金", type = "income", timestamp = todayStart + 2000))
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = todayStart + 3000))

        dao.getTotalIncome(todayStart, todayEnd).test {
            assertThat(awaitItem()).isEqualTo(5500.0)
            awaitComplete()
        }
    }

    @Test
    fun getTotalIncome_noIncome_returnsNull() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = todayStart + 1000))

        dao.getTotalIncome(todayStart, todayEnd).test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun getTransactionsByCategory_returnsOnlyMatchingCategory() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = now))
        dao.insert(Transaction(amount = 50.0, category = "交通", type = "expense", timestamp = now + 1000))

        dao.getTransactionsByCategory("餐饮").test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].category).isEqualTo("餐饮")
            awaitComplete()
        }
    }

    @Test
    fun getExpenseByCategory_groupsCorrectly() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = todayStart + 1000))
        dao.insert(Transaction(amount = 50.0, category = "餐饮", type = "expense", timestamp = todayStart + 2000))
        dao.insert(Transaction(amount = 100.0, category = "交通", type = "expense", timestamp = todayStart + 3000))

        dao.getExpenseByCategory(todayStart, todayEnd).test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].category).isEqualTo("餐饮")
            assertThat(result[0].total).isEqualTo(80.0)
            awaitComplete()
        }
    }

    @Test
    fun getTransactionsSince_returnsOnlyAfterStartTime() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = 1000L))
        dao.insert(Transaction(amount = 50.0, category = "交通", type = "expense", timestamp = 3000L))

        dao.getTransactionsSince(2000L).test {
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat(result[0].timestamp).isEqualTo(3000L)
            awaitComplete()
        }
    }

    @Test
    fun getAllTransactions_orderedByTimestampDesc() = runTest {
        dao.insert(Transaction(amount = 30.0, category = "餐饮", type = "expense", timestamp = 1000L))
        dao.insert(Transaction(amount = 50.0, category = "交通", type = "expense", timestamp = 3000L))
        dao.insert(Transaction(amount = 80.0, category = "购物", type = "expense", timestamp = 2000L))

        dao.getAllTransactions().test {
            val result = awaitItem()
            assertThat(result[0].timestamp).isEqualTo(3000L)
            assertThat(result[1].timestamp).isEqualTo(2000L)
            assertThat(result[2].timestamp).isEqualTo(1000L)
            awaitComplete()
        }
    }
}
