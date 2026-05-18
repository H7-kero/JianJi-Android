package com.jianji.app.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class CategoryStat(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class MonthlyStat(
    val month: String,
    val expense: Double,
    val income: Double
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow("month")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthlyTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .map { transactions ->
            val (startTime, endTime) = getCurrentMonthRange()
            transactions.filter { it.timestamp in startTime until endTime }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthlyExpense: StateFlow<Double> = monthlyTransactions
        .map { transactions ->
            transactions.filter { it.type == "expense" }.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val monthlyIncome: StateFlow<Double> = monthlyTransactions
        .map { transactions ->
            transactions.filter { it.type == "income" }.sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val expenseByCategory: StateFlow<List<CategoryStat>> = monthlyTransactions
        .map { transactions ->
            val expenseTransactions = transactions.filter { it.type == "expense" }
            val totalExpense = expenseTransactions.sumOf { it.amount }

            expenseTransactions
                .groupBy { it.category }
                .map { (category, trans) ->
                    val amount = trans.sumOf { it.amount }
                    CategoryStat(
                        category = category,
                        amount = amount,
                        percentage = if (totalExpense > 0) (amount / totalExpense * 100).toFloat() else 0f
                    )
                }
                .sortedByDescending { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomeByCategory: StateFlow<List<CategoryStat>> = monthlyTransactions
        .map { transactions ->
            val incomeTransactions = transactions.filter { it.type == "income" }
            val totalIncome = incomeTransactions.sumOf { it.amount }

            incomeTransactions
                .groupBy { it.category }
                .map { (category, trans) ->
                    val amount = trans.sumOf { it.amount }
                    CategoryStat(
                        category = category,
                        amount = amount,
                        percentage = if (totalIncome > 0) (amount / totalIncome * 100).toFloat() else 0f
                    )
                }
                .sortedByDescending { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSelectedPeriod(period: String) {
        _selectedPeriod.value = period
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }
}