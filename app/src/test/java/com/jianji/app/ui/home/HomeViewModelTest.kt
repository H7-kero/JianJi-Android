package com.jianji.app.ui.home

import com.google.common.truth.Truth.assertThat
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HomeViewModelTest {

    private lateinit var repository: TransactionRepository
    private lateinit var viewModel: HomeViewModel

    private val testTransaction = Transaction(
        id = 1,
        amount = 50.0,
        category = "餐饮",
        type = "expense",
        timestamp = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk(relaxed = true)
        every { repository.getTodayExpense() } returns flowOf(100.0)
        every { repository.getTodayIncome() } returns flowOf(5000.0)
        every { repository.getExpenseByDateRange(any(), any()) } returns flowOf(100.0)
        every { repository.getIncomeByDateRange(any(), any()) } returns flowOf(5000.0)
        every { repository.getTransactionsByDateRange(any(), any()) } returns flowOf(listOf(testTransaction))
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(repository)
    }

    @Test
    fun initialState_selectedDateIsToday() {
        viewModel = createViewModel()
        assertThat(viewModel.selectedDate.value).isEqualTo(LocalDate.now())
    }

    @Test
    fun initialState_todayExpenseIsZero() {
        viewModel = createViewModel()
        assertThat(viewModel.todayExpense.value).isEqualTo(0.0)
    }

    @Test
    fun initialState_todayIncomeIsZero() {
        viewModel = createViewModel()
        assertThat(viewModel.todayIncome.value).isEqualTo(0.0)
    }

    @Test
    fun selectDate_updatesSelectedDate() {
        viewModel = createViewModel()
        val targetDate = LocalDate.of(2026, 1, 15)

        viewModel.selectDate(targetDate)

        assertThat(viewModel.selectedDate.value).isEqualTo(targetDate)
    }

    @Test
    fun selectDate_toFutureDate() {
        viewModel = createViewModel()
        val futureDate = LocalDate.of(2030, 12, 31)

        viewModel.selectDate(futureDate)

        assertThat(viewModel.selectedDate.value).isEqualTo(futureDate)
    }

    @Test
    fun selectDate_toPastDate() {
        viewModel = createViewModel()
        val pastDate = LocalDate.of(2020, 1, 1)

        viewModel.selectDate(pastDate)

        assertThat(viewModel.selectedDate.value).isEqualTo(pastDate)
    }

    @Test
    fun startEditing_setsEditingTransaction() {
        viewModel = createViewModel()

        viewModel.startEditing(testTransaction)

        assertThat(viewModel.editingTransaction.value).isEqualTo(testTransaction)
    }

    @Test
    fun stopEditing_clearsEditingTransaction() {
        viewModel = createViewModel()
        viewModel.startEditing(testTransaction)

        viewModel.stopEditing()

        assertThat(viewModel.editingTransaction.value).isNull()
    }

    @Test
    fun deleteTransaction_callsRepositoryDelete() = runTest {
        viewModel = createViewModel()

        viewModel.deleteTransaction(testTransaction)

        coVerify { repository.deleteTransaction(testTransaction) }
    }

    @Test
    fun editingTransaction_initiallyNull() {
        viewModel = createViewModel()
        assertThat(viewModel.editingTransaction.value).isNull()
    }

    @Test
    fun dayExpense_reflectsSelectedDate() {
        every { repository.getExpenseByDateRange(any(), any()) } returns flowOf(200.0)
        viewModel = createViewModel()

        viewModel.selectDate(LocalDate.of(2026, 5, 1))

        assertThat(viewModel.dayExpense.value).isEqualTo(0.0)
    }

    @Test
    fun dayIncome_reflectsSelectedDate() {
        every { repository.getIncomeByDateRange(any(), any()) } returns flowOf(8000.0)
        viewModel = createViewModel()

        viewModel.selectDate(LocalDate.of(2026, 5, 1))

        assertThat(viewModel.dayIncome.value).isEqualTo(0.0)
    }
}
