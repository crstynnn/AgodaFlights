package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityProfileBinding
import com.example.agodaapp.models.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        loadUserProfile()
        loadStatistics()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                binding.tvName.text = document.getString("name") ?: "User"
                binding.tvEmail.text = document.getString("email") ?: auth.currentUser?.email ?: ""
            }
            .addOnFailureListener {
                binding.tvEmail.text = auth.currentUser?.email ?: ""
            }
    }

    private fun loadStatistics() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("bookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val bookings = result.toObjects(Booking::class.java)
                binding.tvTotalBookings.text = bookings.size.toString()
                val totalSpent = bookings.sumOf { it.totalPrice }
                binding.tvTotalSpent.text = String.format("₱%,.2f", totalSpent)
            }
            .addOnFailureListener {
                binding.tvTotalBookings.text = "0"
                binding.tvTotalSpent.text = "₱0.00"
            }
    }
}