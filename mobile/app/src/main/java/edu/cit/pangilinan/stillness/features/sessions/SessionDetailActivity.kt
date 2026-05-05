package edu.cit.pangilinan.stillness.features.sessions

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.auth.LoginActivity
import edu.cit.pangilinan.stillness.features.bookings.BookingApi
import edu.cit.pangilinan.stillness.features.bookings.MyBookingsActivity
import edu.cit.pangilinan.stillness.shared.api.ApiClient


import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import edu.cit.pangilinan.stillness.model.SessionDto
import edu.cit.pangilinan.stillness.model.SingleBookingResponse
import java.text.SimpleDateFormat
import java.util.*

class SessionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        val sessionJson = intent.getStringExtra("SESSION_JSON")
        if (sessionJson == null) {
            Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val session = Gson().fromJson(sessionJson, SessionDto::class.java)

        setupToolbar(session.title)
        populateSessionDetails(session)

        findViewById<Button>(R.id.btnBookSession).setOnClickListener {
            handleBooking(session.id)
        }
    }

    private fun setupToolbar(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "" // Using custom hero title instead
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun populateSessionDetails(session: SessionDto) {
        // Hero section
        findViewById<TextView>(R.id.tvDetailBadge).text = session.category.uppercase()
        findViewById<TextView>(R.id.tvDetailHeroTitle).text = session.title

        // About section
        findViewById<TextView>(R.id.tvDetailDescription).text = session.description

        // Instructor section
        findViewById<TextView>(R.id.tvDetailInstructorName).text = session.instructorName
        findViewById<TextView>(R.id.tvInstructorInitials).text = session.instructorName
            .split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .take(2)
            .uppercase()
        
        // Mock bio/exp if not in DTO (matching web fallback logic)
        findViewById<TextView>(R.id.tvInstructorBio).text = "Start your day with clarity and intention. This guided ${session.category.lowercase()} session focuses on breath awareness and mindful presence."
        findViewById<TextView>(R.id.tvInstructorExp).text = "8 years experience"

        // Details Sidebar (matching web layout)
        setupDetailRow(R.id.rowDate, android.R.drawable.ic_menu_my_calendar, "Date", formatDate(session.date))
        setupDetailRow(R.id.rowTime, android.R.drawable.ic_menu_recent_history, "Time", "${session.startTime} - ${session.endTime}")
        setupDetailRow(R.id.rowLocation, android.R.drawable.ic_dialog_map, "Location", session.location ?: "Studio A - Downtown", "123 Wellness Way, Suite 100")
        setupDetailRow(R.id.rowDuration, android.R.drawable.ic_menu_recent_history, "Duration", "60 minutes")

        // Capacity
        val enrolled = session.enrolledCount
        val capacity = session.capacity
        val remaining = capacity - enrolled
        val pct = if (capacity > 0) (enrolled.toFloat() / capacity * 100).toInt() else 0
        
        findViewById<TextView>(R.id.tvCapacityRatio).text = "$enrolled of $capacity spots filled"
        val pbCapacity = findViewById<ProgressBar>(R.id.pbCapacity)
        pbCapacity.progress = pct
        
        val tvRemaining = findViewById<TextView>(R.id.tvRemainingSpots)
        tvRemaining.text = "$remaining spot${if (remaining != 1) "s" else ""} remaining"
        
        val colorHex = when {
            remaining <= 2 -> "#EF4444"
            remaining <= 5 -> "#F59E0B"
            else -> "#10B981"
        }
        val colorInt = Color.parseColor(colorHex)
        tvRemaining.setTextColor(colorInt)
        pbCapacity.progressTintList = ColorStateList.valueOf(colorInt)

        // Price
        findViewById<TextView>(R.id.tvDetailPrice).text = if (session.price > 0) "$${"%.2f".format(session.price)}" else "Free"
        
        val btnBook = findViewById<Button>(R.id.btnBookSession)
        if (remaining <= 0) {
            btnBook.isEnabled = false
            btnBook.text = "Session Full"
            btnBook.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9CA3AF"))
        }
    }

    private fun setupDetailRow(viewId: Int, iconRes: Int, label: String, value: String, subValue: String? = null) {
        val row = findViewById<View>(viewId)
        row.findViewById<ImageView>(R.id.ivDetailIcon).setImageResource(iconRes)
        row.findViewById<TextView>(R.id.tvDetailLabel).text = label
        row.findViewById<TextView>(R.id.tvDetailValue).text = value
        val tvSub = row.findViewById<TextView>(R.id.tvDetailSubValue)
        if (subValue != null) {
            tvSub.text = subValue
            tvSub.visibility = View.VISIBLE
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun handleBooking(sessionId: String) {
        val sharedPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val token = sharedPrefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        val btnBook = findViewById<Button>(R.id.btnBookSession)
        btnBook.isEnabled = false
        btnBook.text = "Processing..."

        BookingApi.createBooking(token, sessionId, object : ApiClient.ApiCallback<SingleBookingResponse> {
            override fun onSuccess(result: SingleBookingResponse) {
                runOnUiThread {
                    Toast.makeText(this@SessionDetailActivity, "Booking Successful!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@SessionDetailActivity, MyBookingsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    btnBook.isEnabled = true
                    btnBook.text = "Reserve Spot"
                    Toast.makeText(this@SessionDetailActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
