package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_transactions",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("productId"),
        Index("createdAt"),
        Index("type")
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val type: StockTransactionType,
    val quantity: Double,
    val previousStock: Double,
    val newStock: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class StockTransactionType {
    STOCK_IN,   // Purchase / restock
    STOCK_OUT,  // Sale deduction
    STOCK_ADJUST, // Manual correction
    STOCK_LOSS  // Damaged / expired
}
