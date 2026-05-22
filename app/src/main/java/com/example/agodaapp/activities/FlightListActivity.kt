package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agodaapp.adapters.FlightAdapter
import com.example.agodaapp.databinding.ActivityFlightListBinding
import com.example.agodaapp.models.Flight
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class FlightListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlightListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var flightAdapter: FlightAdapter
    private var from = ""
    private var to = ""
    private var date = ""
    private var passengers = 1
    private var fromCode = ""
    private var toCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlightListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        from = intent.getStringExtra("from") ?: ""
        to = intent.getStringExtra("to") ?: ""
        date = intent.getStringExtra("date") ?: ""
        passengers = intent.getIntExtra("passengers", 1)
        fromCode = intent.getStringExtra("from_code") ?: from.substring(0, 3)
        toCode = intent.getStringExtra("to_code") ?: to.substring(0, 3)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.tvSearchInfo.text = "$from → $to | $date | $passengers passenger(s)"

        setupRecyclerView()
        loadFlightsFromFirestore()
    }

    private fun setupRecyclerView() {
        flightAdapter = FlightAdapter(mutableListOf()) { flight ->
            val intent = Intent(this, BookingDetailsActivity::class.java).apply {
                putExtra("flight_id", flight.id)
                putExtra("flight_airline", flight.airline)
                putExtra("flight_number", flight.flightNumber)
                putExtra("flight_price", flight.price)
                putExtra("flight_departure", flight.departureTime)
                putExtra("flight_arrival", flight.arrivalTime)
                putExtra("flight_duration", flight.duration)
                putExtra("flight_from", flight.from)
                putExtra("flight_to", flight.to)
                putExtra("flight_date", flight.date)
                putExtra("flight_gate", flight.gate)
                putExtra("flight_terminal", flight.terminal)
                putExtra("flight_baggage", flight.baggageAllowance)
                putExtra("passenger_count", passengers)
                putExtra("from_airport", from)
                putExtra("to_airport", to)
                putExtra("from_code", fromCode)
                putExtra("to_code", toCode)
            }
            startActivity(intent)
        }

        binding.rvFlights.layoutManager = LinearLayoutManager(this)
        binding.rvFlights.adapter = flightAdapter
    }

    private fun loadFlightsFromFirestore() {
        // Query flights from Firestore based on origin and destination
        firestore.collection("flights")
            .whereEqualTo("fromCode", fromCode)
            .whereEqualTo("toCode", toCode)
            .get()
            .addOnSuccessListener { result ->
                val flights = mutableListOf<Flight>()
                for (document in result) {
                    val flight = document.toObject<Flight>()
                    flights.add(flight)
                }

                if (flights.isEmpty()) {
                    Toast.makeText(this, "No flights found for this route", Toast.LENGTH_SHORT).show()
                    binding.tvFlightCount.text = "0 flights found"
                } else {
                    binding.tvFlightCount.text = "${flights.size} flights found"
                    flightAdapter.updateFlights(flights)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading flights: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FlightList", "Error: ${e.message}")
            }
    }
}