package edu.cit.pangilinan.stillness

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.api.SessionApi
import edu.cit.pangilinan.stillness.model.SessionResponse
import edu.cit.pangilinan.stillness.model.SessionDto

class SessionsActivity : AppCompatActivity() {
    private lateinit var sessionAdapter: SessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sessions)

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewSessions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        sessionAdapter = SessionAdapter(emptyList()) { session ->
            val intent = Intent(this, SessionDetailActivity::class.java)
            intent.putExtra("SESSION_JSON", com.google.gson.Gson().toJson(session))
            startActivity(intent)
        }
        recyclerView.adapter = sessionAdapter

        fetchSessions()
    }

    private fun fetchSessions() {
        val sharedPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val token = sharedPrefs.getString("token", null)

        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)
        progressBar.visibility = android.view.View.VISIBLE

        SessionApi.getSessions(token, object : ApiClient.ApiCallback<SessionResponse> {
            override fun onSuccess(result: SessionResponse) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    val sessions = result.data ?: emptyList()
                    sessionAdapter.updateData(sessions)
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@SessionsActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
