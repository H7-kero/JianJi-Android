/**
 * 报表页面 ViewModel
 * 
 * 作用：处理日历视图的数据加载和计算
 */
package com.jianji.app.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 单日收支汇总
 */
data class DaySummary(
    val day: Int,
    val expense: Double,
    val income: Double,
    val transactions: List<Transaction>
)

/**
 * 报表 ViewModel
 * 
 * @param repository 交易数据仓库
 */
class ReportViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // 选中的年份
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // 选中的月份（1-12）
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // 指定月份的每日汇总
    private val _daySummaries = MutableStateFlow<List<DaySummary>>(emptyList())
    val daySummaries: StateFlow<List<DaySummary>> = _daySummaries.asStateFlow()

    // 该月总支出
    private val _monthlyExpense = MutableStateFlow(0.0)
    val monthlyExpense: StateFlow<Double> = _monthlyExpense.asStateFlow()

    // 该月总收入
    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()

    init {
        loadMonthData()
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
        loadMonthData()
    }

    fun selectMonth(month: Int) {
        _selectedMonth.value = month
        loadMonthData()
    }

    fun goToPreviousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value = _selectedYear.value - 1
        } else {
            _selectedMonth.value = _selectedMonth.value - 1
        }
        loadMonthData()
    }

    fun goToNextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value = _selectedYear.value + 1
        } else {
            _selectedMonth.value = _selectedMonth.value + 1
        }
        loadMonthData()
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            repository.getMonthlyTransactions(_selectedYear.value, _selectedMonth.value)
                .collect { transactions ->
                    val calendar = Calendar.getInstance()
                    val daysInMonth = getDaysInMonth(_selectedYear.value, _selectedMonth.value)

                    // 按天分组
                    val groupedByDay = transactions.groupBy { tx ->
                        calendar.timeInMillis = tx.timestamp
                        calendar.get(Calendar.DAY_OF_MONTH)
                    }

                    val summaries = (1..daysInMonth).map { day ->
                        val dayTxs = groupedByDay[day] ?: emptyList()
                        val expense = dayTxs.filter { it.type == "expense" }.sumOf { it.amount }
                        val income = dayTxs.filter { it.type == "income" }.sumOf { it.amount }
                        DaySummary(
                            day = day,
                            expense = expense,
                            income = income,
                            transactions = dayTxs.sortedBy { it.timestamp }
                        )
                    }

                    _daySummaries.value = summaries
                    _monthlyExpense.value = summaries.sumOf { it.expense }
                    _monthlyIncome.value = summaries.sumOf { it.income }
                }
        }
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
