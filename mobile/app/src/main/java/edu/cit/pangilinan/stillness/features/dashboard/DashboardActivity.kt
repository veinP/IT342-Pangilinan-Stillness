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
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.pangilinan.stillness.features.dashboard.QuoteApi
import edu.cit.pangilinan.stillness.model.QuoteResponse
import edu.cit.pangilinan.stillness.model.SessionDto
import edu.cit.pangilinan.stillness.model.SessionResponse
import edu.cit.pangilinan.stillness.model.User
import java.util.*

class DashboardActivity : Activity() {

    private lateinit var sessionAdapter: SessionAdapter
    private lateinit var rvSessions: RecyclerView
    private lateinit var tvQuoteText: TextView
    private lateinit var tvQuoteAuthor: TextView
    private lateinit var btnRefreshQuote: ImageButton
    private lateinit var tvGreeting: TextView
    private lateinit var etSearch: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tvEmptyState: TextView

    private var allSessions: List<SessionDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupUI()
        loadData()
    }

    private fun setupUI() {
        // ═══ Navbar (Parity with Web AppNav logged-in mode) ═══
        findViewById<TextView>(R.id.nav_logout).setOnClickListener { showLogoutDialog() }

        findViewById<TextView>(R.id.btnNavSessions).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }
        findViewById<TextView>(R.id.btnNavBookings).setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        // ═══ Hero Section ═══
        tvGreeting = findViewById(R.id.tvGreeting)

        findViewById<Button>(R.id.btnRefreshSessions).setOnClickListener {
            val token = SessionManager.getToken(this) ?: return@setOnClickListener
            fetchAllSessions(token)
        }

        // ═══ Quote Section (Parity with Web .sc-quote) ═══
        tvQuoteText = findViewById(R.id.tvQuoteTextDash)
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthorDash)
        btnRefreshQuote = findViewById(R.id.btnRefreshQuoteDash)
        btnRefreshQuote.setOnClickListener {
            val rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)
            btnRefreshQuote.startAnimation(rotation)
            fetchQuote()
        }

        // ═══ Search / Filter (Parity with Web .sc-controls) ═══
        etSearch = findViewById(R.id.etSearch)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        // Category Spinner setup
        val categories = arrayOf("All Types", "Meditation", "Yoga", "Breathwork")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Search button
        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            applyFilters()
        }

        // Live search as user types
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ═══ Sessions RecyclerView ═══
        rvSessions = findViewById(R.id.rvDashboardSessions)
        rvSessions.layoutManager = LinearLayoutManager(this)
        sessionAdapter = SessionAdapter(emptyList()) { session ->
            val intent = Intent(this, SessionDetailActivity::class.java)
            intent.putExtra("SESSION_JSON", com.google.gson.Gson().toJson(session))
            startActivity(intent)
        }
        rvSessions.adapter = sessionAdapter
    }

    private fun applyFilters() {
        val query = etSearch.text.toString().lowercase(Locale.ROOT)
        val selectedCategory = spinnerCategory.selectedItem?.toString() ?: "All Types"

        val filteredList = allSessions.filter { session ->
            val matchesSearch = session.title.lowercase(Locale.ROOT).contains(query) ||
                    session.resolvedInstructorName().lowercase(Locale.ROOT).contains(query)

            val matchesCategory = selectedCategory == "All Types" ||
                    session.resolvedType().equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesCategory
        }

        sessionAdapter.updateData(filteredList)
        tvEmptyState.visibility = if (filteredList.isEmpty() && allSessions.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadData() {
        val token = SessionManager.getToken(this)
        if (token == null) {
            goToLogin()
            return
        }

        loadProfile(token)
        fetchQuote()
        fetchAllSessions(token)
    }

    private fun loadProfile(token: String) {
        val progress = findViewById<ProgressBar>(R.id.progress_dashboard)
        progress.visibility = View.VISIBLE

        ApiClient.getProfile(token, object : ApiClient.ApiCallback<User> {
            override fun onSuccess(result: User) {
                runOnUiThread {
                    progress.visibility = View.GONE

                    // Greeting (Parity with Web: "Hi, {firstName}")
                    val firstName = result.fullName.split(" ").firstOrNull() ?: "there"
                    tvGreeting.text = "Hi, $firstName"

                    // Navbar user info
                    findViewById<TextView>(R.id.nav_welcome).text = firstName

                    // Avatar initials
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

    private fun fetchAllSessions(token: String) {
        val progress = findViewById<ProgressBar>(R.id.progress_dashboard)
        progress.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE

        SessionApi.getSessions(token, object : ApiClient.ApiCallback<SessionResponse> {
            override fun onSuccess(result: SessionResponse) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    if (result.success && result.data != null) {
                        allSessions = result.data.sessions
                        applyFilters()
                    } else {
                        tvEmptyState.visibility = View.VISIBLE
                    }
                }
            }
            override fun onError(error: String) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    tvEmptyState.visibility = View.VISIBLE
                    tvEmptyState.text = "Failed to load sessions."
                }
            }
        })
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
