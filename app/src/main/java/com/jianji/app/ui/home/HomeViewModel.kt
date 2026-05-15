/**
 * 首页 ViewModel
 * 
 * 作用：管理首页的业务逻辑和数据
 * ViewModel 是 MVVM 架构中的 ViewModel 层
 * 
 * 特点：
 * 1. 在配置变更（如旋转屏幕）时不会销毁，保持数据
 * 2. 通过 LiveData/StateFlow 向 UI 层提供数据
 * 3. 处理业务逻辑，如数据转换、计算等
 * 
 * @HiltViewModel 让 Hilt 自动注入 Repository
 * @Inject 构造函数表示需要注入 TransactionRepository
 */
package com.jianji.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页 ViewModel
 * 
 * @param repository 交易记录仓库，通过依赖注入获取
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    /**
     * 今日支出
     * 
     * StateFlow 是 Kotlin 的状态流：
     * - 保存最新值，新订阅者立即收到当前值
     * - 值变化时自动通知所有订阅者
     * - 适合表示 UI 状态
     * 
     * stateIn 将普通 Flow 转换为 StateFlow：
     * - viewModelScope: 在 ViewModel 生命周期内有效
     * - SharingStarted.WhileSubscribed(5000): 有订阅者时活跃，5秒后无订阅者停止
     * - 0.0: 初始值
     */
    val todayExpense: StateFlow<Double> = repository.getTodayExpense()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    /**
     * 今日收入
     */
    val todayIncome: StateFlow<Double> = repository.getTodayIncome()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    /**
     * 今日交易列表
     */
    val todayTransactions: StateFlow<List<Transaction>> = repository.getTodayTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 添加交易记录
     * 
     * viewModelScope.launch 启动一个协程：
     * - 在 ViewModel 销毁时自动取消
     * - 避免内存泄漏
     * 
     * @param transaction 要添加的交易记录
     */
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    /**
     * 删除交易记录
     * @param transaction 要删除的交易记录
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
