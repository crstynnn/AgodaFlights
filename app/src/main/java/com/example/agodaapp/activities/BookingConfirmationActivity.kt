package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityBookingConfirmationBinding

class BookingConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val passengerName = intent.getStringExtra("passenger_name") ?: "Traveler"
        val gate = intent.getStringExtra("gate") ?: "A12"
        val terminal = intent.getStringExtra("terminal") ?: "T1"

        binding.tvBookingReference.text = bookingRef
        binding.tvFlightRoute.text = "${flightFrom.split(" ").firstOrNull() ?: ""} → ${flightTo.split(" ").firstOrNull() ?: ""}"
        binding.tvFlightName.text = "$flightAirline • $flightNumber"
        binding.tvTotalAmount.text = String.format("₱%,.2f", totalAmount)
        binding.tvPaymentMethod.text = paymentMethod
        binding.tvTicketStatus.text = "CONFIRMED"
        
        binding.tvDepartureTime.text = departureTime
        binding.tvArrivalTime.text = arrivalTime
        binding.tvFlightDate.text = flightDate
        binding.tvPassengerName.text = passengerName
        binding.tvGate.text = gate
        binding.tvTerminal.text = terminal

        binding.btnViewBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
            finish()
        }

        binding.btnBackToHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnDownloadTicket.setOnClickListener {
            Toast.makeText(this, "Ticket saved to Downloads", Toast.LENGTH_SHORT).show()
        }

        binding.btnShareTicket.setOnClickListener {
            val shareText = buildString {
                append("🎫 AGODA FLIGHTS BOOKING CONFIRMATION\n")
                append("Booking Ref: $bookingRef\n")
                append("Flight: $flightAirline $flightNumber\n")
                append("Route: ${flightFrom.split(" ")[0]} → ${flightTo.split(" ")[0]}\n")
                append("Date: $flightDate\n")
                append("Passenger: $passengerName\n")
                append("Total: ${String.format("₱%,.2f", totalAmount)}\n")
                append("Status: CONFIRMED ✅")
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share Ticket"))
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}