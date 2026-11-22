package com.example.praktikum5_navigationcompose_245150200111040.ui.location

import com.example.praktikum5_navigationcompose_245150200111040.data.location.DeviceLocation

sealed interface LocationUiState {
    data object Idle : LocationUiState
    data object Loading : LocationUiState
    data class Success(val location: DeviceLocation) : LocationUiState
    data class PermissionDenied(val permanentlyDenied: Boolean = false) : LocationUiState
    data class Error(val message: String) : LocationUiState
}
