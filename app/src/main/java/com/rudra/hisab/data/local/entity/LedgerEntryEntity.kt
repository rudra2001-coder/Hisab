package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_entries",
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
        Index("accountType")
    ]
)
data class LedgerEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountType: LedgerAccountType,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val entryType: LedgerEntryType,
    val amount: Double,
    val description: String = "",
    val referenceId: Long? = null,   // Links to sale / payment / expense id
    val createdAt: Long = System.currentTimeMillis()
)

enum class LedgerAccountType {
    CUSTOMER,
    SUPPLIER,
    GENERAL
}

enum class LedgerEntryType {
    DEBIT,
    CREDIT
}
