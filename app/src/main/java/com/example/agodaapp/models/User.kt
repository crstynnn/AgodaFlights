package com.example.agodaapp.models

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val createdAt: String = "",
    val userType: String = "customer",
    val isActive: Boolean = true,
    val totalBookings: Int = 0
)