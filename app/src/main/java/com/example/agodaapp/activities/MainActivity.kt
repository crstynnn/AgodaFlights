package com.example.agodaapp.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agodaapp.R
import com.example.agodaapp.adapters.FlightAdapter
import com.example.agodaapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var flightAdapter: FlightAdapter

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
        setupAvailableFlights()
        loadAvailableFlights()
        setupSearchExpansion()

        binding.btnSearchFlights.setOnClickListener {
            searchFlights()
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_home
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

    private fun setupSearchExpansion() {
        binding.btnExpandSearch.setOnClickListener {
            val isExpanding = binding.llSearchInputs.visibility == View.GONE
            
            TransitionManager.beginDelayedTransition(binding.cvSearch)
            
            if (isExpanding) {
                binding.llSearchInputs.visibility = View.VISIBLE
                binding.btnExpandSearch.text = "Close Search"
                binding.btnExpandSearch.setIconResource(R.drawable.ic_back) // Using ic_back as a close/up icon
            } else {
                binding.llSearchInputs.visibility = View.GONE
                binding.btnExpandSearch.text = "Search Flights"
                binding.btnExpandSearch.setIconResource(R.drawable.ic_search)
            }
        }
    }

    private fun setupAirportSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, philippineAirports)
        binding.actFrom.setAdapter(adapter)
        binding.actTo.setAdapter(adapter)
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
                binding.toolbar.subtitle = "Hello, ${name.split(" ")[0]}"
            }
    }

    private fun setupAvailableFlights() {
        flightAdapter = FlightAdapter(mutableListOf(), emptyMap(), { flight ->
            // ... (rest of intent setup)
            val intent = Intent(this, BookingDetailsActivity::class.java).apply {
                putExtra("flight_id", flight.id)
                putExtra("flight_airline", flight.airline)
                putExtra("flight_number", flight.flightNumber)
                putExtra("flight_price", flight.price)
                putExtra("flight_departure", flight.getFormattedDepartureTime())
                putExtra("flight_arrival", flight.getFormattedArrivalTime())
                putExtra("flight_duration", flight.duration)
                putExtra("flight_from", flight.from)
                putExtra("flight_to", flight.to)
                putExtra("flight_date", flight.getFormattedDate())
                putExtra("flight_gate", flight.gate)
                putExtra("flight_terminal", flight.terminal)
                putExtra("flight_baggage", flight.baggageAllowance)
                putExtra("flight_available_seats", flight.availableSeats)
                putExtra("passenger_count", 1) // Default for quick booking from home
            }
            startActivity(intent)
        })
        binding.rvAvailableFlights.layoutManager = LinearLayoutManager(this)
        binding.rvAvailableFlights.adapter = flightAdapter
    }

    private fun loadAvailableFlights() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Listen for bookings to get accurate counts
        firestore.collection("bookings").addSnapshotListener { bookingsSnapshot, _ ->
            val bookedSeatsPerFlight = mutableMapOf<String, Int>()
            bookingsSnapshot?.forEach { doc ->
                val fId = doc.getString("flightId") ?: ""
                if (fId.isNotEmpty()) {
                    val seatsList = doc.get("seatNumbers") as? List<*>
                    val count = seatsList?.size ?: doc.getLong("seats")?.toInt() ?: 0
                    bookedSeatsPerFlight[fId] = bookedSeatsPerFlight.getOrDefault(fId, 0) + count
                }
            }

            // Load flights
            firestore.collection("flights")
                .whereGreaterThanOrEqualTo("date", todayStr)
                .orderBy("date")
                .limit(20)
                .get()
                .addOnSuccessListener { result ->
                    val flights = result.documents.mapNotNull { doc ->
                        val f = doc.toObject(com.example.agodaapp.models.Flight::class.java)
                        f?.copy(id = if (f.id.isEmpty()) doc.id else f.id)
                    }
                    flightAdapter.updateData(flights, bookedSeatsPerFlight)
                    updateAirportSuggestions(flights)
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("MainActivity", "Error loading flights: ${e.message}")
                    loadAvailableFlightsBasic(bookedSeatsPerFlight)
                }
        }
    }

    private fun loadAvailableFlightsBasic(bookedSeatsPerFlight: Map<String, Int> = emptyMap()) {
        firestore.collection("flights")
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val flights = result.documents.mapNotNull { doc ->
                    val f = doc.toObject(com.example.agodaapp.models.Flight::class.java)
                    f?.copy(id = if (f.id.isEmpty()) doc.id else f.id)
                }
                val now = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val filteredFlights = flights.filter { 
                    val flightDate = it.getDepartureDateObject()
                    flightDate != null && (flightDate.after(now) || flightDate.equals(now))
                }
                flightAdapter.updateData(filteredFlights, bookedSeatsPerFlight)
                updateAirportSuggestions(filteredFlights)
            }
    }

    private fun updateAirportSuggestions(flights: List<com.example.agodaapp.models.Flight>) {
        val airports = mutableSetOf<String>()
        flights.forEach {
            airports.add(it.from)
            airports.add(it.to)
        }
        
        // Combine with default airports
        airports.addAll(philippineAirports)
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, airports.toList().sorted())
        binding.actFrom.setAdapter(adapter)
        binding.actTo.setAdapter(adapter)
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