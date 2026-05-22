package com.jianji.app.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.jianji.app.data.local.TransactionDao
import com.jianji.app.data.model.Transaction

class TransactionRepositoryTest {

    private lateinit var dao: TransactionDao
    private lateinit var repository: TransactionRepository

    private val testTransaction = Transaction(
        id = 1,
        amount = 50.0,
        category = "餐饮",
        subCategory = "午餐",
        channel = "微信",
        type = "expense",
        note = "测试",
        timestamp = 1700000000000L
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = TransactionRepository(dao)
    }

    @Test
    fun getAllTransactions_returnsFlowFromDao() = runTest {
        val transactions = listOf(testTransaction)
        every { dao.getAllTransactions() } returns flowOf(transactions)

        repository.getAllTransactions().test {
            assertThat(awaitItem()).isEqualTo(transactions)
            awaitComplete()
        }
    }

    @Test
    fun getTodayTransactions_callsDaoWithDateRange() = runTest {
        every { dao.getTransactionsByDateRange(any(), any()) } returns flowOf(listOf(testTransaction))

        repository.getTodayTransactions().test {
            val result = awaitItem()
            assertThat(result).contains(testTransaction)
            awaitComplete()
        }
    }

    @Test
    fun getTodayExpense_mapsNullToZero() = runTest {
        every { dao.getTotalExpense(any(), any()) } returns flowOf(null)

        repository.getTodayExpense().test {
            assertThat(awaitItem()).isEqualTo(0.0)
            awaitComplete()
        }
    }

    @Test
    fun getTodayExpense_returnsValue() = runTest {
        every { dao.getTotalExpense(any(), any()) } returns flowOf(150.0)

        repository.getTodayExpense().test {
            assertThat(awaitItem()).isEqualTo(150.0)
            awaitComplete()
        }
    }

    @Test
    fun getTodayIncome_mapsNullToZero() = runTest {
        every { dao.getTotalIncome(any(), any()) } returns flowOf(null)

        repository.getTodayIncome().test {
            assertThat(awaitItem()).isEqualTo(0.0)
            awaitComplete()
        }
    }

    @Test
    fun getTodayIncome_returnsValue() = runTest {
        every { dao.getTotalIncome(any(), any()) } returns flowOf(10000.0)

        repository.getTodayIncome().test {
            assertThat(awaitItem()).isEqualTo(10000.0)
            awaitComplete()
        }
    }

    @Test
    fun insertTransaction_callsDaoInsert() = runTest {
        coEvery { dao.insert(any()) } returns Unit

        repository.insertTransaction(testTransaction)

        coVerify { dao.insert(testTransaction) }
    }

    @Test
    fun addTransaction_delegatesToInsertTransaction() = runTest {
        coEvery { dao.insert(any()) } returns Unit

        repository.addTransaction(testTransaction)

        coVerify { dao.insert(testTransaction) }
    }

    @Test
    fun deleteTransaction_callsDaoDelete() = runTest {
        coEvery { dao.delete(any()) } returns Unit

        repository.deleteTransaction(testTransaction)

        coVerify { dao.delete(testTransaction) }
    }

    @Test
    fun updateTransaction_callsDaoUpdate() = runTest {
        coEvery { dao.update(any()) } returns Unit

        repository.updateTransaction(testTransaction)

        coVerify { dao.update(testTransaction) }
    }

    @Test
    fun getMonthlyTransactions_callsDaoWithMonthRange() = runTest {
        every { dao.getTransactionsByDateRange(any(), any()) } returns flowOf(listOf(testTransaction))

        repository.getMonthlyTransactions(2026, 5).test {
            val result = awaitItem()
            assertThat(result).isNotEmpty()
            awaitComplete()
        }
    }

    @Test
    fun getMonthlyExpense_mapsNullToZero() = runTest {
        every { dao.getTotalExpense(any(), any()) } returns flowOf(null)

        repository.getMonthlyExpense(2026, 5).test {
            assertThat(awaitItem()).isEqualTo(0.0)
            awaitComplete()
        }
    }

    @Test
    fun getExpenseByDateRange_returnsValue() = runTest {
        every { dao.getTotalExpense(any(), any()) } returns flowOf(500.0)

        repository.getExpenseByDateRange(0L, 9999999999999L).test {
            assertThat(awaitItem()).isEqualTo(500.0)
            awaitComplete()
        }
    }

    @Test
    fun getIncomeByDateRange_returnsValue() = runTest {
        every { dao.getTotalIncome(any(), any()) } returns flowOf(8000.0)

        repository.getIncomeByDateRange(0L, 9999999999999L).test {
            assertThat(awaitItem()).isEqualTo(8000.0)
            awaitComplete()
        }
    }

    @Test
    fun getTransactionsByDateRange_returnsFlowFromDao() = runTest {
        every { dao.getTransactionsByDateRange(any(), any()) } returns flowOf(listOf(testTransaction))

        repository.getTransactionsByDateRange(0L, 9999999999999L).test {
            assertThat(awaitItem()).contains(testTransaction)
            awaitComplete()
        }
    }

    @Test
    fun getTransactionsSince_returnsFlowFromDao() = runTest {
        every { dao.getTransactionsSince(any()) } returns flowOf(listOf(testTransaction))

        repository.getTransactionsSince(0L).test {
            assertThat(awaitItem()).contains(testTransaction)
            awaitComplete()
        }
    }
}
