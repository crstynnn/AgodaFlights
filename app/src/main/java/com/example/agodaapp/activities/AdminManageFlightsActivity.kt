package com.example.agodaapp.activities

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agodaapp.adapters.FlightAdapter
import com.example.agodaapp.databinding.ActivityAdminManageFlightsBinding
import com.example.agodaapp.models.Flight
import com.google.firebase.firestore.FirebaseFirestore

class AdminManageFlightsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminManageFlightsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: FlightAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminManageFlightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSearchView()
        loadAllFlights()
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
        adapter = FlightAdapter(mutableListOf(), emptyMap(), { flight ->
            showCancelConfirmation(flight)
        }, isAdminMode = true)
        binding.rvFlights.layoutManager = LinearLayoutManager(this)
        binding.rvFlights.adapter = adapter
    }

    fun showDelayConfirmation(flight: Flight) {
        val calendar = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(this, { _, year, month, dayOfMonth ->
            android.app.TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                updateFlightDelay(flight, calendar.time)
            }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateFlightDelay(flight: Flight, newTime: java.util.Date) {
        val updateData = mapOf(
            "status" to "Delayed",
            "departureTime" to java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(newTime),
            "date" to java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(newTime)
        )
        firestore.collection("flights").document(flight.id)
            .update(updateData)
            .addOnSuccessListener {
                syncBookingFlightStatus(flight.id, "Delayed")
                Toast.makeText(this, "Flight status updated to Delayed", Toast.LENGTH_SHORT).show()
                loadAllFlights()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun syncBookingFlightStatus(flightId: String, newStatus: String) {
        android.util.Log.d("AdminManageFlights", "Attempting to sync status for flightId: $flightId to newStatus: $newStatus")
        firestore.collection("bookings")
            .whereEqualTo("flightId", flightId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                android.util.Log.d("AdminManageFlights", "Found ${querySnapshot.size()} bookings to update.")
                val batch = firestore.batch()
                for (doc in querySnapshot) {
                    batch.update(doc.reference, "flight.status", newStatus)
                }
                batch.commit()
                    .addOnSuccessListener { android.util.Log.d("AdminManageFlights", "Sync batch commit successful.") }
                    .addOnFailureListener { e -> android.util.Log.e("AdminManageFlights", "Sync batch commit failed: ${e.message}") }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AdminManageFlights", "Failed to query bookings for sync: ${e.message}")
            }
    }

    private fun loadAllFlights() {
        firestore.collection("bookings").get().addOnSuccessListener { bookingsSnapshot ->
            val bookedSeatsPerFlight = mutableMapOf<String, Int>()
            bookingsSnapshot?.forEach { doc ->
                val fId = doc.getString("flightId") ?: ""
                if (fId.isNotEmpty()) {
                    val seatsList = doc.get("seatNumbers") as? List<*>
                    val count = seatsList?.size ?: doc.getLong("seats")?.toInt() ?: 0
                    bookedSeatsPerFlight[fId] = bookedSeatsPerFlight.getOrDefault(fId, 0) + count
                }
            }

            firestore.collection("flights")
                .get()
                .addOnSuccessListener { result ->
                    val flights = result.toObjects(Flight::class.java)
                    adapter.updateData(flights, bookedSeatsPerFlight)
                }
        }
    }

    private fun showCancelConfirmation(flight: Flight) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Flight")
            .setMessage("Are you sure you want to cancel flight ${flight.flightNumber}?")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelFlight(flight)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelFlight(flight: Flight) {
        firestore.collection("flights").document(flight.id)
            .update("status", "Cancelled")
            .addOnSuccessListener {
                syncBookingFlightStatus(flight.id, "Cancelled")
                Toast.makeText(this, "Flight status updated to Cancelled", Toast.LENGTH_SHORT).show()
                loadAllFlights() // Refresh list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}