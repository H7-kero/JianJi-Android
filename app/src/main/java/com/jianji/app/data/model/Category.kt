package com.jianji.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val icon: String = "category",
    val color: Long = 0xFF000000,
    val sortOrder: Int = 0,
    val isSystem: Boolean = false
)