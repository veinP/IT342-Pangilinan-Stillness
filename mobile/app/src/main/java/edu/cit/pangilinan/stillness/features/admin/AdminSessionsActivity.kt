package edu.cit.pangilinan.stillness.features.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.R
import edu.cit.pangilinan.stillness.features.sessions.SessionApi
import edu.cit.pangilinan.stillness.model.SessionDto
import edu.cit.pangilinan.stillness.model.SessionResponse
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager
import edu.cit.pangilinan.stillness.model.GenericResponse

class AdminSessionsActivity : AppCompatActivity() {

    private lateinit var rvSessions: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCreateSession: Button
    private lateinit var btnPayments: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_sessions)

        rvSessions = findViewById(R.id.rv_sessions)
        progressBar = findViewById(R.id.progress_bar)
        btnCreateSession = findViewById(R.id.btn_create_session)
        btnPayments = findViewById(R.id.btn_payments)
        btnLogout = findViewById(R.id.btn_logout)

        rvSessions.layoutManager = LinearLayoutManager(this)

        btnCreateSession.setOnClickListener {
            Toast.makeText(this, "Create session not fully implemented on mobile yet.", Toast.LENGTH_SHORT).show()
        }

        btnPayments.setOnClickListener {
            startActivity(Intent(this, AdminPaymentsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            SessionManager.clearSession(this)
            val intent = Intent(this, edu.cit.pangilinan.stillness.features.auth.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        loadSessions()
    }

    private fun loadSessions() {
        val token = SessionManager.getToken(this) ?: return

        progressBar.visibility = View.VISIBLE
        SessionApi.getSessions(token, object : ApiClient.ApiCallback<SessionResponse> {
            override fun onSuccess(result: SessionResponse) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    val adapter = AdminSessionAdapter(
                        result.data?.sessions ?: emptyList(),
                        onAttendeesClick = { session ->
                            val intent = Intent(this@AdminSessionsActivity, AdminAttendeesActivity::class.java)
                            intent.putExtra("SESSION_ID", session.id)
                            startActivity(intent)
                        },
                        onEditClick = { session ->
                            Toast.makeText(this@AdminSessionsActivity, "Edit ${session.title}", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = { session ->
                            deleteSession(session.id)
                        }
                    )
                    rvSessions.adapter = adapter
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminSessionsActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun deleteSession(sessionId: String) {
        val token = SessionManager.getToken(this) ?: return
        progressBar.visibility = View.VISIBLE
        AdminApi.deleteSession(token, sessionId, object : ApiClient.ApiCallback<GenericResponse> {
            override fun onSuccess(result: GenericResponse) {
                runOnUiThread {
                    Toast.makeText(this@AdminSessionsActivity, "Session deleted", Toast.LENGTH_SHORT).show()
                    loadSessions()
                }
            }
            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminSessionsActivity, "Failed to delete: $error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
