package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupClickListeners()
        loadStats()
    }

    private fun setupClickListeners() {
        binding.cardAddFlight.setOnClickListener {
            startActivity(Intent(this, AdminAddFlightActivity::class.java))
        }

        binding.cardViewBookings.setOnClickListener {
            startActivity(Intent(this, AdminViewBookingsActivity::class.java))
        }

        binding.cardManageFlights.setOnClickListener {
            startActivity(Intent(this, AdminManageFlightsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadStats() {
        // Load total flights
        firestore.collection("flights").get()
            .addOnSuccessListener { result ->
                binding.tvTotalFlights.text = result.size().toString()
            }

        // Load total bookings
        firestore.collection("bookings").get()
            .addOnSuccessListener { result ->
                binding.tvTotalBookings.text = result.size().toString()
            }
    }
}