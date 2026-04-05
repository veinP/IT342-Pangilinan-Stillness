package edu.cit.pangilinan.stillness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import edu.cit.pangilinan.stillness.model.SessionDto
import edu.cit.pangilinan.stillness.api.BookingApi
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.model.SingleBookingResponse

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

        val toolbar = findViewById<Toolbar>(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = session.title
        toolbar.setNavigationOnClickListener { onBackPressed() }

        findViewById<TextView>(R.id.tvDetailTitle).text = session.title
        findViewById<TextView>(R.id.tvDetailInstructor).text = "Instructor: ${session.instructorName}"
        findViewById<TextView>(R.id.tvDetailCategory).text = session.category
        findViewById<TextView>(R.id.tvDetailDate).text = "${session.date}\n${session.startTime} - ${session.endTime}"
        findViewById<TextView>(R.id.tvDetailDescription).text = session.description
        
        val tvPrice = findViewById<TextView>(R.id.tvDetailPrice)
        if (session.price > 0) {
            tvPrice.text = "$${session.price}"
        } else {
            tvPrice.text = "Free"
            tvPrice.setTextColor(getColor(android.R.color.holo_blue_dark))
        }

        findViewById<Button>(R.id.btnBookSession).setOnClickListener {
            val sharedPrefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
            val token = sharedPrefs.getString("token", null)

            if (token == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                return@setOnClickListener
            }

            BookingApi.createBooking(token, session.id, object : ApiClient.ApiCallback<SingleBookingResponse> {
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
                        Toast.makeText(this@SessionDetailActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }
}
