package com.example.agodaapp.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agodaapp.R
import com.example.agodaapp.adapters.BookingAdapter
import com.example.agodaapp.databinding.ActivityMainBinding
import com.example.agodaapp.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bookingAdapter: BookingAdapter

    private val philippineAirports = arrayOf(
        "MNL - Ninoy Aquino Int'l Airport, Manila",
        "CEB - Mactan-Cebu Int'l Airport, Cebu",
        "DVO - Francisco Bangoy Int'l Airport, Davao",
        "CRK - Clark Int'l Airport, Pampanga",
        "ILO - Iloilo Int'l Airport, Iloilo"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupAirportSpinners()
        setupDatePicker()
        loadUserData()
        setupRecentBookings()
        loadRecentBookings()

        binding.btnSearchFlights.setOnClickListener {
            searchFlights()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_bookings -> {
                    startActivity(Intent(this, MyBookingsActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAirportSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, philippineAirports)
        binding.actFrom.setAdapter(adapter)
        binding.actTo.setAdapter(adapter)
        binding.actFrom.setText(philippineAirports[0], false)
        binding.actTo.setText(philippineAirports[1], false)
    }

    private fun setupDatePicker() {
        binding.etDate.isFocusable = false
        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etDate.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDatePicker() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = String.format("%d-%02d-%02d", year, month + 1, day)
                binding.etDate.setText(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).also { dialog ->
            // Prevent selecting past dates
            dialog.datePicker.minDate = calendar.timeInMillis
        }.show()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "User"
                binding.tvUserName.text = name.split(" ")[0]
            }
    }

    private fun setupRecentBookings() {
        bookingAdapter = BookingAdapter(mutableListOf()) { booking ->
            // Navigate to booking confirmation details on click
            val intent = Intent(this, BookingConfirmationActivity::class.java).apply {
                putExtra("booking_ref", booking.bookingReference)
                putExtra("total_amount", booking.totalPrice)
                putExtra("payment_method", booking.paymentMethod)
                putExtra("flight_airline", booking.flight?.airline ?: "")
                putExtra("flight_number", booking.flight?.flightNumber ?: "")
                putExtra("flight_from", booking.flight?.from ?: "")
                putExtra("flight_to", booking.flight?.to ?: "")
            }
            startActivity(intent)
        }
        binding.rvRecentBookings.layoutManager = LinearLayoutManager(this)
        binding.rvRecentBookings.adapter = bookingAdapter
    }

    private fun loadRecentBookings() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("bookingDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                val bookings = result.toObjects(Booking::class.java)
                bookingAdapter.updateBookings(bookings)
            }
            .addOnFailureListener {
                // Silently fail — recent bookings are non-critical
            }
    }

    private fun searchFlights() {
        val from = binding.actFrom.text.toString().trim()
        val to = binding.actTo.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val passengersStr = binding.etPassengers.text.toString().trim()

        if (from.isEmpty() || to.isEmpty() || date.isEmpty() || passengersStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (from == to) {
            Toast.makeText(this, "Origin and destination cannot be the same", Toast.LENGTH_SHORT).show()
            return
        }

        val passengers = passengersStr.toIntOrNull()
        if (passengers == null || passengers < 1) {
            Toast.makeText(this, "Please enter a valid number of passengers", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, FlightListActivity::class.java).apply {
            putExtra("from", from)
            putExtra("to", to)
            putExtra("date", date)
            putExtra("passengers", passengers)
        }
        startActivity(intent)
    }
}