package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index("phone", unique = true),
        Index("totalDue")
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val totalDue: Double = 0.0,
    val lastTransactionAt: Long? = null
)
