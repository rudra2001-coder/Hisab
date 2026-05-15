package com.rudra.hisab.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [
        Index("phone", unique = true),
        Index("name")
    ]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val companyName: String = "",
    val totalDue: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
