package com.example.smartspenda.data.database

import androidx.room.*
import com.example.smartspenda.data.entities.User

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :password")
    suspend fun login(email: String, password: String): User?
}