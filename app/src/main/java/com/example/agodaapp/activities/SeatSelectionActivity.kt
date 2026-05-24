package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.agodaapp.adapters.Seat
import com.example.agodaapp.adapters.SeatSelectionAdapter
import com.example.agodaapp.adapters.SeatStatus
import com.example.agodaapp.databinding.ActivitySeatSelectionBinding
import com.google.firebase.firestore.FirebaseFirestore

class SeatSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatSelectionBinding
    private val selectedSeats = mutableListOf<String>()
    private lateinit var firestore: FirebaseFirestore
    private var flightId = ""
    private var availableSeats = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        flightId = intent.getStringExtra("flight_id") ?: ""
        availableSeats = intent.getIntExtra("available_seats", 50)
        val basePrice = intent.getDoubleExtra("flight_price", 0.0)
        
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadTakenSeats(basePrice)

        binding.btnConfirm.setOnClickListener {
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Please select at least one seat", Toast.LENGTH_SHORT).show()
            } else {
                val resultIntent = Intent().apply {
                    putStringArrayListExtra("selected_seats", ArrayList(selectedSeats))
                    putExtra("total_price", basePrice * selectedSeats.size)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun loadTakenSeats(basePrice: Double) {
        firestore.collection("bookings")
            .whereEqualTo("flightId", flightId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val takenSeats = mutableSetOf<String>()
                Log.d("SeatSync", "Found ${querySnapshot.size()} bookings for flight $flightId")
                for (doc in querySnapshot) {
                    // Try different fields just in case of mapping issues
                    val seats = doc.get("seatNumbers")
                    Log.d("SeatSync", "Booking ${doc.id} has seatNumbers: $seats")
                    
                    if (seats is List<*>) {
                        seats.forEach { takenSeats.add(it.toString()) }
                    }
                }
                Log.d("SeatSync", "All taken seats: $takenSeats")
                setupSeatGrid(basePrice, takenSeats)
            }
            .addOnFailureListener { e ->
                Log.e("SeatSync", "Error fetching bookings: ${e.message}")
                setupSeatGrid(basePrice, emptySet())
            }
    }

    private fun setupSeatGrid(basePrice: Double, takenSeats: Set<String>) {
        val seats = mutableListOf<Seat>()
        val columns = 4
        for (i in 1..(availableSeats / columns + 1)) {
            listOf("A", "B", "C", "D").forEach { col ->
                val seatId = "$i$col"
                if (seats.size < availableSeats) {
                    val status = if (takenSeats.contains(seatId)) SeatStatus.TAKEN else SeatStatus.AVAILABLE
                    seats.add(Seat(seatId, status))
                }
            }
        }

        binding.rvSeats.layoutManager = GridLayoutManager(this, columns)
        binding.rvSeats.adapter = SeatSelectionAdapter(seats) { seat ->
            if (seat.status == SeatStatus.TAKEN) return@SeatSelectionAdapter
            
            if (selectedSeats.contains(seat.id)) {
                selectedSeats.remove(seat.id)
            } else {
                selectedSeats.add(seat.id)
            }
            val totalPrice = basePrice * selectedSeats.size
            binding.tvSelectedSeat.text = "Selected: ${selectedSeats.joinToString(", ")} (Total: ₱$totalPrice)"
        }
    }
}
