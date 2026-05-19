package com.jianji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.jianji.app.data.local.AppDatabase
import com.jianji.app.data.repository.TransactionRepository

class ConfirmTransactionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra("type") ?: "expense"
        val amount = intent.getDoubleExtra("amount", 0.0)
        val channel = intent.getStringExtra("channel") ?: "微信"
        val category = intent.getStringExtra("category") ?: "其他"
        val subCategory = intent.getStringExtra("subCategory")
        val merchant = intent.getStringExtra("merchant")

        val database = AppDatabase.getDatabase(this)
        val repository = TransactionRepository(database.transactionDao())

        setContent {
            MaterialTheme {
                ConfirmTransactionScreen(
                    type = type,
                    amount = amount,
                    channel = channel,
                    category = category,
                    subCategory = subCategory,
                    merchant = merchant,
                    repository = repository,
                    onDismiss = { finish() }
                )
            }
        }
    }
}
