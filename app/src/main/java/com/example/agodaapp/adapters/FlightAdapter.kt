package com.example.agodaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agodaapp.databinding.ItemFlightBinding
import com.example.agodaapp.models.Flight

class FlightAdapter(
    private var flights: MutableList<Flight>,
    private var bookedSeats: Map<String, Int> = emptyMap(),
    private val onFlightSelected: (Flight) -> Unit,
    private val isAdminMode: Boolean = false
) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    private var allFlights = ArrayList(flights)

    fun updateData(newFlights: List<Flight>, newBookedSeats: Map<String, Int>) {
        allFlights.clear()
        allFlights.addAll(newFlights)
        flights = ArrayList(allFlights)
        bookedSeats = newBookedSeats
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        flights = if (query.isEmpty()) {
            ArrayList(allFlights)
        } else {
            allFlights.filter { 
                it.airline.contains(query, true) || 
                it.flightNumber.contains(query, true) ||
                it.from.contains(query, true) ||
                it.to.contains(query, true)
            }.toMutableList()
        }
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
            binding.tvDepartureTime.text = flight.getFormattedDepartureTime()
            binding.tvArrivalTime.text = flight.getFormattedArrivalTime()
            binding.tvDuration.text = flight.duration
            binding.tvFrom.text = flight.from.split(" ").firstOrNull() ?: flight.from
            binding.tvTo.text = flight.to.split(" ").firstOrNull() ?: flight.to
            binding.tvFlightDate.text = flight.getFormattedDate()

            val taken = bookedSeats.getOrDefault(flight.id, 0)
            val remaining = (flight.availableSeats - taken).coerceAtLeast(0)
            
            binding.tvAvailableSeats.text = "$remaining/${flight.availableSeats} available"

            val isSoldOut = remaining == 0
            val isCancelled = flight.status.equals("Cancelled", true)
            val isDelayed = flight.status.equals("Delayed", true)
            
            binding.tvStatus.text = flight.status
            when {
                isCancelled -> {
                    binding.tvStatus.setTextColor(binding.root.context.getColor(com.example.agodaapp.R.color.agoda_red))
                }
                isDelayed -> {
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#FFC107")) // Amber/Yellow
                }
                else -> {
                    binding.tvStatus.setTextColor(binding.root.context.getColor(com.example.agodaapp.R.color.agoda_green))
                }
            }
            
            if (isCancelled) {
                binding.btnSelect.text = "Cancelled"
                binding.btnSelect.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(com.example.agodaapp.R.color.agoda_red)
                )
                binding.btnSelect.isEnabled = false
                binding.btnDelay.visibility = android.view.View.GONE
            } else if (isAdminMode) {
                binding.btnSelect.text = "Cancel"
                binding.btnSelect.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(com.example.agodaapp.R.color.agoda_red)
                )
                binding.btnSelect.isEnabled = true
                binding.btnDelay.visibility = android.view.View.VISIBLE
                binding.btnDelay.text = if (isDelayed) "Delay Updated" else "Delay Flight"
            } else {
                binding.btnSelect.text = if (isSoldOut) "Fully Booked" else "Book"
                binding.btnSelect.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    binding.root.context.getColor(com.example.agodaapp.R.color.agoda_blue)
                )
                binding.btnSelect.isEnabled = !isSoldOut
                binding.btnDelay.visibility = android.view.View.GONE
            }
            
            binding.btnSelect.alpha = if (binding.btnSelect.isEnabled) 1.0f else 0.8f
            binding.btnSelect.setTextColor(binding.root.context.getColor(com.example.agodaapp.R.color.white))

            binding.btnSelect.setOnClickListener {
                if (isAdminMode || (!isSoldOut && !isCancelled)) onFlightSelected(flight)
            }
            binding.btnDelay.setOnClickListener {
                (binding.root.context as? com.example.agodaapp.activities.AdminManageFlightsActivity)?.showDelayConfirmation(flight)
            }
            binding.root.setOnClickListener {
                if (isAdminMode || (!isSoldOut && !isCancelled)) onFlightSelected(flight)
            }
        }
    }
}