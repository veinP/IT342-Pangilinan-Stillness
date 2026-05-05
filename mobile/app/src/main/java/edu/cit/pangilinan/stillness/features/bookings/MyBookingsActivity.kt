package edu.cit.pangilinan.stillness.features.bookings

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.auth.LoginActivity
import edu.cit.pangilinan.stillness.features.sessions.SessionsActivity
import edu.cit.pangilinan.stillness.shared.api.ApiClient


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.tabs.TabLayout
import edu.cit.pangilinan.stillness.features.bookings.BookingApi
import edu.cit.pangilinan.stillness.model.ListBookingResponse
import edu.cit.pangilinan.stillness.model.BookingDto
import java.text.SimpleDateFormat
import java.util.*

class MyBookingsActivity : AppCompatActivity() {
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvFeaturedHeader: TextView
    
    private var allBookings: List<BookingDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        setupUI()
        fetchBookings()
    }

    private fun setupUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarBookings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { onBackPressed() }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutBookings)
        tabLayout = findViewById(R.id.tabsBookings)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        tvFeaturedHeader = findViewById(R.id.tvFeaturedHeader)
        
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(emptyList())
        recyclerView.adapter = bookingAdapter

        swipeRefreshLayout.setOnRefreshListener {
            fetchBookings()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                applyTabFilter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

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
                val sessionDate = sdf.parse(booking.session.date)?.time ?: 0
                if (isUpcomingTab) sessionDate >= now - (24 * 60 * 60 * 1000) // Today or later
                else sessionDate < now - (24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                true
            }
        }

        if (filtered.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            bookingAdapter.updateData(emptyList())
            tvFeaturedHeader.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            bookingAdapter.updateData(filtered)
            // Show "Latest confirmed booking" header only on upcoming tab if bookings exist
            tvFeaturedHeader.visibility = if (isUpcomingTab && filtered.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun fetchBookings() {
        val sharedPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val token = sharedPrefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBarBookings)
        if (!swipeRefreshLayout.isRefreshing) {
            progressBar.visibility = View.VISIBLE
        }

        BookingApi.getMyBookings(token, object : ApiClient.ApiCallback<ListBookingResponse> {
            override fun onSuccess(result: ListBookingResponse) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    allBookings = result.data ?: emptyList()
                    applyTabFilter()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this@MyBookingsActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
