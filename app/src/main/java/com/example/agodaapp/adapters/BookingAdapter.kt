package com.example.agodaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agodaapp.databinding.ItemBookingBinding
import com.example.agodaapp.models.Booking

class BookingAdapter(
    private var bookings: MutableList<Booking>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    private var allBookings = ArrayList(bookings)

    fun updateBookings(newBookings: List<Booking>) {
        allBookings.clear()
        allBookings.addAll(newBookings)
        bookings = ArrayList(allBookings)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        bookings = if (query.isEmpty()) {
            ArrayList(allBookings)
        } else {
            allBookings.filter { 
                it.passengerName.contains(query, true) || 
                it.flight?.flightNumber?.contains(query, true) == true ||
                it.flight?.airline?.contains(query, true) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            val airline = booking.flight?.airline ?: "Unknown Airline"
            val flightNum = booking.flight?.flightNumber ?: ""
            val fromCode = booking.flight?.from?.split(" ")?.firstOrNull() ?: "-"
            val toCode = booking.flight?.to?.split(" ")?.firstOrNull() ?: "-"

            binding.tvFlightInfo.text = "$airline - $flightNum"
            binding.tvRoute.text = "$fromCode → $toCode"
            binding.tvDate.text = booking.flight?.getFormattedDate() ?: ""
            binding.tvPassenger.text = "Passenger: ${booking.passengerName}"
            binding.tvSeats.text = "Seats: ${booking.seats}"
            binding.tvPrice.text = String.format("₱%,.2f", booking.totalPrice)
            
            val bookingStatus = booking.status ?: "Confirmed"
            val flightStatus = booking.flight?.status ?: "On Time"
            
            android.util.Log.d("BookingAdapter", "DEBUG: booking.id=${booking.id}, bookingStatus=$bookingStatus, flightStatus=$flightStatus, flightObj=${booking.flight}")
            
            binding.tvBookingStatus.text = bookingStatus
            binding.tvFlightStatus.text = flightStatus
            
            // Apply styles for booking status
            when {
                bookingStatus.equals("Cancelled", true) -> {
                    binding.tvBookingStatus.setTextColor(binding.root.context.getColor(com.example.agodaapp.R.color.agoda_red))
                    binding.tvBookingStatus.background = android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#FFEBEE"))
                }
                else -> {
                    binding.tvBookingStatus.setTextColor(binding.root.context.getColor(com.example.agodaapp.R.color.agoda_green))
                    binding.tvBookingStatus.background = android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#E8F5E9"))
                }
            }

            // Apply styles for flight status
            when {
                flightStatus.equals("Delayed", true) -> {
                    binding.tvFlightStatus.setTextColor(android.graphics.Color.parseColor("#FF8F00")) // Amber
                    binding.tvFlightStatus.background = android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#FFF8E1"))
                }
                flightStatus.equals("Cancelled", true) -> {
                    binding.tvFlightStatus.setTextColor(binding.root.context.getColor(com.example.agodaapp.R.color.agoda_red))
                    binding.tvFlightStatus.background = android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#FFEBEE"))
                }
                else -> {
                    binding.tvFlightStatus.setTextColor(binding.root.context.getColor(com.example.agodaapp.R.color.agoda_green))
                    binding.tvFlightStatus.background = android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#E8F5E9"))
                }
            }

            binding.root.setOnClickListener { onItemClick(booking) }
        }
    }
}