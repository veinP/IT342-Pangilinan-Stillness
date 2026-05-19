package edu.cit.pangilinan.stillness.features.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.R
import edu.cit.pangilinan.stillness.model.Attendee

class AdminAttendeeAdapter(
    private val attendees: List<Attendee>
) : RecyclerView.Adapter<AdminAttendeeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFullName: TextView = view.findViewById(R.id.tv_full_name)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val tvEmail: TextView = view.findViewById(R.id.tv_email)
        val tvBookingNumber: TextView = view.findViewById(R.id.tv_booking_number)
        val tvPayment: TextView = view.findViewById(R.id.tv_payment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_attendee, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attendee = attendees[position]
        holder.tvFullName.text = attendee.fullName
        holder.tvStatus.text = attendee.status
        holder.tvEmail.text = attendee.email
        holder.tvBookingNumber.text = attendee.bookingNumber
        
        if (attendee.paid) {
            holder.tvPayment.text = "Paid"
            holder.tvPayment.setTextColor(Color.parseColor("#22c55e"))
        } else {
            holder.tvPayment.text = "Pending"
            holder.tvPayment.setTextColor(Color.parseColor("#f59e0b"))
        }
        
        when (attendee.status.uppercase()) {
            "CONFIRMED" -> holder.tvStatus.setTextColor(Color.parseColor("#2563eb"))
            "CANCELLED" -> holder.tvStatus.setTextColor(Color.parseColor("#ef4444"))
            else -> holder.tvStatus.setTextColor(Color.parseColor("#64748b"))
        }
    }

    override fun getItemCount() = attendees.size
}
