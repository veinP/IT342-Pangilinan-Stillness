package edu.cit.pangilinan.stillness.features.bookings

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.sessions.SessionDetailActivity


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
        val btnCancel: TextView = itemView.findViewById(R.id.btnCancelBooking)
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

        // Title
        holder.tvTitle.text = s.title

        // Category badge (Parity with Web getBadgeClass)
        val categoryLower = s.resolvedType().lowercase()
        val tagLabel = when {
            categoryLower.contains("yoga") -> "Yoga"
            categoryLower.contains("breath") -> "Breathwork"
            categoryLower.contains("meditat") -> "Meditation"
            else -> s.category
        }
        holder.tvCategory.text = tagLabel

        val tagBgColor = when {
            categoryLower.contains("yoga") -> "#D1FAE5"
            categoryLower.contains("breath") -> "#FEF3C7"
            categoryLower.contains("meditat") -> "#DBEAFE"
            else -> "#E5E7EB"
        }
        val tagTextColor = when {
            categoryLower.contains("yoga") -> "#065F46"
            categoryLower.contains("breath") -> "#92400E"
            categoryLower.contains("meditat") -> "#0284C7"
            else -> "#374151"
        }
        holder.tvCategory.backgroundTintList = ColorStateList.valueOf(Color.parseColor(tagBgColor))
        holder.tvCategory.setTextColor(Color.parseColor(tagTextColor))

        // Date & Time (Parity with Web fmtDate and fmtTimeRange)
        holder.tvDate.text = formatSessionDate(s.resolvedDate())
        holder.tvTime.text = "${s.resolvedStartTimeDisplay()} - ${s.resolvedEndTimeDisplay()}"

        // Booking number
        holder.tvNumber.text = "#${booking.bookingNumber}"

        // Price
        holder.tvPrice.text = if (booking.amount > 0) "${"$"}${"%.2f".format(booking.amount)}" else "Free"

        // Status Pill (Parity with Web statusPill)
        when (booking.status.uppercase()) {
            "CONFIRMED" -> {
                holder.tvStatusPill.text = "Confirmed"
                setStatusPillColors(holder.tvStatusPill, "#DCFCE7", "#166534", "#BBF7D0")
                holder.tvThumbText.text = "Confirmed"
                holder.tvThumbText.setTextColor(Color.parseColor("#15803D"))
                holder.flThumb.setBackgroundColor(Color.parseColor("#F0FDF4"))
            }
            "CANCELLED" -> {
                holder.tvStatusPill.text = "Cancelled"
                setStatusPillColors(holder.tvStatusPill, "#FECACA", "#991B1B", "#FCA5A5")
                holder.tvThumbText.text = "Cancelled"
                holder.tvThumbText.setTextColor(Color.parseColor("#991B1B"))
                holder.flThumb.setBackgroundColor(Color.parseColor("#FEF2F2"))
            }
            else -> {
                holder.tvStatusPill.text = booking.status
                setStatusPillColors(holder.tvStatusPill, "#FEF3C7", "#92400E", "#FDE68A")
                holder.tvThumbText.text = "Session"
                holder.tvThumbText.setTextColor(Color.parseColor("#92400E"))
                holder.flThumb.setBackgroundColor(Color.parseColor("#FEF3C7"))
            }
        }

        // Payment Status (Parity with Web paymentPill)
        when (booking.paymentStatus.uppercase()) {
            "PAID" -> {
                holder.tvPaymentStatus.text = "Paid"
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#15803D"))
            }
            "REFUNDED" -> {
                holder.tvPaymentStatus.text = "Refunded"
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#991B1B"))
            }
            "FAILED" -> {
                holder.tvPaymentStatus.text = "Failed"
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#DC2626"))
            }
            else -> {
                holder.tvPaymentStatus.text = "Pending"
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#92400E"))
            }
        }

        // Cancel button — show only for confirmed bookings with cancellableUntil in future
        val canCancel = booking.status.equals("CONFIRMED", true) &&
                booking.cancellableUntil != null
        holder.btnCancel.visibility = if (canCancel) View.VISIBLE else View.GONE

        // View Details button
        holder.btnViewDetails.setOnClickListener {
            val intent = Intent(context, SessionDetailActivity::class.java)
            intent.putExtra("SESSION_JSON", Gson().toJson(s))
            context.startActivity(intent)
        }
    }

    private fun setStatusPillColors(tv: TextView, bgColor: String, textColor: String, borderColor: String) {
        tv.setTextColor(Color.parseColor(textColor))
        val bg = tv.background
        if (bg is GradientDrawable) {
            bg.setColor(Color.parseColor(bgColor))
            bg.setStroke(1, Color.parseColor(borderColor))
        } else {
            // Fallback: create a new drawable
            val gd = GradientDrawable()
            gd.shape = GradientDrawable.RECTANGLE
            gd.cornerRadius = 99f * tv.context.resources.displayMetrics.density
            gd.setColor(Color.parseColor(bgColor))
            gd.setStroke((1 * tv.context.resources.displayMetrics.density).toInt(), Color.parseColor(borderColor))
            tv.background = gd
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
