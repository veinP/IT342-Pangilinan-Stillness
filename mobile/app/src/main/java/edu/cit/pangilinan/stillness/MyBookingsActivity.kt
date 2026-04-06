package edu.cit.pangilinan.stillness

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.api.BookingApi
import edu.cit.pangilinan.stillness.model.ListBookingResponse
import edu.cit.pangilinan.stillness.model.BookingDto

class MyBookingsActivity : AppCompatActivity() {
    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        val toolbar = findViewById<Toolbar>(R.id.toolbarBookings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewBookings)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookingAdapter = BookingAdapter(emptyList())
        recyclerView.adapter = bookingAdapter

        fetchBookings()
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
        progressBar.visibility = android.view.View.VISIBLE

        BookingApi.getMyBookings(token, object : ApiClient.ApiCallback<ListBookingResponse> {
            override fun onSuccess(result: ListBookingResponse) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    val bookings = result.data ?: emptyList()
                    bookingAdapter.updateData(bookings)
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@MyBookingsActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
