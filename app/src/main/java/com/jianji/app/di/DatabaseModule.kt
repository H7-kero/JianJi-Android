/**
 * 数据库依赖注入模块
 * 
 * 作用：告诉 Hilt 如何创建和提供数据库相关的对象
 * Hilt 会自动调用这些方法来获取依赖
 * 
 * @Module 标记这是一个 Hilt 模块
 * @InstallIn(SingletonComponent::class) 表示这些依赖在应用生命周期内有效
 * object 表示单例对象，只创建一次
 */
package com.jianji.app.di

import android.content.Context
import androidx.room.Room
import com.jianji.app.data.local.AppDatabase
import com.jianji.app.data.local.TransactionDao
import com.jianji.app.data.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供数据库实例
     * 
     * @param context 应用上下文，通过 @ApplicationContext 注入
     * @return AppDatabase 实例
     * 
     * @Singleton 表示整个应用只有一个数据库实例
     * @Provides 告诉 Hilt 这个方法提供依赖
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,  // 数据库类
            "jianji_database"         // 数据库文件名
        )
        .fallbackToDestructiveMigration() // 数据库版本升级时删除旧数据（开发时用）
        .build()
    }

    /**
     * 提供交易记录 DAO
     * 
     * @param database 数据库实例，Hilt 会自动传入上面提供的数据库
     * @return TransactionDao 实例
     */
    @Provides
    @Singleton
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    /**
     * 提供交易记录仓库
     * 
     * @param dao DAO 实例，Hilt 会自动传入上面提供的 DAO
     * @return TransactionRepository 实例
     */
    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): TransactionRepository {
        return TransactionRepository(dao)
    }
}
