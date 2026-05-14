package com.rudra.hisab.di

import android.content.Context
import androidx.room.Room
import com.rudra.hisab.data.local.HisabDatabase
import com.rudra.hisab.data.local.dao.CategoryDao
import com.rudra.hisab.data.local.dao.CustomerDao
import com.rudra.hisab.data.local.dao.DailySnapshotDao
import com.rudra.hisab.data.local.dao.ExpenseDao
import com.rudra.hisab.data.local.dao.ProductDao
import com.rudra.hisab.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HisabDatabase {
        return Room.databaseBuilder(
            context,
            HisabDatabase::class.java,
            HisabDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideProductDao(database: HisabDatabase): ProductDao = database.productDao()

    @Provides
    fun provideCategoryDao(database: HisabDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: HisabDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCustomerDao(database: HisabDatabase): CustomerDao = database.customerDao()

    @Provides
    fun provideExpenseDao(database: HisabDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideDailySnapshotDao(database: HisabDatabase): DailySnapshotDao =
        database.dailySnapshotDao()
}
