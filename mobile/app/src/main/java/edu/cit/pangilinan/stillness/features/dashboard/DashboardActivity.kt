package edu.cit.pangilinan.stillness.features.dashboard

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.auth.LoginActivity
import edu.cit.pangilinan.stillness.features.bookings.MyBookingsActivity
import edu.cit.pangilinan.stillness.features.sessions.SessionAdapter
import edu.cit.pangilinan.stillness.features.sessions.SessionApi
import edu.cit.pangilinan.stillness.features.sessions.SessionDetailActivity
import edu.cit.pangilinan.stillness.features.sessions.SessionsActivity
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.features.dashboard.QuoteApi
import edu.cit.pangilinan.stillness.model.QuoteResponse
import edu.cit.pangilinan.stillness.model.SessionResponse
import edu.cit.pangilinan.stillness.model.User
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : Activity() {

    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var rvSessions: RecyclerView
    private lateinit var tvQuoteText: TextView
    private lateinit var tvQuoteAuthor: TextView
    private lateinit var btnRefreshQuote: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupUI()
        loadData()
    }

    private fun setupUI() {
        // Navbar: Welcome text + Logout
        findViewById<TextView>(R.id.nav_logout).setOnClickListener { showLogoutDialog() }
        
        findViewById<Button>(R.id.btnNavSessions).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnNavBookings).setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        findViewById<TextView>(R.id.tvViewAllSessions).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }

        // Quote section
        tvQuoteText = findViewById(R.id.tvQuoteTextDash)
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthorDash)
        btnRefreshQuote = findViewById(R.id.btnRefreshQuoteDash)
        btnRefreshQuote.setOnClickListener {
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            btnRefreshQuote.startAnimation(rotation)
            fetchQuote()
        }

        // Sessions RecyclerView
        rvSessions = findViewById(R.id.rvDashboardSessions)
        rvSessions.layoutManager = LinearLayoutManager(this)
        sessionAdapter = SessionAdapter(emptyList()) { session ->
            val intent = Intent(this, SessionDetailActivity::class.java)
            intent.putExtra("SESSION_JSON", com.google.gson.Gson().toJson(session))
            startActivity(intent)
        }
        rvSessions.adapter = sessionAdapter
    }

    private fun loadData() {
        val token = SessionManager.getToken(this)
        if (token == null) {
            goToLogin()
            return
        }

        loadProfile(token)
        fetchQuote()
        fetchUpcomingSessions(token)
    }

    private fun loadProfile(token: String) {
        val progress = findViewById<ProgressBar>(R.id.progress_dashboard)
        progress.visibility = View.VISIBLE

        ApiClient.getProfile(token, object : ApiClient.ApiCallback<User> {
            override fun onSuccess(result: User) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    
                    findViewById<TextView>(R.id.tv_email).text = result.email
                    findViewById<TextView>(R.id.tv_member_since).text = formatDate(result.createdAt)
                    findViewById<TextView>(R.id.tv_full_name_summary).text = result.fullName
                    findViewById<TextView>(R.id.tv_role_summary).text = result.role.replace("ROLE_", "").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                    
                    findViewById<TextView>(R.id.nav_welcome).text = "Welcome, ${result.fullName.split(" ").firstOrNull() ?: "User"}"
                    
                    findViewById<TextView>(R.id.tv_avatar_initials).text = result.fullName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .joinToString("")
                        .take(2)
                        .uppercase()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    if (error.contains("401") || error.contains("403")) {
                        SessionManager.clearSession(this@DashboardActivity)
                        goToLogin()
                    }
                }
            }
        })
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
            override fun onError(error: String) {}
        })
    }

    private fun fetchUpcomingSessions(token: String) {
        SessionApi.getSessions(token, object : ApiClient.ApiCallback<SessionResponse> {
            override fun onSuccess(result: SessionResponse) {
                runOnUiThread {
                    if (result.success && result.data != null) {
                        // Parity with Web: Slice(0, 4)
                        sessionAdapter.updateData(result.data.sessions.take(4))
                    }
                }
            }
            override fun onError(error: String) {}
        })
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "—"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        val btnLogout = dialog.findViewById<Button>(R.id.btn_logout_confirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_logout_cancel)

        btnLogout.setOnClickListener {
            SessionManager.clearSession(this)
            goToLogin()
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
