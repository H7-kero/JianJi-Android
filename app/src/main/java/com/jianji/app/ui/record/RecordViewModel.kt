package com.jianji.app.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactionType = MutableStateFlow("expense")
    val transactionType: StateFlow<String> = _transactionType.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    val expenseCategories = listOf(
        "餐饮", "交通", "购物", "娱乐", "医疗", "教育", "居住", "通讯", "其他"
    )

    val incomeCategories = listOf(
        "工资", "奖金", "投资收益", "兼职", "礼金", "退款", "其他"
    )

    fun setTransactionType(type: String) {
        _transactionType.value = type
        _category.value = ""
    }

    fun setAmount(amount: String) {
        _amount.value = amount
    }

    fun setCategory(category: String) {
        _category.value = category
    }

    fun setNote(note: String) {
        _note.value = note
    }

    fun saveTransaction() {
        val amountValue = _amount.value.toDoubleOrNull() ?: return
        val categoryValue = _category.value

        if (amountValue <= 0 || categoryValue.isEmpty()) return

        viewModelScope.launch {
            val transaction = Transaction(
                amount = amountValue,
                category = categoryValue,
                type = _transactionType.value,
                note = _note.value
            )
            repository.addTransaction(transaction)
            _saveSuccess.value = true
            resetForm()
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    private fun resetForm() {
        _amount.value = ""
        _category.value = ""
        _note.value = ""
    }
}