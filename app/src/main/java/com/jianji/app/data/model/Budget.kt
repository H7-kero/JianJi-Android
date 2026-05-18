package com.jianji.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val period: String,
    val categoryId: Long? = null,
    val year: Int,
    val month: Int
)