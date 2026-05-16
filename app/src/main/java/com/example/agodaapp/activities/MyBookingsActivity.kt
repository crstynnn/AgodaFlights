package com.example.agodaapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(mutableListOf()) { booking ->
            // View booking details
        }

        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = bookingAdapter
    }

    private fun loadBookings() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("bookingDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val bookings = result.toObjects(Booking::class.java)
                bookingAdapter.updateBookings(bookings)
            }
    }
}