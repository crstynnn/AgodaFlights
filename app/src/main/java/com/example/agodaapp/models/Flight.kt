package com.example.agodaapp.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class Flight(
    val id: String = "",
    val airline: String = "",
    val flightNumber: String = "",
    val from: String = "",
    val to: String = "",
    val fromCode: String = "",
    val toCode: String = "",
    val departureTime: Any? = "", // Handle String and Timestamp
    val arrivalTime: Any? = "",   // Handle String and Timestamp
    val duration: String = "",
    val price: Double = 0.0,
    val availableSeats: Int = 0,
    val date: Any? = "",          // Handle String and Timestamp
    val gate: String = "",
    val terminal: String = "",
    val baggageAllowance: String = "",
    val status: String = "On Time"
) {
    fun getFormattedDate(): String = formatAnyDate(date, "yyyy-MM-dd")
    fun getFormattedDepartureTime(): String = formatAnyDate(departureTime, "HH:mm")
    fun getFormattedArrivalTime(): String = formatAnyDate(arrivalTime, "HH:mm")

    fun getDepartureDateObject(): Date? {
        return when (val d = date) {
            is Timestamp -> d.toDate()
            is String -> {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(d)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    private fun formatAnyDate(dateVal: Any?, pattern: String): String {
        return when (dateVal) {
            is String -> dateVal
            is Timestamp -> {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.format(dateVal.toDate())
            }
            else -> dateVal?.toString() ?: ""
        }
    }
}