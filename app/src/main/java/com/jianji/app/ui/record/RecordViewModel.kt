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
import java.util.Calendar

/**
 * 记账 ViewModel
 * 
 * @param repository 交易数据仓库
 */
class RecordViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactionType = MutableStateFlow("expense")
    val transactionType: StateFlow<String> = _transactionType.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("餐饮")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSubCategory = MutableStateFlow<String?>(getDefaultSubCategory())
    val selectedSubCategory: StateFlow<String?> = _selectedSubCategory.asStateFlow()

    private val _selectedChannel = MutableStateFlow("微信")
    val selectedChannel: StateFlow<String> = _selectedChannel.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    companion object {
        val expenseCategories = listOf(
            "餐饮", "交通", "购物", "娱乐", "医疗", "教育", "居住", "其他"
        )

        val incomeCategories = listOf(
            "工资", "奖金", "投资", "兼职", "其他"
        )

        val channels = listOf(
            "微信", "支付宝", "现金", "银行卡", "京东"
        )

        val subCategories = mapOf(
            "餐饮" to listOf("早餐", "午餐", "晚餐", "宵夜", "零食", "饮品"),
            "交通" to listOf("公交", "地铁", "打车", "加油", "停车"),
            "购物" to listOf("日用品", "服饰", "数码", "美妆", "家居"),
            "娱乐" to listOf("电影", "游戏", "KTV", "运动", "旅游"),
            "医疗" to listOf("门诊", "药品", "住院", "体检"),
            "教育" to listOf("学费", "书籍", "培训", "文具"),
            "居住" to listOf("房租", "水电", "物业", "维修"),
            "其他" to listOf("其他"),
            "工资" to listOf("月薪"),
            "奖金" to listOf("年终奖", "项目奖"),
            "投资" to listOf("股票", "基金", "利息"),
            "兼职" to listOf("兼职收入")
        )

        fun getDefaultSubCategory(): String? {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when {
                hour in 5..10 -> "早餐"
                hour in 11..13 -> "午餐"
                hour in 14..16 -> null
                hour in 17..20 -> "晚餐"
                else -> "宵夜"
            }
        }
    }

    fun getSubCategoriesForCategory(category: String): List<String> {
        return subCategories[category] ?: emptyList()
    }

    fun setTransactionType(type: String) {
        _transactionType.value = type
        _selectedCategory.value = "餐饮"
        _selectedSubCategory.value = getDefaultSubCategory()
        _selectedChannel.value = "微信"
    }

    fun setAmount(amount: String) {
        _amount.value = amount
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (category == "餐饮") {
            _selectedSubCategory.value = getDefaultSubCategory()
        }
    }

    fun selectSubCategory(subCategory: String?) {
        _selectedSubCategory.value = subCategory
    }

    fun selectChannel(channel: String) {
        _selectedChannel.value = channel
    }

    fun setNote(note: String) {
        _note.value = note
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val amountValue = _amount.value.toDoubleOrNull() ?: return@launch
            val category = _selectedCategory.value ?: return@launch

            val transaction = Transaction(
                amount = amountValue,
                category = category,
                subCategory = _selectedSubCategory.value,
                channel = _selectedChannel.value,
                type = _transactionType.value,
                note = _note.value
            )

            repository.insertTransaction(transaction)
            _isSaved.value = true
        }
    }

    fun resetSavedState() {
        _isSaved.value = false
    }

    fun resetForm() {
        _amount.value = ""
        _selectedCategory.value = "餐饮"
        _selectedSubCategory.value = getDefaultSubCategory()
        _selectedChannel.value = "微信"
        _note.value = ""
    }

    private val _editingTransactionId = MutableStateFlow<Long?>(null)
    val editingTransactionId: StateFlow<Long?> = _editingTransactionId.asStateFlow()

    private val _originalTimestamp = MutableStateFlow(0L)
    private val _originalMerchant = MutableStateFlow<String?>(null)

    fun loadTransaction(transaction: Transaction) {
        _editingTransactionId.value = transaction.id
        _originalTimestamp.value = transaction.timestamp
        _originalMerchant.value = transaction.merchant
        _transactionType.value = transaction.type
        _selectedCategory.value = transaction.category
        _selectedSubCategory.value = transaction.subCategory
        _selectedChannel.value = transaction.channel ?: "微信"
        _note.value = transaction.note
    }

    fun updateTransaction(originalId: Long) {
        viewModelScope.launch {
            val amountValue = _amount.value.toDoubleOrNull() ?: return@launch
            val category = _selectedCategory.value ?: return@launch

            val updated = Transaction(
                id = originalId,
                amount = amountValue,
                category = category,
                subCategory = _selectedSubCategory.value,
                channel = _selectedChannel.value,
                type = _transactionType.value,
                note = _note.value,
                merchant = _originalMerchant.value,
                timestamp = _originalTimestamp.value,
                source = "manual"
            )

            repository.updateTransaction(updated)
            _isSaved.value = true
        }
    }
}
