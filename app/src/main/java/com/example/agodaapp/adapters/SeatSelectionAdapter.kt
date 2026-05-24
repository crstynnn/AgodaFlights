package com.example.agodaapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agodaapp.R

enum class SeatStatus { AVAILABLE, TAKEN, SELECTED }

data class Seat(val id: String, var status: SeatStatus = SeatStatus.AVAILABLE)

class SeatSelectionAdapter(
    private val seats: List<Seat>,
    private val onSeatSelected: (Seat) -> Unit
) : RecyclerView.Adapter<SeatSelectionAdapter.SeatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = seats[position]
        holder.bind(seat)
        holder.itemView.setOnClickListener {
            if (seat.status != SeatStatus.TAKEN) {
                // Toggle status
                seat.status = if (seat.status == SeatStatus.SELECTED) SeatStatus.AVAILABLE else SeatStatus.SELECTED
                notifyItemChanged(position)
                onSeatSelected(seat)
            }
        }
    }
    // ... remainder unchanged
    override fun getItemCount() = seats.size

    class SeatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSeat = view.findViewById<TextView>(R.id.tvSeat)
        
        fun bind(seat: Seat) {
            tvSeat.text = seat.id
            when (seat.status) {
                SeatStatus.AVAILABLE -> tvSeat.setBackgroundResource(R.drawable.bg_seat_available)
                SeatStatus.TAKEN -> tvSeat.setBackgroundResource(R.color.gray_300)
                SeatStatus.SELECTED -> tvSeat.setBackgroundResource(R.drawable.bg_seat_selected)
            }
        }
    }
}