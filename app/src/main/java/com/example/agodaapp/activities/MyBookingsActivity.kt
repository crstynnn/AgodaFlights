package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.example.agodaapp.adapters.BookingAdapter
import com.example.agodaapp.databinding.ActivityMyBookingsBinding
import com.example.agodaapp.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyBookingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("MyBookings", "onCreate() started")
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        android.util.Log.d("MyBookings", "onCreate() content set")

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSearchView()
        loadBookings()
        android.util.Log.d("MyBookings", "onCreate() finished")
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                bookingAdapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(mutableListOf()) { booking ->
            // Open booking confirmation details when tapped
            val intent = Intent(this, BookingConfirmationActivity::class.java).apply {
                putExtra("booking_ref", booking.bookingReference)
                putExtra("total_amount", booking.totalPrice)
                putExtra("payment_method", booking.paymentMethod.ifEmpty { "N/A" })
                putExtra("flight_airline", booking.flight?.airline ?: "")
                putExtra("flight_number", booking.flight?.flightNumber ?: "")
                putExtra("flight_from", booking.flight?.from ?: "")
                putExtra("flight_to", booking.flight?.to ?: "")
                putExtra("flight_date", booking.flight?.getFormattedDate() ?: "")
                putExtra("departure_time", booking.flight?.getFormattedDepartureTime() ?: "")
                putExtra("arrival_time", booking.flight?.getFormattedArrivalTime() ?: "")
                putExtra("passenger_name", booking.passengerName)
                putExtra("gate", booking.flight?.gate ?: "A12")
                putExtra("terminal", booking.flight?.terminal ?: "T1")
            }
            startActivity(intent)
        }
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = bookingAdapter
    }

    private fun loadBookings() {
        android.util.Log.d("MyBookings", "loadBookings() called")
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("MyBookings", "Fetching fresh from Firestore...")
        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener { result ->
                android.util.Log.d("MyBookings", "Firestore query success")
                val bookings = mutableListOf<Booking>()
                for (document in result) {
                    try {
                        val booking = document.toObject(Booking::class.java).copy(id = document.id)
                        bookings.add(booking)
                        android.util.Log.d("MyBookings", "Parsed Booking: ${booking.id}, Status: ${booking.status}, FlightStatus: ${booking.flight?.status}")
                    } catch (e: Exception) {
                        android.util.Log.e("MyBookings", "Failed to parse doc ${document.id}: ${e.message}")
                    }
                }
                
                android.util.Log.d("MyBookings", "Total parsed matching UID: ${bookings.size}")
                bookingAdapter.updateBookings(bookings)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("MyBookings", "Error loading bookings: ${e.message}")
                Toast.makeText(this, "Error loading bookings. Please check your connection.", Toast.LENGTH_SHORT).show()
            }
    }
}