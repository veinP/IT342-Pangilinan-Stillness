package edu.cit.pangilinan.stillness.features.sessions

import edu.cit.pangilinan.stillness.R

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.model.SessionDto
import java.text.SimpleDateFormat
import java.util.*

class SessionAdapter(
    private var sessions: List<SessionDto>,
    private val onClick: (SessionDto) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvSessionTitle)
        val tvInstructor: TextView = itemView.findViewById(R.id.tvSessionInstructor)
        val tvDate: TextView = itemView.findViewById(R.id.tvSessionDate)
        val tvLocation: TextView = itemView.findViewById(R.id.tvSessionLocation)
        val tvSpots: TextView = itemView.findViewById(R.id.tvSessionSpots)
        val tvRatio: TextView = itemView.findViewById(R.id.tvSessionRatio)
        val pbCapacity: android.widget.ProgressBar = itemView.findViewById(R.id.pbCapacity)
        val tvThumbText: TextView = itemView.findViewById(R.id.tvSessionThumbText)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val btnReserve: TextView = itemView.findViewById(R.id.btnReserve)
        val tvAvatarInitials: TextView = itemView.findViewById(R.id.tvAvatarInitials)
        val flSessionThumb: android.widget.FrameLayout = itemView.findViewById(R.id.flSessionThumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        
        holder.tvTitle.text = session.title ?: "Untitled Session"
        
        val instructorName = session.resolvedInstructorName()
        holder.tvInstructor.text = "with $instructorName"
        
        // Avatar initials
        val initials = instructorName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        holder.tvAvatarInitials.text = if (initials.isNotEmpty()) initials else "IN"
        
        // Category tag in thumbnail
        val categoryLower = session.resolvedType().lowercase()
        val tagLabel = when {
            categoryLower.contains("yoga") -> "Yoga"
            categoryLower.contains("breath") -> "Breathwork"
            else -> "Meditation"
        }
        
        // Formatting date/time
        holder.tvDate.text = formatSessionTime(session.resolvedDate(), session.resolvedStartTimeDisplay())
        
        // Location
        holder.tvLocation.text = session.location ?: "TBD"

        // Capacity and Spots
        val booked = session.resolvedBookedCount()
        val capacity = session.capacity
        val remaining = Math.max(capacity - booked, 0)
        
        val pct = if (capacity > 0) Math.min((booked.toFloat() / capacity.toFloat()) * 100, 100f).toInt() else 0
        
        val spotsText = "$remaining spot${if (remaining != 1) "s" else ""} remaining"
        holder.tvSpots.text = spotsText
        holder.tvRatio.text = "$booked/$capacity"
        
        holder.pbCapacity.progress = pct
        
        val spotsColor = when {
            remaining == 0 -> "#EF4444" // Red (full)
            remaining <= 3 -> "#EF4444" // Red
            remaining <= 5 -> "#F59E0B" // Orange
            else -> "#10B981"           // Green
        }
        val colorInt = Color.parseColor(spotsColor)
        holder.tvSpots.setTextColor(colorInt)
        
        holder.pbCapacity.progressTintList = ColorStateList.valueOf(colorInt)

        // Thumbnail text
        holder.tvThumbText.text = when {
            categoryLower.contains("yoga") -> "Yoga Session"
            categoryLower.contains("breath") -> "Breathwork Session"
            else -> "Meditation Session"
        }

        // Price
        holder.tvPrice.text = if (session.price > 0) String.format("$%.2f", session.price) else "Free"

        // Reserve Button state
        if (remaining > 0) {
            holder.btnReserve.text = "Reserve Spot"
            holder.btnReserve.backgroundTintList = null // Use default bg
            holder.btnReserve.setTextColor(Color.WHITE)
            holder.btnReserve.isEnabled = true
            
            holder.itemView.setOnClickListener { onClick(session) }
            holder.btnReserve.setOnClickListener { onClick(session) }
        } else {
            holder.btnReserve.text = "Session Full"
            holder.btnReserve.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E2E8F0"))
            holder.btnReserve.setTextColor(Color.parseColor("#64748B"))
            holder.btnReserve.isEnabled = false
            
            holder.itemView.setOnClickListener(null)
            holder.btnReserve.setOnClickListener(null)
        }
    }

    override fun getItemCount() = sessions.size

    fun updateData(newSessions: List<SessionDto>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    private fun formatSessionTime(dateStr: String, timeStr: String): String {
        if (dateStr.isEmpty() && timeStr.isEmpty()) return "TBA"
        return try {
            val inputDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            val dayFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(inputDate!!)
            "$dayFormat • $timeStr"
        } catch (e: Exception) {
            if (dateStr.isNotEmpty() || timeStr.isNotEmpty()) "$dateStr • $timeStr" else "TBA"
        }
    }
}
