package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ExpenseCategory {
    TRANSPORT,
    LABOR,
    RENT,
    UTILITY,
    PURCHASE,
    OTHER
}

@Entity(
    tableName = "expenses",
    indices = [
        Index("date"),
        Index("categoryId")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: ExpenseCategory = ExpenseCategory.OTHER,
    val amount: Double = 0.0,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
)
