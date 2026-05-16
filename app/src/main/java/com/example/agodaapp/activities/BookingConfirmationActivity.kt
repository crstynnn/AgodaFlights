package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityBookingConfirmationBinding

class bookingconfirmationactivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val bookingRef = intent.getStringExtra("booking_ref") ?: "N/A"
        val totalAmount = intent.getDoubleExtra("total_amount", 0.0)
        val paymentMethod = intent.getStringExtra("payment_method") ?: "Credit Card"
        val flightAirline = intent.getStringExtra("flight_airline") ?: ""
        val flightNumber = intent.getStringExtra("flight_number") ?: ""
        val flightFrom = intent.getStringExtra("flight_from") ?: ""
        val flightTo = intent.getStringExtra("flight_to") ?: ""
        val flightDate = intent.getStringExtra("flight_date") ?: ""
        val departureTime = intent.getStringExtra("departure_time") ?: ""
        val arrivalTime = intent.getStringExtra("arrival_time") ?: ""
        val passengerName = intent.getStringExtra("passenger_name") ?: ""
        val passengerCount = intent.getIntExtra("passenger_count", 1)
        val gate = intent.getStringExtra("gate") ?: "A12"
        val terminal = intent.getStringExtra("terminal") ?: "T1"

        // Set values to views
        binding.tvBookingReference.text = bookingRef
        binding.tvFlightRoute.text = "$flightFrom → $flightTo"
        binding.tvFlightName.text = "$flightAirline • $flightNumber"
        binding.tvDepartureTime.text = departureTime
        binding.tvArrivalTime.text = arrivalTime
        binding.tvFlightDate.text = flightDate
        binding.tvGate.text = gate
        binding.tvTerminal.text = terminal
        binding.tvPassengerName.text = passengerName
        binding.tvPaymentMethod.text = paymentMethod
        binding.tvTotalAmount.text = String.format("₱%,.2f", totalAmount)
        binding.tvTicketStatus.text = "CONFIRMED"

        // Button Click Listeners
        binding.btnViewBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
            finish()
        }

        binding.btnBackToHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnDownloadTicket.setOnClickListener {
            Toast.makeText(this, "Ticket saved to Downloads", Toast.LENGTH_SHORT).show()
        }

        binding.btnShareTicket.setOnClickListener {
            shareTicket(bookingRef, flightAirline, flightNumber, flightFrom, flightTo, passengerName, totalAmount)
        }
    }

    private fun shareTicket(
        bookingRef: String,
        airline: String,
        flightNo: String,
        from: String,
        to: String,
        passenger: String,
        amount: Double
    ) {
        val shareText = """
            🎫 AGODA FLIGHTS TICKET
            ───────────────────
            Booking Reference: $bookingRef
            Flight: $airline $flightNo
            Route: $from → $to
            Passenger: $passenger
            Total: ₱${String.format("%,.2f", amount)}
            ───────────────────
            Thank you for booking with Agoda Flights!
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Ticket"))
    }
}