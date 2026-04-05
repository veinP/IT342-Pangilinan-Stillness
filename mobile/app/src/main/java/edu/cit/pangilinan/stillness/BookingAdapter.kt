package edu.cit.pangilinan.stillness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.model.BookingDto

class BookingAdapter(
    private var bookings: List<BookingDto>
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStatus: TextView = itemView.findViewById(R.id.tvBookingStatus)
        val tvNumber: TextView = itemView.findViewById(R.id.tvBookingNumber)
        val tvTitle: TextView = itemView.findViewById(R.id.tvBookingSessionTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvBookingDate)
        val tvPrice: TextView = itemView.findViewById(R.id.tvBookingPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.tvNumber.text = "Booking #${booking.bookingNumber}"
        holder.tvStatus.text = booking.status
        holder.tvTitle.text = booking.session.title
        holder.tvDate.text = "${booking.session.date} - ${booking.session.startTime}"
        
        if (booking.amount > 0) {
            holder.tvPrice.text = "$${booking.amount}"
            holder.tvPrice.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.tvPrice.text = "Free"
            holder.tvPrice.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_dark))
        }

        when (booking.status) {
            "CONFIRMED" -> holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            "CANCELLED" -> holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            else -> holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<BookingDto>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
