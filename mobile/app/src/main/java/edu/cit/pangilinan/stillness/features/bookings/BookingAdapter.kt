package edu.cit.pangilinan.stillness.features.bookings

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.sessions.SessionDetailActivity


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import edu.cit.pangilinan.stillness.model.BookingDto
import java.text.SimpleDateFormat
import java.util.*

class BookingAdapter(
    private var bookings: List<BookingDto>
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStatusPill: TextView = itemView.findViewById(R.id.tvBookingStatusPill)
        val tvNumber: TextView = itemView.findViewById(R.id.tvBookingNumber)
        val tvTitle: TextView = itemView.findViewById(R.id.tvBookingSessionTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvBookingCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvBookingDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvBookingTime)
        val tvPrice: TextView = itemView.findViewById(R.id.tvBookingPrice)
        val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)
        val tvThumbText: TextView = itemView.findViewById(R.id.tvBookingThumbText)
        val flThumb: FrameLayout = itemView.findViewById(R.id.flBookingThumb)
        val btnViewDetails: TextView = itemView.findViewById(R.id.btnViewSessionDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        val s = booking.session
        val context = holder.itemView.context

        holder.tvNumber.text = booking.bookingNumber
        holder.tvTitle.text = s.title
        holder.tvCategory.text = s.category.uppercase()
        
        // Date & Time formatting (Parity with Web fmtDate and fmtTimeRange)
        holder.tvDate.text = formatSessionDate(s.date)
        holder.tvTime.text = "${s.startTime} - ${s.endTime}"

        // Price formatting
        holder.tvPrice.text = if (booking.amount > 0) "$${"%.2f".format(booking.amount)}" else "Free"

        // Status Pill (Parity with Web statusPill)
        holder.tvStatusPill.text = booking.status.uppercase()
        when (booking.status.uppercase()) {
            "CONFIRMED" -> {
                holder.tvStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F0FDF4"))
                holder.tvStatusPill.setTextColor(Color.parseColor("#15803D"))
                holder.tvThumbText.text = "Confirmed"
                holder.flThumb.setBackgroundColor(Color.parseColor("#F0FDF4"))
            }
            "CANCELLED" -> {
                holder.tvStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEF2F2"))
                holder.tvStatusPill.setTextColor(Color.parseColor("#DC2626"))
                holder.tvThumbText.text = "Cancelled"
                holder.flThumb.setBackgroundColor(Color.parseColor("#FEF2F2"))
            }
            else -> {
                holder.tvStatusPill.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F9FAFB"))
                holder.tvStatusPill.setTextColor(Color.parseColor("#6B7280"))
                holder.tvThumbText.text = "Session"
                holder.flThumb.setBackgroundColor(Color.parseColor("#F3F4F6"))
            }
        }

        // Payment Pill (Parity with Web paymentPill)
        holder.tvPaymentStatus.text = booking.paymentStatus.uppercase()
        when (booking.paymentStatus.uppercase()) {
            "PAID" -> holder.tvPaymentStatus.setTextColor(Color.parseColor("#15803D"))
            "REFUNDED", "FAILED" -> holder.tvPaymentStatus.setTextColor(Color.parseColor("#DC2626"))
            else -> holder.tvPaymentStatus.setTextColor(Color.parseColor("#F59E0B"))
        }

        holder.btnViewDetails.setOnClickListener {
            val intent = Intent(context, SessionDetailActivity::class.java)
            // Note: We need to pass the full SessionDto. 
            // In a real app we'd fetch it, but here we'll pass the session from booking
            intent.putExtra("SESSION_JSON", Gson().toJson(s))
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<BookingDto>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    private fun formatSessionDate(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}
