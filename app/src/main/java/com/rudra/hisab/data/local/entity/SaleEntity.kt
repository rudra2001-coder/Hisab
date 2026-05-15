package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("customerId"),
        Index("createdAt"),
        Index("paymentStatus")
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String = "",
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class PaymentStatus {
    PAID,
    PARTIAL,
    DUE
}
