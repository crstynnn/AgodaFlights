package com.example.agodaapp.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agodaapp.databinding.ActivityAdminAddFlightBinding
import com.example.agodaapp.models.Flight
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminAddFlightActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminAddFlightBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAddFlightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupPickers()

        binding.btnAddFlight.setOnClickListener {
            saveFlight()
        }
    }

    private fun setupPickers() {
        binding.etDeparture.setOnClickListener { showTimePicker { time -> binding.etDeparture.setText(time) } }
        binding.etArrival.setOnClickListener { showTimePicker { time -> binding.etArrival.setText(time) } }
        binding.etDate.setOnClickListener { showDatePicker() }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            onTimeSelected(String.format("%02d:%02d", hour, minute))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            binding.etDate.setText(String.format("%d-%02d-%02d", year, month + 1, day))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveFlight() {
        val airline = binding.etAirline.text.toString().trim()
        val flightNumber = binding.etFlightNumber.text.toString().trim()
        val from = binding.etFrom.text.toString().trim()
        val to = binding.etTo.text.toString().trim()
        val departure = binding.etDeparture.text.toString().trim()
        val arrival = binding.etArrival.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val seatsStr = binding.etSeats.text.toString().trim()

        if (airline.isEmpty() || flightNumber.isEmpty() || from.isEmpty() || to.isEmpty() ||
            departure.isEmpty() || arrival.isEmpty() || date.isEmpty() || priceStr.isEmpty() || seatsStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val seats = seatsStr.toIntOrNull() ?: 0

        val flightId = UUID.randomUUID().toString()
        val flight = Flight(
            id = flightId,
            airline = airline,
            flightNumber = flightNumber,
            from = from,
            to = to,
            departureTime = departure,
            arrivalTime = arrival,
            date = date,
            price = price,
            availableSeats = seats,
            status = "On Time"
        )

        binding.btnAddFlight.isEnabled = false
        firestore.collection("flights").document(flightId).set(flight)
            .addOnSuccessListener {
                Toast.makeText(this, "Flight added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnAddFlight.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}