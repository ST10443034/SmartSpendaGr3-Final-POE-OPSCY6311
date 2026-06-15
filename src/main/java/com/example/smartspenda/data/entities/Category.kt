package com.example.smartspenda.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long = 0,
    val userId: Long,
    val name: String,
    val iconRes: Int = 0,
    val color: String = "#FF6200EE"
)