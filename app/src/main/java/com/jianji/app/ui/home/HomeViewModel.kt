package com.jianji.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.CategoryExpense
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar

class HomeViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val todayExpense: StateFlow<Double> = repository.getTodayExpense()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val todayIncome: StateFlow<Double> = repository.getTodayIncome()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val dayExpense: StateFlow<Double> = _selectedDate.flatMapLatest { date ->
        val (start, end) = getDateRange(date)
        repository.getExpenseByDateRange(start, end)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val dayIncome: StateFlow<Double> = _selectedDate.flatMapLatest { date ->
        val (start, end) = getDateRange(date)
        repository.getIncomeByDateRange(start, end)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    val transactions: StateFlow<List<Transaction>> = _selectedDate.flatMapLatest { date ->
        val (start, end) = getDateRange(date)
        repository.getTransactionsByDateRange(start, end)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val categoryExpenses: StateFlow<List<CategoryExpense>> = _selectedDate.flatMapLatest { date ->
        val (start, end) = getDateRange(date)
        repository.getCategoryExpenseByDateRange(start, end)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    private val currentYearMonth: String
        get() = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val monthlyExpense: StateFlow<Double> = flow {
        val yearMonth = YearMonth.now()
        val startOfMonth = yearMonth.atDay(1).atStartOfDay().toInstant(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())).toEpochMilli()
        val endOfMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay().toInstant(java.time.ZoneOffset.systemDefault().rules.getOffset(java.time.Instant.now())).toEpochMilli()
        repository.getExpenseByDateRange(startOfMonth, endOfMonth).collect { emit(it) }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0.0)

    private val _monthlyBudget = MutableStateFlow(repository.getMonthlyBudget(currentYearMonth))
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _editingTransaction = MutableStateFlow<Transaction?>(null)
    val editingTransaction: StateFlow<Transaction?> = _editingTransaction.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun startEditing(transaction: Transaction) {
        _editingTransaction.value = transaction
    }

    fun stopEditing() {
        _editingTransaction.value = null
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    private fun getDateRange(date: LocalDate): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.monthValue - 1, date.dayOfMonth, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endTime = calendar.timeInMillis
        return Pair(startTime, endTime)
    }
}
