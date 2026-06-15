package com.example.smartspenda.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val joinDate: Long = System.currentTimeMillis()
)