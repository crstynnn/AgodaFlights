package com.example.agodaapp.models

data class Booking(
    val id: String = "",
    val userId: String = "",
    val flightId: String = "",
    val flight: Flight? = null,
    val passengerName: String = "",
    val passengerEmail: String = "",
    val passengerPhone: String = "",
    val seats: Int = 1,
    val totalPrice: Double = 0.0,
    val bookingDate: String = "",
    val status: String = "Confirmed",
    val bookingReference: String = ""
)