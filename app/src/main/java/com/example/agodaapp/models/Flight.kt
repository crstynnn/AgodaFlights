package com.example.agodaapp.models

data class Flight(
    val id: String = "",
    val airline: String = "",
    val flightNumber: String = "",
    val from: String = "",
    val to: String = "",
    val fromCode: String = "",
    val toCode: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val duration: String = "",
    val price: Double = 0.0,
    val availableSeats: Int = 0,
    val date: String = "",
    val gate: String = "",
    val terminal: String = "",
    val baggageAllowance: String = "",
    val status: String = "On Time"
)