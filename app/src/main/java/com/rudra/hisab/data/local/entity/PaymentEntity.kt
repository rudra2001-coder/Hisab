package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
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
        Index("customerId"),
        Index("supplierId"),
        Index("createdAt"),
        Index("type")
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: PaymentType,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class PaymentType {
    RECEIVED,   // Money received from customer
    PAID        // Money paid to supplier
}

enum class PaymentMethod {
    CASH,
    MOBILE_BANKING,
    BANK_TRANSFER,
    CHEQUE,
    OTHER
}
