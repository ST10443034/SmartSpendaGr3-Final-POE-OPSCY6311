package com.example.smartspenda.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Long = 0,
    val userId: Long,
    val month: Int,
    val year: Int,
    val categoryId: Long? = null,   // null = total budget
    val maxAmount: Double = 0.0,     // maximum budget
    val minAmount: Double = 0.0      // minimum goal
)