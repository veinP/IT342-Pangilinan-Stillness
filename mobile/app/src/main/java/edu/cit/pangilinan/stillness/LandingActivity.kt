package edu.cit.pangilinan.stillness

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.api.QuoteApi
import edu.cit.pangilinan.stillness.api.SessionApi
import edu.cit.pangilinan.stillness.model.QuoteResponse
import edu.cit.pangilinan.stillness.model.SessionResponse
import edu.cit.pangilinan.stillness.auth.SessionManager

class LandingActivity : AppCompatActivity() {

    private lateinit var tvQuoteText: TextView
    private lateinit var tvQuoteAuthor: TextView
    private lateinit var btnRefreshQuote: ImageButton
    private lateinit var rvUpcomingSessions: RecyclerView
    private lateinit var sessionAdapter: SessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Redirect if already logged in (Parity with Web App behavior)
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_landing)

        tvQuoteText = findViewById(R.id.tvQuoteText)
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthor)
        btnRefreshQuote = findViewById(R.id.btnRefreshQuote)
        rvUpcomingSessions = findViewById(R.id.rvUpcomingSessions)

        // Setup Buttons
        findViewById<Button>(R.id.btnViewSessions).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Setup RecyclerView for Upcoming Sessions (Parity with Web "Upcoming Sessions" section)
        rvUpcomingSessions.layoutManager = LinearLayoutManager(this)
        sessionAdapter = SessionAdapter(emptyList()) { session ->
            val intent = Intent(this, SessionDetailActivity::class.java)
            intent.putExtra("SESSION_JSON", com.google.gson.Gson().toJson(session))
            startActivity(intent)
        }
        rvUpcomingSessions.adapter = sessionAdapter

        // Load Initial Data
        fetchQuote()
        fetchUpcomingSessions()

        btnRefreshQuote.setOnClickListener {
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            btnRefreshQuote.startAnimation(rotation)
            fetchQuote()
        }
    }

    private fun fetchQuote() {
        QuoteApi.getRandomQuote(object : ApiClient.ApiCallback<QuoteResponse> {
            override fun onSuccess(result: QuoteResponse) {
                runOnUiThread {
                    if (result.success && result.data != null) {
                        tvQuoteText.text = "\"${result.data.text}\""
                        tvQuoteAuthor.text = "— ${result.data.author}"
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    tvQuoteText.text = "Focus on the present moment."
                    tvQuoteAuthor.text = "— StillNess"
                }
            }
        })
    }

    private fun fetchUpcomingSessions() {
        SessionApi.getSessions(null, object : ApiClient.ApiCallback<SessionResponse> {
            override fun onSuccess(result: SessionResponse) {
                runOnUiThread {
                    if (result.success && result.data != null) {
                        // Show only top 3 sessions on landing page (Parity with Web .slice(0, 3))
                        val upcoming = result.data.sessions.take(3)
                        sessionAdapter.updateData(upcoming)
                    }
                }
            }

            override fun onError(error: String) {
                // Silently fail for landing page sessions
            }
        })
    }
}
