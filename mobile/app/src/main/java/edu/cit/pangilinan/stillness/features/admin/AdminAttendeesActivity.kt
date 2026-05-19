package edu.cit.pangilinan.stillness.features.admin

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.R
import edu.cit.pangilinan.stillness.model.Attendee
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager

class AdminAttendeesActivity : AppCompatActivity() {

    private lateinit var rvAttendees: RecyclerView
    private lateinit var progressBar: ProgressBar
    private var sessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_attendees)

        sessionId = intent.getStringExtra("SESSION_ID")

        rvAttendees = findViewById(R.id.rv_attendees)
        progressBar = findViewById(R.id.progress_bar)

        rvAttendees.layoutManager = LinearLayoutManager(this)

        if (sessionId != null) {
            loadAttendees(sessionId!!)
        } else {
            Toast.makeText(this, "No Session ID provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAttendees(id: String) {
        val token = SessionManager.getToken(this) ?: return

        progressBar.visibility = View.VISIBLE
        AdminApi.getAdminAttendees(token, id, object : ApiClient.ApiCallback<List<Attendee>> {
            override fun onSuccess(result: List<Attendee>) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    rvAttendees.adapter = AdminAttendeeAdapter(result)
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminAttendeesActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
