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
        val tvCategory: TextView = itemView.findViewById(R.id.tvSessionCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvSessionDate)
        val tvSpots: TextView = itemView.findViewById(R.id.tvSessionSpots)
        val ivSpotsIcon: ImageView = itemView.findViewById(R.id.ivSpotsIcon)
        val tvThumbText: TextView = itemView.findViewById(R.id.tvSessionThumbText)
        val btnReserve: TextView = itemView.findViewById(R.id.btnReserve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        val context = holder.itemView.context

        holder.tvTitle.text = session.title ?: "Untitled Session"
        holder.tvInstructor.text = "with ${session.instructorName ?: "Instructor"}"
        holder.tvCategory.text = (session.category ?: "General").uppercase(Locale.ROOT)
        
        // Formatting date/time to match web app fmtTime helper
        holder.tvDate.text = formatSessionTime(session.date ?: "", session.startTime ?: "")

        // Spots logic (Parity with Web getSpots helper)
        val remaining = session.capacity - session.enrolledCount
        val spotsText = "$remaining spot${if (remaining != 1) "s" else ""} remaining"
        holder.tvSpots.text = spotsText
        
        val spotsColor = when {
            remaining <= 2 -> "#EF4444" // Red
            remaining <= 5 -> "#F59E0B" // Orange
            else -> "#10B981"           // Green
        }
        val colorInt = Color.parseColor(spotsColor)
        holder.tvSpots.setTextColor(colorInt)
        ImageViewCompat.setImageTintList(holder.ivSpotsIcon, ColorStateList.valueOf(colorInt))

        // Thumbnail text (Parity with Web getThumbLabel helper)
        val categoryLower = (session.category ?: "").lowercase()
        holder.tvThumbText.text = when {
            categoryLower.contains("yoga") -> "Yoga Session"
            categoryLower.contains("breath") -> "Breathwork Session"
            else -> "Meditation Session"
        }

        holder.itemView.setOnClickListener { onClick(session) }
        holder.btnReserve.setOnClickListener { onClick(session) }
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
