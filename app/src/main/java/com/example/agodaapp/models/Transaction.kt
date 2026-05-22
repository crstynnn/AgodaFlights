package com.example.agodaapp.models

data class Transaction(
    val id: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val currency: String = "PHP",
    val paymentMethod: String = "",
    val paymentStatus: String = "Pending",
    val transactionDate: String = "",
    val referenceNumber: String = "",
    val receiptUrl: String = "",
    val description: String = ""
)