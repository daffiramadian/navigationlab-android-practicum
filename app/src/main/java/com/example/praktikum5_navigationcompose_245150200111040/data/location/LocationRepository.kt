package com.example.praktikum5_navigationcompose_245150200111040.data.location

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<DeviceLocation>
}
