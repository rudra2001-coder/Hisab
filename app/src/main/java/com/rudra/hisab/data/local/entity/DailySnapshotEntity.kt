package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_snapshots",
    indices = [
        Index("date", unique = true)
    ]
)
data class DailySnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val totalSales: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val cashReceived: Double = 0.0,
    val creditGiven: Double = 0.0,
    val netProfit: Double = 0.0,
    val openingStockValue: Double = 0.0,
    val closingStockValue: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
