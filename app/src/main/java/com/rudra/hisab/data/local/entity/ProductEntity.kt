package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("supplierId"),
        Index("name"),
        Index("barcode")
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nameBangla: String,
    val unit: String = "piece",
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val lowStockThreshold: Double = 10.0,
    val categoryId: Long? = null,
    val supplierId: Long? = null,
    val barcode: String = "",
    val batchNumber: String = "",
    val expiryDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
