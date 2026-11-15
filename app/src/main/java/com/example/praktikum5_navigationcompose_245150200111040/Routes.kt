package com.example.praktikum5_navigationcompose_245150200111040

object Routes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val ABOUT = "about"
    const val DETAIL = "detail/{studentId}"
    const val FAVORITES = "favorites"

    fun detail(studentId: Int): String = "detail/$studentId"
}
