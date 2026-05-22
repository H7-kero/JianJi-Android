package com.jianji.app.ui.record

import com.google.common.truth.Truth.assertThat
import com.jianji.app.data.model.Transaction
import com.jianji.app.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class RecordViewModelTest {

    private lateinit var repository: TransactionRepository
    private lateinit var viewModel: RecordViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk(relaxed = true)
        coEvery { repository.insertTransaction(any()) } returns Unit
        coEvery { repository.updateTransaction(any()) } returns Unit
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): RecordViewModel {
        return RecordViewModel(repository)
    }

    @Test
    fun initialState_transactionTypeIsExpense() {
        viewModel = createViewModel()
        assertThat(viewModel.transactionType.value).isEqualTo("expense")
    }

    @Test
    fun initialState_amountIsEmpty() {
        viewModel = createViewModel()
        assertThat(viewModel.amount.value).isEmpty()
    }

    @Test
    fun initialState_selectedCategoryIsDefault() {
        viewModel = createViewModel()
        assertThat(viewModel.selectedCategory.value).isEqualTo("餐饮")
    }

    @Test
    fun initialState_selectedChannelIsWechat() {
        viewModel = createViewModel()
        assertThat(viewModel.selectedChannel.value).isEqualTo("微信")
    }

    @Test
    fun initialState_noteIsEmpty() {
        viewModel = createViewModel()
        assertThat(viewModel.note.value).isEmpty()
    }

    @Test
    fun initialState_isSavedIsFalse() {
        viewModel = createViewModel()
        assertThat(viewModel.isSaved.value).isFalse()
    }

    @Test
    fun setTransactionType_updatesType() {
        viewModel = createViewModel()
        viewModel.setTransactionType("income")
        assertThat(viewModel.transactionType.value).isEqualTo("income")
    }

    @Test
    fun setTransactionType_resetsCategory() {
        viewModel = createViewModel()
        viewModel.selectCategory("交通")
        viewModel.setTransactionType("income")
        assertThat(viewModel.selectedCategory.value).isEqualTo("餐饮")
    }

    @Test
    fun setAmount_updatesAmount() {
        viewModel = createViewModel()
        viewModel.setAmount("128.50")
        assertThat(viewModel.amount.value).isEqualTo("128.50")
    }

    @Test
    fun selectCategory_updatesCategory() {
        viewModel = createViewModel()
        viewModel.selectCategory("交通")
        assertThat(viewModel.selectedCategory.value).isEqualTo("交通")
    }

    @Test
    fun selectCategory_餐饮_resetsSubCategory() {
        viewModel = createViewModel()
        viewModel.selectSubCategory("晚餐")
        viewModel.selectCategory("交通")
        viewModel.selectCategory("餐饮")
        assertThat(viewModel.selectedSubCategory.value).isNotNull()
    }

    @Test
    fun selectSubCategory_updatesSubCategory() {
        viewModel = createViewModel()
        viewModel.selectSubCategory("晚餐")
        assertThat(viewModel.selectedSubCategory.value).isEqualTo("晚餐")
    }

    @Test
    fun selectChannel_updatesChannel() {
        viewModel = createViewModel()
        viewModel.selectChannel("支付宝")
        assertThat(viewModel.selectedChannel.value).isEqualTo("支付宝")
    }

    @Test
    fun setNote_updatesNote() {
        viewModel = createViewModel()
        viewModel.setNote("测试备注")
        assertThat(viewModel.note.value).isEqualTo("测试备注")
    }

    @Test
    fun saveTransaction_withValidAmount_insertsTransaction() = runTest {
        viewModel = createViewModel()
        viewModel.setAmount("50.0")
        viewModel.selectCategory("餐饮")

        viewModel.saveTransaction()

        coVerify { repository.insertTransaction(match { it.amount == 50.0 && it.category == "餐饮" }) }
    }

    @Test
    fun saveTransaction_withValidAmount_setsIsSavedTrue() = runTest {
        viewModel = createViewModel()
        viewModel.setAmount("50.0")

        viewModel.saveTransaction()

        assertThat(viewModel.isSaved.value).isTrue()
    }

    @Test
    fun saveTransaction_withInvalidAmount_doesNotInsert() = runTest {
        viewModel = createViewModel()
        viewModel.setAmount("abc")

        viewModel.saveTransaction()

        coVerify(exactly = 0) { repository.insertTransaction(any()) }
    }

    @Test
    fun saveTransaction_withEmptyAmount_doesNotInsert() = runTest {
        viewModel = createViewModel()

        viewModel.saveTransaction()

        coVerify(exactly = 0) { repository.insertTransaction(any()) }
    }

    @Test
    fun saveTransaction_preservesTransactionType() = runTest {
        viewModel = createViewModel()
        viewModel.setTransactionType("income")
        viewModel.setAmount("5000.0")

        viewModel.saveTransaction()

        coVerify { repository.insertTransaction(match { it.type == "income" }) }
    }

    @Test
    fun resetSavedState_setsIsSavedFalse() = runTest {
        viewModel = createViewModel()
        viewModel.setAmount("50.0")

        viewModel.saveTransaction()

        viewModel.resetSavedState()

        assertThat(viewModel.isSaved.value).isFalse()
    }

    @Test
    fun resetForm_clearsAllFields() {
        viewModel = createViewModel()
        viewModel.setAmount("100.0")
        viewModel.setNote("测试")
        viewModel.selectCategory("交通")

        viewModel.resetForm()

        assertThat(viewModel.amount.value).isEmpty()
        assertThat(viewModel.note.value).isEmpty()
        assertThat(viewModel.selectedCategory.value).isEqualTo("餐饮")
        assertThat(viewModel.selectedChannel.value).isEqualTo("微信")
    }

    @Test
    fun loadTransaction_populatesFields() {
        viewModel = createViewModel()
        val transaction = Transaction(
            id = 5,
            amount = 88.0,
            category = "购物",
            subCategory = "数码",
            channel = "支付宝",
            type = "expense",
            note = "买充电器",
            merchant = "京东",
            timestamp = 1700000000000L
        )

        viewModel.loadTransaction(transaction)

        assertThat(viewModel.editingTransactionId.value).isEqualTo(5L)
        assertThat(viewModel.transactionType.value).isEqualTo("expense")
        assertThat(viewModel.selectedCategory.value).isEqualTo("购物")
        assertThat(viewModel.selectedSubCategory.value).isEqualTo("数码")
        assertThat(viewModel.selectedChannel.value).isEqualTo("支付宝")
        assertThat(viewModel.note.value).isEqualTo("买充电器")
    }

    @Test
    fun updateTransaction_preservesOriginalTimestampAndMerchant() = runTest {
        viewModel = createViewModel()
        val originalTimestamp = 1700000000000L
        val originalMerchant = "京东"
        val transaction = Transaction(
            id = 5,
            amount = 88.0,
            category = "购物",
            type = "expense",
            merchant = originalMerchant,
            timestamp = originalTimestamp
        )

        viewModel.loadTransaction(transaction)
        viewModel.setAmount("100.0")

        viewModel.updateTransaction(5L)

        coVerify {
            repository.updateTransaction(match {
                it.id == 5L && it.timestamp == originalTimestamp && it.merchant == originalMerchant
            })
        }
    }

    @Test
    fun updateTransaction_withInvalidAmount_doesNotUpdate() = runTest {
        viewModel = createViewModel()
        val transaction = Transaction(id = 5, amount = 88.0, category = "购物", type = "expense")
        viewModel.loadTransaction(transaction)
        viewModel.setAmount("invalid")

        viewModel.updateTransaction(5L)

        coVerify(exactly = 0) { repository.updateTransaction(any()) }
    }

    @Test
    fun companionObject_expenseCategories_containsExpectedItems() {
        assertThat(RecordViewModel.expenseCategories).containsExactly(
            "餐饮", "交通", "购物", "娱乐", "医疗", "教育", "居住", "其他"
        )
    }

    @Test
    fun companionObject_incomeCategories_containsExpectedItems() {
        assertThat(RecordViewModel.incomeCategories).containsExactly(
            "工资", "奖金", "投资", "兼职", "其他"
        )
    }

    @Test
    fun companionObject_channels_containsExpectedItems() {
        assertThat(RecordViewModel.channels).containsExactly(
            "微信", "支付宝", "现金", "银行卡", "京东"
        )
    }

    @Test
    fun companionObject_subCategories_餐饮containsMeals() {
        assertThat(RecordViewModel.subCategories["餐饮"]).containsExactly(
            "早餐", "午餐", "晚餐", "宵夜", "零食", "饮品"
        )
    }

    @Test
    fun companionObject_subCategories_unknownCategoryReturnsNull() {
        assertThat(RecordViewModel.subCategories["不存在的分类"]).isNull()
    }

    @Test
    fun getSubCategoriesForCategory_returnsCorrectList() {
        viewModel = createViewModel()
        assertThat(viewModel.getSubCategoriesForCategory("交通")).containsExactly(
            "公交", "地铁", "打车", "加油", "停车"
        )
    }

    @Test
    fun getSubCategoriesForCategory_unknownReturnsEmpty() {
        viewModel = createViewModel()
        assertThat(viewModel.getSubCategoriesForCategory("不存在的分类")).isEmpty()
    }
}
