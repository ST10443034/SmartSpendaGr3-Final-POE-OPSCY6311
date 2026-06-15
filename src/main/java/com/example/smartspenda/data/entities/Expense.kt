package com.example.smartspenda.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val expenseId: Long = 0,
    val userId: Long,
    val categoryId: Long,
    val amount: Double,
    val date: Long,          // day timestamp (00:00)
    val startTime: Long?,    // timestamp with time of day
    val endTime: Long?,
    val description: String,
    val receiptPath: String? = null
)