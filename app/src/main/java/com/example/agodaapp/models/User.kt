package com.example.agodaapp.models

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val createdAt: Long = 0L,
    val userType: String = "customer",
    val isActive: Boolean = true,
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0,
    val profileImage: String = ""
)