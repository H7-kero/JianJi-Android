/**
 * 记账页面 ViewModel
 * 
 * 作用：处理记账页面的业务逻辑
 */
package com.jianji.app.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 记账 ViewModel
 * 
 * @param repository 交易数据仓库
 */
class RecordViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    // 交易类型：支出或收入
    private val _transactionType = MutableStateFlow("expense")
    val transactionType: StateFlow<String> = _transactionType.asStateFlow()

    // 金额
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    // 选中的分类
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // 选中的子分类
    private val _selectedSubCategory = MutableStateFlow<String?>(null)
    val selectedSubCategory: StateFlow<String?> = _selectedSubCategory.asStateFlow()

    // 备注
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    // 保存状态
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    // 支出分类列表
    val expenseCategories = listOf(
        "餐饮", "交通", "购物", "娱乐", "医疗", "教育", "居住", "其他"
    )

    // 收入分类列表
    val incomeCategories = listOf(
        "工资", "奖金", "投资", "兼职", "其他"
    )

    // 子分类映射
    val subCategories = mapOf(
        "餐饮" to listOf("早餐", "午餐", "晚餐", "宵夜", "饮料", "零食", "水果"),
        "交通" to listOf("充电", "停车费", "过路费", "地铁", "打车", "自行车")
    )

    /**
     * 更新交易类型
     */
    fun setTransactionType(type: String) {
        _transactionType.value = type
        _selectedCategory.value = null
        _selectedSubCategory.value = null
    }

    /**
     * 更新金额
     */
    fun setAmount(amount: String) {
        _amount.value = amount
    }

    /**
     * 选择分类
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category
        // 清除之前选择的子分类
        _selectedSubCategory.value = null
    }

    /**
     * 选择子分类
     */
    fun selectSubCategory(subCategory: String?) {
        _selectedSubCategory.value = subCategory
    }

    /**
     * 更新备注
     */
    fun setNote(note: String) {
        _note.value = note
    }

    /**
     * 保存交易记录
     */
    fun saveTransaction() {
        viewModelScope.launch {
            val amountValue = _amount.value.toDoubleOrNull() ?: return@launch
            val category = _selectedCategory.value ?: return@launch

            val transaction = Transaction(
                amount = amountValue,
                category = category,
                subCategory = _selectedSubCategory.value,
                type = _transactionType.value,
                note = _note.value
            )

            repository.insertTransaction(transaction)
            _isSaved.value = true
        }
    }

    /**
     * 重置保存状态
     */
    fun resetSavedState() {
        _isSaved.value = false
    }

    /**
     * 重置表单
     */
    fun resetForm() {
        _amount.value = ""
        _selectedCategory.value = null
        _selectedSubCategory.value = null
        _note.value = ""
    }
}
