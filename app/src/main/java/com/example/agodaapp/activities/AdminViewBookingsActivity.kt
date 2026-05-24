package com.example.agodaapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agodaapp.adapters.BookingAdapter
import com.example.agodaapp.databinding.ActivityAdminViewBookingsBinding
import com.example.agodaapp.models.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminViewBookingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminViewBookingsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminViewBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSearchView()
        loadAllBookings()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter(mutableListOf()) { /* No click action for admin view for now */ }
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = adapter
    }

    private fun loadAllBookings() {
        firestore.collection("bookings")
            .orderBy("bookingDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val bookings = result.toObjects(Booking::class.java)
                adapter.updateBookings(bookings)
            }
    }
}