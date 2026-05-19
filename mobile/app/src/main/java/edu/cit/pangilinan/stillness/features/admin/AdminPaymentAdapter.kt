package edu.cit.pangilinan.stillness.features.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.R
import edu.cit.pangilinan.stillness.model.PaymentRecord
import java.text.SimpleDateFormat
import java.util.*

class AdminPaymentAdapter(
    private val records: List<PaymentRecord>
) : RecyclerView.Adapter<AdminPaymentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBookingNumber: TextView = view.findViewById(R.id.tv_booking_number)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val tvUserName: TextView = view.findViewById(R.id.tv_user_name)
        val tvSessionTitle: TextView = view.findViewById(R.id.tv_session_title)
        val tvAmount: TextView = view.findViewById(R.id.tv_amount)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_payment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.tvBookingNumber.text = record.bookingNumber
        holder.tvStatus.text = record.status
        
        when (record.status.uppercase()) {
            "PAID" -> holder.tvStatus.setTextColor(Color.parseColor("#22c55e"))
            "FAILED" -> holder.tvStatus.setTextColor(Color.parseColor("#ef4444"))
            "REFUNDED" -> holder.tvStatus.setTextColor(Color.parseColor("#f59e0b"))
            else -> holder.tvStatus.setTextColor(Color.parseColor("#64748b"))
        }
        
        holder.tvUserName.text = record.userName
        holder.tvSessionTitle.text = record.sessionTitle
        holder.tvAmount.text = String.format("$%.2f", record.amount)

        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val dateObj = parser.parse(record.date)
            val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val formattedDate = dateObj?.let { formatter.format(it) } ?: record.date
            holder.tvDate.text = formattedDate
        } catch (e: Exception) {
            holder.tvDate.text = record.date
        }
    }

    override fun getItemCount() = records.size
}
