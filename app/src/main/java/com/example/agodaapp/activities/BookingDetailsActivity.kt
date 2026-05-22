package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityBookingDetailsBinding
import com.example.agodaapp.models.Booking
import com.example.agodaapp.models.Flight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BookingDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookingDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Variables to receive data from FlightListActivity
    private var flightId = ""
    private var flightAirline = ""
    private var flightNumber = ""
    private var flightPrice = 0.0
    private var flightDeparture = ""
    private var flightArrival = ""
    private var flightDuration = ""
    private var flightFrom = ""
    private var flightTo = ""
    private var flightDate = ""
    private var flightGate = ""
    private var flightTerminal = ""
    private var flightBaggage = ""
    private var passengerCount = 1
    private var fromAirport = ""
    private var toAirport = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // RECEIVE DATA FROM FLIGHTLISTACTIVITY
        flightId = intent.getStringExtra("flight_id") ?: ""
        flightAirline = intent.getStringExtra("flight_airline") ?: ""
        flightNumber = intent.getStringExtra("flight_number") ?: ""
        flightPrice = intent.getDoubleExtra("flight_price", 0.0)
        flightDeparture = intent.getStringExtra("flight_departure") ?: ""
        flightArrival = intent.getStringExtra("flight_arrival") ?: ""
        flightDuration = intent.getStringExtra("flight_duration") ?: ""
        flightFrom = intent.getStringExtra("flight_from") ?: ""
        flightTo = intent.getStringExtra("flight_to") ?: ""
        flightDate = intent.getStringExtra("flight_date") ?: ""
        flightGate = intent.getStringExtra("flight_gate") ?: "A12"
        flightTerminal = intent.getStringExtra("flight_terminal") ?: "T1"
        flightBaggage = intent.getStringExtra("flight_baggage") ?: "20kg"
        passengerCount = intent.getIntExtra("passenger_count", 1)
        fromAirport = intent.getStringExtra("from_airport") ?: ""
        toAirport = intent.getStringExtra("to_airport") ?: ""

        // Setup toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Display flight details
        displayFlightDetails()

        // Load user data
        loadUserData()

        // Confirm button click
        binding.btnConfirmBooking.setOnClickListener {
            confirmBooking()
        }
    }

    private fun displayFlightDetails() {
        // Airline info
        binding.tvAirline.text = "$flightAirline - $flightNumber"
        binding.tvFlightNumber.text = flightNumber

        // Flight times
        binding.tvDepartureTime.text = flightDeparture
        binding.tvArrivalTime.text = flightArrival
        binding.tvDuration.text = flightDuration

        // Route
        binding.tvFrom.text = flightFrom
        binding.tvTo.text = flightTo

        // Date
        binding.tvFlightDate.text = "Date: $flightDate"

        // Gate and baggage info (using the correct TextView IDs from your layout)
        try {
            binding.tvGateInfo.text = "Gate: $flightGate | Terminal: $flightTerminal"
            binding.tvBaggageInfo.text = "Baggage: $flightBaggage"
        } catch (e: Exception) {
            // If these TextViews don't exist, just skip them
        }

        // Price calculation
        val taxes = 250.0
        val totalPrice = (flightPrice * passengerCount) + taxes

        // Update price summary
        binding.tvBaseFare.text = String.format("₱%.2f", flightPrice)
        binding.tvPassengerCount.text = passengerCount.toString()
        binding.tvTaxes.text = String.format("₱%.2f", taxes)
        binding.tvTotalPrice.text = String.format("₱%.2f", totalPrice)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                binding.etPassengerName.setText(document.getString("name") ?: "")
                binding.etPassengerEmail.setText(document.getString("email") ?: "")
                binding.etPassengerPhone.setText(document.getString("phone") ?: "")
            }
    }

    private fun confirmBooking() {
        val passengerName = binding.etPassengerName.text.toString().trim()
        val passengerEmail = binding.etPassengerEmail.text.toString().trim()
        val passengerPhone = binding.etPassengerPhone.text.toString().trim()

        if (passengerName.isEmpty() || passengerEmail.isEmpty()) {
            Toast.makeText(this, "Please fill all passenger details", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val taxes = 250.0
        val totalPrice = (flightPrice * passengerCount) + taxes
        val bookingDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val bookingRef = generateBookingReference()

        // Create Flight object
        val flight = Flight(
            id = flightId,
            airline = flightAirline,
            flightNumber = flightNumber,
            from = flightFrom,
            to = flightTo,
            departureTime = flightDeparture,
            arrivalTime = flightArrival,
            duration = flightDuration,
            price = flightPrice,
            availableSeats = 50,
            date = flightDate,
            gate = flightGate,
            terminal = flightTerminal,
            baggageAllowance = flightBaggage,
            fromCode = "",
            toCode = ""
        )

        val booking = Booking(
            id = UUID.randomUUID().toString(),
            userId = userId,
            flightId = flightId,
            flight = flight,
            passengerName = passengerName,
            passengerEmail = passengerEmail,
            passengerPhone = passengerPhone,
            seats = passengerCount,
            totalPrice = totalPrice,
            bookingDate = bookingDate,
            status = "Confirmed",
            paymentMethod = "Credit Card",
            transactionId = "",
            bookingReference = bookingRef,
            isDownloaded = false,
            isShared = false
        )

        binding.btnConfirmBooking.isEnabled = false
        binding.btnConfirmBooking.text = "Processing..."

        firestore.collection("bookings").document(booking.id)
            .set(booking)
            .addOnSuccessListener {
                // Navigate to Payment or Confirmation
                val intent = Intent(this, BookingConfirmationActivity::class.java).apply {
                    putExtra("booking_id", booking.id)
                    putExtra("booking_ref", bookingRef)
                    putExtra("total_amount", totalPrice)
                    putExtra("payment_method", "Credit Card")
                    putExtra("flight_airline", flightAirline)
                    putExtra("flight_number", flightNumber)
                    putExtra("flight_from", fromAirport)
                    putExtra("flight_to", toAirport)
                    putExtra("flight_date", flightDate)
                    putExtra("departure_time", flightDeparture)
                    putExtra("arrival_time", flightArrival)
                    putExtra("passenger_name", passengerName)
                    putExtra("passenger_count", passengerCount)
                    putExtra("gate", flightGate)
                    putExtra("terminal", flightTerminal)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnConfirmBooking.isEnabled = true
                binding.btnConfirmBooking.text = "Confirm Booking"
                Toast.makeText(this, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateBookingReference(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}