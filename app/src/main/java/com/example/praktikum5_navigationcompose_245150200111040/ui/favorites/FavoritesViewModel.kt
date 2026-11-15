package com.example.praktikum5_navigationcompose_245150200111040.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.praktikum5_navigationcompose_245150200111040.data.preferences.UserPreferencesRepository
import com.example.praktikum5_navigationcompose_245150200111040.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                favoriteRepository.observeFavoriteStudents(),
                preferencesRepository.sortAscending
            ) { students, ascending ->
                val sorted = if (ascending) {
                    students.sortedBy { it.name.lowercase() }
                } else {
                    students.sortedByDescending { it.name.lowercase() }
                }
                FavoritesUiState(favorites = sorted, sortAscending = ascending)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleSortAscending() {
        viewModelScope.launch {
            preferencesRepository.saveSortAscending(!_uiState.value.sortAscending)
        }
    }

    companion object {
        fun factory(
            favoriteRepository: FavoriteRepository,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FavoritesViewModel(favoriteRepository, preferencesRepository) as T
                }
            }
    }
}
