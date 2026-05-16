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
        "ILO - Iloilo Int'l Airport, Iloilo",
        "BAG - Baguio Airport, Baguio",
        "BCD - Bacolod-Silay Airport, Bacolod",
        "PPS - Puerto Princesa Airport, Palawan",
        "TAG - Bohol-Panglao Airport, Bohol",
        "LAO - Laoag Int'l Airport, Ilocos Norte"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupAirportSpinners()
        setupDatePicker()
        loadUserData()
        loadRecentBookings()

        binding.btnSearchFlights.setOnClickListener {
            searchFlights()
        }

        // Bottom Navigation Setup - FIXED
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    // Already on home, do nothing
                    true
                }
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

        // Optional: Set default values
        binding.actFrom.setText(philippineAirports[0], false)
        binding.actTo.setText(philippineAirports[1], false)
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val date = String.format("%d-%02d-%02d", year, month + 1, day)
                    binding.etDate.setText(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: "User"
                binding.tvUserName.text = name.split(" ")[0]
            }
            .addOnFailureListener {
                binding.tvUserName.text = "User"
            }
    }

    private fun loadRecentBookings() {
        val userId = auth.currentUser?.uid ?: return

        bookingAdapter = BookingAdapter(mutableListOf()) { booking ->
            // Optional: Handle booking click
            Toast.makeText(this, "Booking: ${booking.id}", Toast.LENGTH_SHORT).show()
        }

        binding.rvRecentBookings.layoutManager = LinearLayoutManager(this)
        binding.rvRecentBookings.adapter = bookingAdapter

        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("bookingDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                val bookings = result.toObjects(Booking::class.java)
                bookingAdapter.updateBookings(bookings)
                if (bookings.isEmpty()) {
                    binding.tvNoBookings.visibility = android.view.View.VISIBLE
                } else {
                    binding.tvNoBookings.visibility = android.view.View.GONE
                }
            }
            .addOnFailureListener {
                binding.tvNoBookings.visibility = android.view.View.VISIBLE
                binding.tvNoBookings.text = "Error loading bookings"
            }
    }

    private fun searchFlights() {
        val from = binding.actFrom.text.toString()
        val to = binding.actTo.text.toString()
        val date = binding.etDate.text.toString()
        val passengers = binding.etPassengers.text.toString()

        if (from.isEmpty() || to.isEmpty() || date.isEmpty() || passengers.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (from == to) {
            Toast.makeText(this, "Origin and destination cannot be the same", Toast.LENGTH_SHORT).show()
            return
        }

        val passengersInt = passengers.toIntOrNull()
        if (passengersInt == null || passengersInt < 1) {
            Toast.makeText(this, "Please enter valid number of passengers", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, FlightListActivity::class.java)
        intent.putExtra("from", from)
        intent.putExtra("to", to)
        intent.putExtra("date", date)
        intent.putExtra("passengers", passengersInt)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadRecentBookings() // Refresh bookings when returning to this screen
    }
}