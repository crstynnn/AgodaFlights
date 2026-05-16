package com.example.agodaapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agodaapp.databinding.ItemBookingBinding
import com.example.agodaapp.models.Booking

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount(): Int = bookings.size

    fun updateBookings(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvFlightInfo.text = "${booking.flight?.airline} - ${booking.flight?.flightNumber}"
            binding.tvRoute.text = "${booking.flight?.from} → ${booking.flight?.to}"
            binding.tvDate.text = booking.flight?.date
            binding.tvPassenger.text = "Passenger: ${booking.passengerName}"
            binding.tvPrice.text = String.format("₱%.2f", booking.totalPrice)
            binding.tvStatus.text = booking.status

            binding.root.setOnClickListener {
                onItemClick(booking)
            }
        }
    }
}