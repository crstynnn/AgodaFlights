package com.example.agodaapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agodaapp.R
import com.example.agodaapp.adapters.FlightAdapter
import com.example.agodaapp.databinding.ActivityFlightListBinding
import com.example.agodaapp.databinding.DialogFilterSortBinding
import com.example.agodaapp.models.Flight
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlightListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlightListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var flightAdapter: FlightAdapter
    private var allFlights = mutableListOf<Flight>()
    private var currentBookedSeats: Map<String, Int> = emptyMap()
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
        fromCode = intent.getStringExtra("from_code") ?: if(from.length >= 3) from.substring(0, 3) else ""
        toCode = intent.getStringExtra("to_code") ?: if(to.length >= 3) to.substring(0, 3) else ""

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_filter) {
                showFilterSortDialog()
                true
            } else false
        }

        binding.tvSearchInfo.text = "$from → $to | $date | $passengers passenger(s)"

        setupRecyclerView()
        loadFlightsFromFirestore()
    }

    private fun setupRecyclerView() {
        flightAdapter = FlightAdapter(
            flights = mutableListOf(),
            bookedSeats = emptyMap(),
            onFlightSelected = { flight ->
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
                    putExtra("passenger_count", passengers)
                    putExtra("from_airport", from)
                    putExtra("to_airport", to)
                    putExtra("from_code", fromCode)
                    putExtra("to_code", toCode)
                }
                startActivity(intent)
            }
        )

        binding.rvFlights.layoutManager = LinearLayoutManager(this)
        binding.rvFlights.adapter = flightAdapter
    }

    private fun loadFlightsFromFirestore() {
        Log.d("SearchDebug", "Searching: from=$from, to=$to, date=$date")
        
        fun getCode(input: String): String = if (input.length >= 3) input.substring(0, 3).trim() else input
        fun normalizeDate(input: String): String {
            return try {
                val sdfInput = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdfOutput.format(sdfInput.parse(input) ?: Date())
            } catch (e: Exception) { input }
        }

        val searchFromCode = getCode(from)
        val searchToCode = getCode(to)
        val searchDateNormalized = normalizeDate(date)

        // Listen for bookings first to have the counts ready
        firestore.collection("bookings").addSnapshotListener { bookingsSnapshot, _ ->
            val bookedSeatsPerFlight = mutableMapOf<String, Int>()
            bookingsSnapshot?.forEach { doc ->
                val fId = doc.getString("flightId") ?: ""
                if (fId.isNotEmpty()) {
                    // Try to get seat count from seatNumbers list first, fallback to 'seats' field
                    val seatsList = doc.get("seatNumbers") as? List<*>
                    val count = seatsList?.size ?: doc.getLong("seats")?.toInt() ?: 0
                    bookedSeatsPerFlight[fId] = bookedSeatsPerFlight.getOrDefault(fId, 0) + count
                }
            }
            currentBookedSeats = bookedSeatsPerFlight
            
            // Only load flights once we have booking data (or if bookings are empty)
            // But we don't want to nest listeners. Let's trigger a refresh instead.
            refreshFlightList(searchFromCode, searchToCode, searchDateNormalized)
        }

        // Listen for flights separately
        firestore.collection("flights").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val all = snapshot.documents.mapNotNull { doc ->
                    val f = doc.toObject(Flight::class.java)
                    // Ensure flight ID is present, fallback to document ID
                    f?.copy(id = if (f.id.isEmpty()) doc.id else f.id)
                }
                allFlights = all.toMutableList()
                refreshFlightList(searchFromCode, searchToCode, searchDateNormalized)
            }
        }
    }

    private fun refreshFlightList(searchFromCode: String, searchToCode: String, searchDateNormalized: String) {
        fun getCode(input: String): String = if (input.length >= 3) input.substring(0, 3).trim() else input
        fun normalizeDate(input: String): String {
            return try {
                val sdfInput = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdfOutput.format(sdfInput.parse(input) ?: Date())
            } catch (e: Exception) { input }
        }

        val filtered = allFlights.filter { flight ->
            val flightFromCode = getCode(flight.from)
            val flightToCode = getCode(flight.to)
            val flightDateNormalized = normalizeDate(flight.getFormattedDate())
            
            val matchFrom = flightFromCode.equals(searchFromCode, true)
            val matchTo = flightToCode.equals(searchToCode, true)
            val matchDate = flightDateNormalized == searchDateNormalized
            
            val takenCount = currentBookedSeats.getOrDefault(flight.id, 0)
            val isAvailable = takenCount < flight.availableSeats

            matchFrom && matchTo && matchDate && isAvailable
        }
        
        flightAdapter.updateData(filtered.toMutableList(), currentBookedSeats)
        applyFiltersAndSort() // Re-apply existing filters/sort
    }

    private fun showFilterSortDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogFilterSortBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(dialogBinding.root)

        val uniqueAirlines = allFlights.map { it.airline }.distinct()
        for (airline in uniqueAirlines) {
            val checkBox = CheckBox(this).apply { text = airline }
            dialogBinding.llAirlines.addView(checkBox)
        }

        dialogBinding.btnApply.setOnClickListener {
            // Logic to collect selections
            val selectedAirlines = mutableListOf<String>()
            for (i in 0 until dialogBinding.llAirlines.childCount) {
                val cb = dialogBinding.llAirlines.getChildAt(i) as CheckBox
                if (cb.isChecked) selectedAirlines.add(cb.text.toString())
            }

            val sortId = dialogBinding.rgSort.checkedRadioButtonId
            applyFiltersAndSort(selectedAirlines, sortId)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun applyFiltersAndSort(selectedAirlines: List<String> = emptyList(), sortId: Int = -1) {
        var filtered = allFlights.toList()

        if (selectedAirlines.isNotEmpty()) {
            filtered = filtered.filter { it.airline in selectedAirlines }
        }

        when (sortId) {
            R.id.rbPriceLow -> filtered = filtered.sortedBy { it.price }
            R.id.rbPriceHigh -> filtered = filtered.sortedByDescending { it.price }
            R.id.rbDuration -> filtered = filtered.sortedBy { it.duration } // simplified
        }

        binding.tvFlightCount.text = "${filtered.size} flights found"
        flightAdapter.updateData(filtered.toMutableList(), currentBookedSeats)
    }
}