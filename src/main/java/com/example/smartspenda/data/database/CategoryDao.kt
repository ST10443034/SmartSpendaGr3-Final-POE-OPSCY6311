package com.example.smartspenda.data.database

import androidx.room.*
import com.example.smartspenda.data.entities.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name")
    suspend fun getCategoriesForUser(userId: Long): List<Category>

    @Delete
    suspend fun deleteCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)
}