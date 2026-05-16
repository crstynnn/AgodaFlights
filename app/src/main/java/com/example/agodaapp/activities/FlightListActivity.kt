package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agodaapp.adapters.FlightAdapter
import com.example.agodaapp.databinding.ActivityFlightListBinding
import com.example.agodaapp.models.Flight
import com.google.firebase.firestore.FirebaseFirestore

class FlightListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlightListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var flightAdapter: FlightAdapter
    private var from = ""
    private var to = ""
    private var date = ""
    private var passengers = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlightListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        from = intent.getStringExtra("from") ?: ""
        to = intent.getStringExtra("to") ?: ""
        date = intent.getStringExtra("date") ?: ""
        passengers = intent.getIntExtra("passengers", 1)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.tvSearchInfo.text = "$from → $to | $date | $passengers passenger(s)"

        setupRecyclerView()
        loadFlights()
    }

    private fun setupRecyclerView() {
        flightAdapter = FlightAdapter(mutableListOf()) { flight ->
            val intent = Intent(this, BookingDetailsActivity::class.java)
            intent.putExtra("flight_id", flight.id)
            intent.putExtra("flight_airline", flight.airline)
            intent.putExtra("flight_number", flight.flightNumber)
            intent.putExtra("flight_price", flight.price)
            intent.putExtra("flight_departure", flight.departureTime)
            intent.putExtra("flight_arrival", flight.arrivalTime)
            intent.putExtra("flight_duration", flight.duration)
            intent.putExtra("flight_from", flight.from)
            intent.putExtra("flight_to", flight.to)
            intent.putExtra("flight_date", flight.date)
            intent.putExtra("flight_gate", flight.gate)
            intent.putExtra("flight_terminal", flight.terminal)
            intent.putExtra("flight_baggage", flight.baggageAllowance)
            intent.putExtra("passenger_count", passengers)
            intent.putExtra("from_airport", from)
            intent.putExtra("to_airport", to)
            startActivity(intent)
        }

        binding.rvFlights.layoutManager = LinearLayoutManager(this)
        binding.rvFlights.adapter = flightAdapter
    }

    private fun loadFlights() {
        val sampleFlights = listOf(
            Flight(
                id = "1",
                airline = "Philippine Airlines",
                flightNumber = "PR 123",
                from = from,
                to = to,
                departureTime = "06:00 AM",
                arrivalTime = "07:30 AM",
                duration = "1h 30m",
                price = 2500.00,
                availableSeats = 50,
                date = date,
                gate = "A12",
                terminal = "T1",
                baggageAllowance = "20kg"
            ),
            Flight(
                id = "2",
                airline = "Cebu Pacific",
                flightNumber = "5J 456",
                from = from,
                to = to,
                departureTime = "10:00 AM",
                arrivalTime = "11:35 AM",
                duration = "1h 35m",
                price = 1899.00,
                availableSeats = 30,
                date = date,
                gate = "B05",
                terminal = "T2",
                baggageAllowance = "15kg"
            ),
            Flight(
                id = "3",
                airline = "AirAsia Philippines",
                flightNumber = "Z2 789",
                from = from,
                to = to,
                departureTime = "02:00 PM",
                arrivalTime = "03:25 PM",
                duration = "1h 25m",
                price = 2100.00,
                availableSeats = 45,
                date = date,
                gate = "C08",
                terminal = "T3",
                baggageAllowance = "20kg"
            ),
            Flight(
                id = "4",
                airline = "Philippine Airlines",
                flightNumber = "PR 321",
                from = from,
                to = to,
                departureTime = "05:00 PM",
                arrivalTime = "06:30 PM",
                duration = "1h 30m",
                price = 3200.00,
                availableSeats = 20,
                date = date,
                gate = "D15",
                terminal = "T1",
                baggageAllowance = "25kg"
            ),
            Flight(
                id = "5",
                airline = "Cebu Pacific",
                flightNumber = "5J 654",
                from = from,
                to = to,
                departureTime = "08:00 PM",
                arrivalTime = "09:35 PM",
                duration = "1h 35m",
                price = 1899.00,
                availableSeats = 35,
                date = date,
                gate = "E22",
                terminal = "T2",
                baggageAllowance = "15kg"
            )
        )

        flightAdapter.updateFlights(sampleFlights)
    }
}

class BookingDetailsActivity {

}
