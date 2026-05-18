package com.jianji.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: String,
    val note: String = "",
    val merchant: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "manual"
)