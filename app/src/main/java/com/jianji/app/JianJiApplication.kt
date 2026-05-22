package com.jianji.app

import android.app.Application
import com.jianji.app.data.local.AppDatabase

class JianJiApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}
