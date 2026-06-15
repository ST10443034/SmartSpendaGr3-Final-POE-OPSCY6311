package com.example.smartspenda.data.database

import androidx.room.*
import com.example.smartspenda.data.entities.Expense

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesBetween(userId: Long, startDate: Long, endDate: Long): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpentForCategory(userId: Long, categoryId: Long, startDate: Long, endDate: Long): Double?
}