package com.example.agodaapp

import com.example.agodaapp.models.Booking
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingStatusTest {
    @Test
    fun testBookingStatusDefault() {
        val booking = Booking(id = "1")
        assertEquals("Confirmed", booking.status)
    }

    @Test
    fun testBookingStatusCustom() {
        val booking = Booking(id = "1", status = "Delayed")
        assertEquals("Delayed", booking.status)
    }
}
