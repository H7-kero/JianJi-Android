package com.jianji.app.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CategoryExpenseTest {

    @Test
    fun categoryExpense_creation() {
        val ce = CategoryExpense(category = "餐饮", total = 500.0)
        assertThat(ce.category).isEqualTo("餐饮")
        assertThat(ce.total).isEqualTo(500.0)
    }

    @Test
    fun categoryExpense_equality() {
        val ce1 = CategoryExpense(category = "交通", total = 200.0)
        val ce2 = CategoryExpense(category = "交通", total = 200.0)
        assertThat(ce1).isEqualTo(ce2)
    }

    @Test
    fun categoryExpense_zeroTotal() {
        val ce = CategoryExpense(category = "教育", total = 0.0)
        assertThat(ce.total).isEqualTo(0.0)
    }
}

class CategoryTest {

    @Test
    fun category_defaultValues() {
        val category = Category(name = "餐饮", type = "expense")
        assertThat(category.id).isEqualTo(0)
        assertThat(category.name).isEqualTo("餐饮")
        assertThat(category.type).isEqualTo("expense")
        assertThat(category.icon).isEqualTo("category")
        assertThat(category.isSystem).isFalse()
    }

    @Test
    fun category_fullConstructor() {
        val category = Category(id = 1, name = "工资", type = "income", icon = "salary", color = 0xFF00FF00, sortOrder = 1, isSystem = true)
        assertThat(category.id).isEqualTo(1)
        assertThat(category.name).isEqualTo("工资")
        assertThat(category.type).isEqualTo("income")
        assertThat(category.isSystem).isTrue()
    }
}

class BudgetTest {

    @Test
    fun budget_creation() {
        val budget = Budget(amount = 3000.0, period = "monthly", year = 2026, month = 5)
        assertThat(budget.amount).isEqualTo(3000.0)
        assertThat(budget.period).isEqualTo("monthly")
        assertThat(budget.year).isEqualTo(2026)
        assertThat(budget.month).isEqualTo(5)
        assertThat(budget.categoryId).isNull()
    }

    @Test
    fun budget_withCategory() {
        val budget = Budget(amount = 1000.0, period = "monthly", categoryId = 3L, year = 2026, month = 5)
        assertThat(budget.categoryId).isEqualTo(3L)
    }
}
