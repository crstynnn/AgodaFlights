package com.example.agodaapp.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class Booking(
    val id: String = "",
    val userId: String = "",
    val flightId: String = "",
    val flight: Flight? = null,
    val passengerName: String = "",
    val passengerEmail: String = "",
    val passengerPhone: String = "",
    val seats: Int = 1,
    val seatNumbers: List<String> = emptyList(),
    val totalPrice: Double = 0.0,
    val bookingDate: Any? = "", // Handle String and Timestamp
    val status: String = "Confirmed",
    val paymentMethod: String = "",
    val transactionId: String = "",
    val bookingReference: String = "",
    val isDownloaded: Boolean = false,
    val isShared: Boolean = false,
    val paymentStatus: String = "Pending",
    val paymentDate: Any? = "" // Handle String and Timestamp
) {
    fun getFormattedBookingDate(): String = formatAnyDate(bookingDate)
    fun getFormattedPaymentDate(): String = formatAnyDate(paymentDate)

    private fun formatAnyDate(date: Any?): String {
        return when (date) {
            is String -> date
            is Timestamp -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                sdf.format(date.toDate())
            }
            else -> date?.toString() ?: ""
        }
    }
}