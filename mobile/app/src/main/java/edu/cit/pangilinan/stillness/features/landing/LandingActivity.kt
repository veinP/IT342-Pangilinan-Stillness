package edu.cit.pangilinan.stillness.features.landing

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.auth.LoginActivity
import edu.cit.pangilinan.stillness.features.auth.RegisterActivity
import edu.cit.pangilinan.stillness.features.dashboard.DashboardActivity
import edu.cit.pangilinan.stillness.features.dashboard.QuoteApi
import edu.cit.pangilinan.stillness.features.sessions.SessionAdapter
import edu.cit.pangilinan.stillness.features.sessions.SessionApi
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager


import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.model.QuoteResponse
import edu.cit.pangilinan.stillness.model.SessionResponse

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

        // Bind views
        tvQuoteText = findViewById(R.id.tvQuoteText)
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthor)
        btnRefreshQuote = findViewById(R.id.btnRefreshQuote)
        rvUpcomingSessions = findViewById(R.id.rvUpcomingSessions)

        // ═══ Navbar Buttons (Parity with Web AppNav guest mode) ═══
        findViewById<Button>(R.id.btnNavLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<Button>(R.id.btnNavSignUp).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // ═══ Hero CTA Buttons ═══
        // Parity with Web: Browse Sessions links to /login when user is null
        findViewById<Button>(R.id.btnViewSessions).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        // Sign In with Email button
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // ═══ Sessions RecyclerView (Parity with Web "Upcoming Sessions" .slice(0, 3)) ═══
        rvUpcomingSessions.layoutManager = LinearLayoutManager(this)
        // Parity with Web: Reserve Spot links to /login when user is null
        sessionAdapter = SessionAdapter(emptyList()) { _ ->
            startActivity(Intent(this, LoginActivity::class.java))
        }
        rvUpcomingSessions.adapter = sessionAdapter

        // ═══ Load Data ═══
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
                    tvQuoteText.text = "\"Focus on the present moment.\""
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
