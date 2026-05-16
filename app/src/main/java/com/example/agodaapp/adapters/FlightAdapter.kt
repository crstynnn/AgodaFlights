package com.example.agodaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agodaapp.databinding.ItemFlightBinding
import com.example.agodaapp.models.Flight

class FlightAdapter(
    private var flights: List<Flight>,
    private val onItemClick: (Flight) -> Unit
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        holder.bind(flights[position])
    }

    override fun getItemCount(): Int = flights.size

    fun updateFlights(newFlights: List<Flight>) {
        flights = newFlights
        notifyDataSetChanged()
    }

    inner class FlightViewHolder(private val binding: ItemFlightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(flight: Flight) {
            binding.tvAirline.text = flight.airline
            binding.tvFlightNumber.text = flight.flightNumber
            binding.tvDepartureTime.text = flight.departureTime
            binding.tvArrivalTime.text = flight.arrivalTime
            binding.tvDuration.text = flight.duration
            binding.tvPrice.text = String.format("₱%.2f", flight.price)

            binding.root.setOnClickListener {
                onItemClick(flight)
            }
        }
    }
}