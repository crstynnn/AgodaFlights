package com.example.agodaapp.models

data class PaymentMethod(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val cardNumber: String = "",
    val cardHolderName: String = "",
    val expiryDate: String = "",
    val isDefault: Boolean = false
)