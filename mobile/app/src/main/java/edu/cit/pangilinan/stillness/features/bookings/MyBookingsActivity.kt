package edu.cit.pangilinan.stillness.features.bookings

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.auth.LoginActivity
import edu.cit.pangilinan.stillness.features.sessions.SessionsActivity
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager


import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import edu.cit.pangilinan.stillness.features.dashboard.DashboardActivity
import edu.cit.pangilinan.stillness.model.User

import edu.cit.pangilinan.stillness.features.bookings.BookingApi
import edu.cit.pangilinan.stillness.model.ListBookingResponse
import edu.cit.pangilinan.stillness.model.BookingDto
import java.text.SimpleDateFormat
import java.util.*

class MyBookingsActivity : AppCompatActivity() {
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var layoutFeaturedHeader: LinearLayout
    
    private var allBookings: List<BookingDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        setupUI()
        fetchBookings()
    }

    private fun setupUI() {
        // ═══ Navbar ═══
        findViewById<TextView>(R.id.btnNavSessions).setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        findViewById<TextView>(R.id.btnNavBookings).setOnClickListener {
            // Already on My Bookings
        }
        findViewById<TextView>(R.id.nav_logout).setOnClickListener {
            showLogoutDialog()
        }

        // ═══ Tabs (Parity with Web mb-tabs: Upcoming / Past Bookings) ═══
        tabLayout = findViewById(R.id.tabsBookings)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        layoutFeaturedHeader = findViewById(R.id.layoutFeaturedHeader)
        
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(emptyList())
        recyclerView.adapter = bookingAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                applyTabFilter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // ═══ Empty State Browse button ═══
        findViewById<Button>(R.id.btnBrowseSessions).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
            finish()
        }
    }

    private fun applyTabFilter() {
        val isUpcomingTab = tabLayout.selectedTabPosition == 0
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val filtered = allBookings.filter { booking ->
            try {
                val sessionDate = sdf.parse(booking.session.resolvedDate())?.time ?: 0
                if (isUpcomingTab) sessionDate >= now - (24 * 60 * 60 * 1000) // Today or later
                else sessionDate < now - (24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                true
            }
        }

        if (filtered.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            bookingAdapter.updateData(emptyList())
            layoutFeaturedHeader.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            bookingAdapter.updateData(filtered)
            // Show "Latest confirmed booking" header only on upcoming tab
            layoutFeaturedHeader.visibility = if (isUpcomingTab && filtered.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun fetchBookings() {
        val token = SessionManager.getToken(this)

        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBarBookings)
        progressBar.visibility = View.VISIBLE

        BookingApi.getMyBookings(token, object : ApiClient.ApiCallback<ListBookingResponse> {
            override fun onSuccess(result: ListBookingResponse) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    allBookings = result.data ?: emptyList()
                    applyTabFilter()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MyBookingsActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
        
        loadProfile(token)
    }

    private fun loadProfile(token: String) {
        ApiClient.getProfile(token, object : ApiClient.ApiCallback<User> {
            override fun onSuccess(result: User) {
                runOnUiThread {
                    val firstName = result.fullName.split(" ").firstOrNull() ?: "User"
                    findViewById<TextView>(R.id.nav_welcome)?.text = firstName
                    
                    findViewById<TextView>(R.id.tv_avatar_initials)?.text = result.fullName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .joinToString("")
                        .take(2)
                        .uppercase()
                }
            }
            override fun onError(error: String) {
                runOnUiThread {
                    if (error.contains("401") || error.contains("403")) {
                        SessionManager.clearSession(this@MyBookingsActivity)
                        goToLogin()
                    }
                }
            }
        })
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
