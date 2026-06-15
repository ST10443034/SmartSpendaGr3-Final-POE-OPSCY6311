package com.example.smartspenda.data.database

import androidx.room.*
import com.example.smartspenda.data.entities.Budget

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year AND categoryId IS NULL")
    suspend fun getTotalMonthlyBudget(userId: Long, month: Int, year: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year AND categoryId = :categoryId")
    suspend fun getCategoryBudget(userId: Long, month: Int, year: Int, categoryId: Long): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getAllBudgetsForMonth(userId: Long, month: Int, year: Int): List<Budget>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year AND categoryId IS NOT NULL")
    suspend fun getCategoryBudgetsForMonth(userId: Long, month: Int, year: Int): List<Budget>
}