package com.example.praktikum5_navigationcompose_245150200111040.data.repository

import com.example.praktikum5_navigationcompose_245150200111040.data.local.FavoriteStudentDao
import com.example.praktikum5_navigationcompose_245150200111040.data.local.FavoriteStudentEntity
import com.example.praktikum5_navigationcompose_245150200111040.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(
    private val dao: FavoriteStudentDao
) {
    fun observeIsFavorite(id: Int): Flow<Boolean> =
        dao.observeIsFavorite(id)

    fun observeFavorites(): Flow<List<FavoriteStudentEntity>> =
        dao.observeFavorites()

    fun observeFavoriteStudents(): Flow<List<Student>> =
        dao.observeFavorites().map { entities ->
            entities.map { entity ->
                Student(
                    id = entity.id,
                    name = "${entity.firstName} ${entity.lastName}".trim(),
                    email = entity.email,
                    university = ""
                )
            }
        }

    suspend fun add(student: Student) {
        dao.insert(
            FavoriteStudentEntity(
                id = student.id,
                firstName = student.name,
                lastName = "",
                email = student.email
            )
        )
    }

    suspend fun remove(id: Int) {
        dao.deleteById(id)
    }
}
