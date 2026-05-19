package edu.cit.pangilinan.stillness.features.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.R
import edu.cit.pangilinan.stillness.model.SessionDto
import java.text.SimpleDateFormat
import java.util.*

class AdminSessionAdapter(
    private val sessions: List<SessionDto>,
    private val onAttendeesClick: (SessionDto) -> Unit,
    private val onEditClick: (SessionDto) -> Unit,
    private val onDeleteClick: (SessionDto) -> Unit
) : RecyclerView.Adapter<AdminSessionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvDetails: TextView = view.findViewById(R.id.tv_details)
        val btnAttendees: Button = view.findViewById(R.id.btn_attendees)
        val btnEdit: Button = view.findViewById(R.id.btn_edit)
        val btnDelete: Button = view.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.tvTitle.text = session.title

        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateObj = parser.parse(session.startTime)
            val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            val formattedDate = dateObj?.let { formatter.format(it) } ?: session.startTime
            holder.tvDetails.text = "${session.type} | ${session.location} | $formattedDate"
        } catch (e: Exception) {
            holder.tvDetails.text = "${session.type} | ${session.location} | ${session.startTime}"
        }

        holder.btnAttendees.setOnClickListener { onAttendeesClick(session) }
        holder.btnEdit.setOnClickListener { onEditClick(session) }
        holder.btnDelete.setOnClickListener { onDeleteClick(session) }
    }

    override fun getItemCount() = sessions.size
}
