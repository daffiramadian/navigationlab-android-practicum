package com.example.praktikum5_navigationcompose_245150200111040.ui.favorites

import com.example.praktikum5_navigationcompose_245150200111040.data.model.Student

data class FavoritesUiState(
    val favorites: List<Student> = emptyList(),
    val sortAscending: Boolean = true
)
