package edu.cit.pangilinan.stillness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.model.SessionDto

class SessionAdapter(
    private var sessions: List<SessionDto>,
    private val onClick: (SessionDto) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvSessionTitle)
        val tvInstructor: TextView = itemView.findViewById(R.id.tvSessionInstructor)
        val tvCategory: TextView = itemView.findViewById(R.id.tvSessionCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvSessionDate)
        val tvPrice: TextView = itemView.findViewById(R.id.tvSessionPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.tvTitle.text = session.title
        holder.tvInstructor.text = session.instructorName
        holder.tvCategory.text = session.category
        holder.tvDate.text = "${session.date} - ${session.startTime}"
        
        if (session.price > 0) {
            holder.tvPrice.text = "$${session.price}"
        } else {
            holder.tvPrice.text = "Free"
            holder.tvPrice.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_dark))
        }

        holder.itemView.setOnClickListener {
            onClick(session)
        }
    }

    override fun getItemCount() = sessions.size

    fun updateData(newSessions: List<SessionDto>) {
        sessions = newSessions
        notifyDataSetChanged()
    }
}
