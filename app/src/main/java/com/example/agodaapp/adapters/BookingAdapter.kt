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

    fun updateBookings(newBookings: List<Booking>) {
        bookings.clear()
        bookings.addAll(newBookings)
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
            binding.tvDate.text = booking.flight?.date ?: ""
            binding.tvPassenger.text = "Passenger: ${booking.passengerName}"
            binding.tvSeats.text = "Seats: ${booking.seats}"
            binding.tvPrice.text = String.format("₱%,.2f", booking.totalPrice)
            binding.tvStatus.text = booking.status

            binding.root.setOnClickListener { onItemClick(booking) }
        }
    }
}