package edu.cit.pangilinan.stillness.features.bookings

import edu.cit.pangilinan.stillness.R
import edu.cit.pangilinan.stillness.features.auth.LoginActivity
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.cit.pangilinan.stillness.model.SessionDto
import edu.cit.pangilinan.stillness.model.SingleBookingResponse
import java.text.SimpleDateFormat
import java.util.*

/**
 * Booking Checkout — Parity with Web BookingCheckoutPage.tsx
 *
 * Flow: SessionDetailActivity → BookingCheckoutActivity → MyBookingsActivity
 * Matches Web: /sessions/:id → /sessions/:id/checkout → /bookings
 */
class BookingCheckoutActivity : AppCompatActivity() {

    private var session: SessionDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_checkout)

        val sessionJson = intent.getStringExtra("SESSION_JSON")
        if (sessionJson == null) {
            Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        session = Gson().fromJson(sessionJson, SessionDto::class.java)
        val s = session!!

        // ═══ Navbar ═══
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { onBackPressed() }

        // ═══ Breadcrumb ═══
        findViewById<TextView>(R.id.tvBreadcrumbTitle).text = s.title

        // ═══ Session Overview Card ═══
        findViewById<TextView>(R.id.tvCheckoutDate).text = formatDate(s.resolvedDate())
        findViewById<TextView>(R.id.tvCheckoutTime).text = "${s.resolvedStartTimeDisplay()} - ${s.resolvedEndTimeDisplay()}"
        findViewById<TextView>(R.id.tvCheckoutLocation).text = s.location ?: "StillNess Studio"

        val remaining = s.capacity - s.enrolledCount
        findViewById<TextView>(R.id.tvCheckoutAvailability).text = "$remaining spots left"

        // ═══ Instructor ═══
        findViewById<TextView>(R.id.tvCheckoutInstructorName).text = s.resolvedInstructorName()
        findViewById<TextView>(R.id.tvCheckoutInstructorType).text = s.resolvedType()
        findViewById<TextView>(R.id.tvCheckoutInstructorInitials).text = s.resolvedInstructorName()
            .split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .take(2)
            .uppercase()

        // ═══ Checkout Card — Price breakdown (Parity with Web totals) ═══
        val fee = s.price
        val tax = fee * 0.09
        val total = fee + tax
        val isFree = total == 0.0

        findViewById<TextView>(R.id.tvCheckoutFee).text = if (fee > 0) "${"$"}${"%.2f".format(fee)}" else "Free"
        findViewById<TextView>(R.id.tvCheckoutTax).text = "${"$"}${"%.2f".format(tax)}"
        findViewById<TextView>(R.id.tvCheckoutTotal).text = "${"$"}${"%.2f".format(total)}"

        // Info banner
        val tvBanner = findViewById<TextView>(R.id.tvCheckoutBannerText)
        tvBanner.text = if (isFree) "No payment required for this class." else "Sandbox card entry for checkout preview."

        // Button text
        val btnConfirm = findViewById<Button>(R.id.btnConfirmBooking)
        btnConfirm.text = if (isFree) "Confirm Free Booking" else "Pay & Confirm Booking"

        // Secure copy
        val tvSecure = findViewById<TextView>(R.id.tvSecureCopy)
        tvSecure.text = if (isFree) "This reservation will be confirmed instantly." else "Secured by Stripe in sandbox mode."

        // Disable form fields for free sessions
        if (isFree) {
            findViewById<EditText>(R.id.etCardholderName).isEnabled = false
            findViewById<EditText>(R.id.etCardNumber).isEnabled = false
            findViewById<EditText>(R.id.etExpiry).isEnabled = false
            findViewById<EditText>(R.id.etCvv).isEnabled = false
        }

        // ═══ Confirm Booking ═══
        btnConfirm.setOnClickListener {
            handleConfirmBooking()
        }
    }

    private fun handleConfirmBooking() {
        val s = session ?: return
        val token = SessionManager.getToken(this)

        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        val btnConfirm = findViewById<Button>(R.id.btnConfirmBooking)
        val tvError = findViewById<TextView>(R.id.tvCheckoutError)
        
        btnConfirm.isEnabled = false
        btnConfirm.text = "Processing..."
        tvError.visibility = View.GONE

        BookingApi.createBooking(token, s.id, object : ApiClient.ApiCallback<SingleBookingResponse> {
            override fun onSuccess(result: SingleBookingResponse) {
                runOnUiThread {
                    Toast.makeText(
                        this@BookingCheckoutActivity,
                        "Booking confirmed for ${s.title}!",
                        Toast.LENGTH_LONG
                    ).show()
                    // Navigate to My Bookings (Parity with Web: navigate('/bookings'))
                    val intent = Intent(this@BookingCheckoutActivity, MyBookingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    btnConfirm.isEnabled = true
                    val isFree = (s.price * 1.09) == 0.0
                    btnConfirm.text = if (isFree) "Confirm Free Booking" else "Pay & Confirm Booking"
                    tvError.text = error
                    tvError.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}
