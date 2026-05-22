package com.jianji.app.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TransactionTest {

    @Test
    fun transaction_defaultValues() {
        val transaction = Transaction(amount = 50.0, category = "餐饮", type = "expense")
        assertThat(transaction.id).isEqualTo(0)
        assertThat(transaction.amount).isEqualTo(50.0)
        assertThat(transaction.category).isEqualTo("餐饮")
        assertThat(transaction.type).isEqualTo("expense")
        assertThat(transaction.subCategory).isNull()
        assertThat(transaction.channel).isNull()
        assertThat(transaction.note).isEmpty()
        assertThat(transaction.merchant).isNull()
        assertThat(transaction.source).isEqualTo("manual")
        assertThat(transaction.timestamp).isGreaterThan(0L)
    }

    @Test
    fun transaction_fullConstructor() {
        val timestamp = 1700000000000L
        val transaction = Transaction(
            id = 1,
            amount = 128.50,
            category = "购物",
            subCategory = "数码",
            channel = "支付宝",
            type = "expense",
            note = "买了个充电器",
            merchant = "京东",
            timestamp = timestamp,
            source = "auto"
        )
        assertThat(transaction.id).isEqualTo(1)
        assertThat(transaction.amount).isEqualTo(128.50)
        assertThat(transaction.category).isEqualTo("购物")
        assertThat(transaction.subCategory).isEqualTo("数码")
        assertThat(transaction.channel).isEqualTo("支付宝")
        assertThat(transaction.type).isEqualTo("expense")
        assertThat(transaction.note).isEqualTo("买了个充电器")
        assertThat(transaction.merchant).isEqualTo("京东")
        assertThat(transaction.timestamp).isEqualTo(timestamp)
        assertThat(transaction.source).isEqualTo("auto")
    }

    @Test
    fun transaction_dataClassEquality() {
        val timestamp = 1700000000000L
        val t1 = Transaction(id = 1, amount = 50.0, category = "餐饮", type = "expense", timestamp = timestamp)
        val t2 = Transaction(id = 1, amount = 50.0, category = "餐饮", type = "expense", timestamp = timestamp)
        assertThat(t1).isEqualTo(t2)
    }

    @Test
    fun transaction_dataClassCopy() {
        val original = Transaction(id = 1, amount = 50.0, category = "餐饮", type = "expense")
        val modified = original.copy(amount = 100.0, category = "交通")
        assertThat(modified.amount).isEqualTo(100.0)
        assertThat(modified.category).isEqualTo("交通")
        assertThat(modified.id).isEqualTo(original.id)
        assertThat(modified.type).isEqualTo(original.type)
    }

    @Test
    fun transaction_expenseType() {
        val expense = Transaction(amount = 30.0, category = "交通", type = "expense")
        assertThat(expense.type).isEqualTo("expense")
    }

    @Test
    fun transaction_incomeType() {
        val income = Transaction(amount = 10000.0, category = "工资", type = "income")
        assertThat(income.type).isEqualTo("income")
    }

    @Test
    fun transaction_zeroAmount() {
        val transaction = Transaction(amount = 0.0, category = "其他", type = "expense")
        assertThat(transaction.amount).isEqualTo(0.0)
    }

    @Test
    fun transaction_negativeAmount() {
        val transaction = Transaction(amount = -50.0, category = "其他", type = "expense")
        assertThat(transaction.amount).isEqualTo(-50.0)
    }

    @Test
    fun transaction_largeAmount() {
        val transaction = Transaction(amount = 999999.99, category = "工资", type = "income")
        assertThat(transaction.amount).isWithin(0.01).of(999999.99)
    }
}
