package com.example.praktikum5_navigationcompose_245150200111040.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStudentDao {

    @Query("SELECT * FROM favorite_students ORDER BY firstName ASC")
    fun observeFavorites(): Flow<List<FavoriteStudentEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_students WHERE id = :id)")
    fun observeIsFavorite(id: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: FavoriteStudentEntity)

    @Query("DELETE FROM favorite_students WHERE id = :id")
    suspend fun deleteById(id: Int)
}