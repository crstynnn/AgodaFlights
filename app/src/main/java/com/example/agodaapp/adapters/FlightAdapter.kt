package com.example.agodaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agodaapp.databinding.ItemFlightBinding
import com.example.agodaapp.models.Flight

class FlightAdapter(
    private var flights: MutableList<Flight>,
    private val onFlightSelected: (Flight) -> Unit
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    fun updateFlights(newFlights: List<Flight>) {
        flights.clear()
        flights.addAll(newFlights)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        holder.bind(flights[position])
    }

    override fun getItemCount(): Int = flights.size

    inner class FlightViewHolder(private val binding: ItemFlightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(flight: Flight) {
            binding.tvAirline.text = flight.airline
            binding.tvFlightNumber.text = flight.flightNumber
            binding.tvPrice.text = "₱${String.format("%,.0f", flight.price)}"
            binding.tvDepartureTime.text = flight.departureTime
            binding.tvArrivalTime.text = flight.arrivalTime
            binding.tvDuration.text = flight.duration
            binding.tvFrom.text = flight.from.split(" ").firstOrNull() ?: flight.from
            binding.tvTo.text = flight.to.split(" ").firstOrNull() ?: flight.to
            binding.tvFlightDate.text = flight.date

            binding.tvAvailableSeats.text = when {
                flight.availableSeats == 0 -> "Sold out"
                flight.availableSeats <= 10 -> "⚠ Only ${flight.availableSeats} seats left!"
                flight.availableSeats <= 30 -> "${flight.availableSeats} seats left"
                else -> "${flight.availableSeats} seats available"
            }

            val isSoldOut = flight.availableSeats == 0
            binding.btnSelect.isEnabled = !isSoldOut
            binding.btnSelect.alpha = if (isSoldOut) 0.4f else 1.0f
            binding.btnSelect.text = if (isSoldOut) "Sold Out" else "Select"

            binding.btnSelect.setOnClickListener {
                if (!isSoldOut) onFlightSelected(flight)
            }
            binding.root.setOnClickListener {
                if (!isSoldOut) onFlightSelected(flight)
            }
        }
    }
}