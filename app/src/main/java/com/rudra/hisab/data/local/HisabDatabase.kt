package com.rudra.hisab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudra.hisab.data.local.converter.Converters
import com.rudra.hisab.data.local.dao.CategoryDao
import com.rudra.hisab.data.local.dao.CustomerDao
import com.rudra.hisab.data.local.dao.DailySnapshotDao
import com.rudra.hisab.data.local.dao.ExpenseDao
import com.rudra.hisab.data.local.dao.ProductDao
import com.rudra.hisab.data.local.dao.TransactionDao
import com.rudra.hisab.data.local.entity.CategoryEntity
import com.rudra.hisab.data.local.entity.CustomerEntity
import com.rudra.hisab.data.local.entity.DailySnapshotEntity
import com.rudra.hisab.data.local.entity.ExpenseEntity
import com.rudra.hisab.data.local.entity.ProductEntity
import com.rudra.hisab.data.local.entity.TransactionEntity

@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        CustomerEntity::class,
        ExpenseEntity::class,
        DailySnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HisabDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customerDao(): CustomerDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun dailySnapshotDao(): DailySnapshotDao

    companion object {
        const val DATABASE_NAME = "hisab_database"
    }
}
