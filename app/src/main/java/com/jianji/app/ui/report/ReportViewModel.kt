/**
 * 报表页面 ViewModel
 * 
 * 作用：处理报表页面的业务逻辑
 */
package com.jianji.app.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 报表 ViewModel
 * 
 * @param repository 交易数据仓库
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("month")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    init {
        loadTransactions()
    }

    fun setPeriod(period: String) {
        _selectedPeriod.value = period
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val startTime = when (_selectedPeriod.value) {
                "week" -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                "month" -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                "year" -> {
                    calendar.set(Calendar.MONTH, 0)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                else -> 0L
            }
            repository.getTransactionsSince(startTime).collect {
                _transactions.value = it
            }
        }
    }

    fun getTotalExpense(): Double {
        return _transactions.value
            .filter { it.type == "expense" }
            .sumOf { it.amount }
    }

    fun getTotalIncome(): Double {
        return _transactions.value
            .filter { it.type == "income" }
            .sumOf { it.amount }
    }

    fun getExpenseByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount }
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }

    fun getIncomeByCategory(): Map<String, Double> {
        return _transactions.value
            .filter { it.type == "income" }
            .groupBy { it.category }
            .mapValues { (_, transactions) ->
                transactions.sumOf { it.amount }
            }
            .toList()
            .sortedByDescending { it.second }
            .toMap()
    }
}