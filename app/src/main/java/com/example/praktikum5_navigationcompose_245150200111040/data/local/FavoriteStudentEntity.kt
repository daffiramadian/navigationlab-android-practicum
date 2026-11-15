package com.example.praktikum5_navigationcompose_245150200111040.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_students")
data class FavoriteStudentEntity(
    @PrimaryKey val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String
)
