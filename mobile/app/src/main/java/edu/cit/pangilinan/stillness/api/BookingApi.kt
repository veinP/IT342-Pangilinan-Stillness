package edu.cit.pangilinan.stillness.api

import android.util.Log
import edu.cit.pangilinan.stillness.model.BookingRequest
import edu.cit.pangilinan.stillness.model.ListBookingResponse
import edu.cit.pangilinan.stillness.model.SingleBookingResponse
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

object BookingApi {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1"
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()
    private val gson = Gson()

    fun createBooking(token: String, sessionId: String, callback: ApiClient.ApiCallback<SingleBookingResponse>) {
        val requestBody = gson.toJson(BookingRequest(sessionId)).toRequestBody(JSON_MEDIA)
        val request = Request.Builder()
            .url("$BASE_URL/bookings")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                try {
                    val bookingResponse = gson.fromJson(responseBody, SingleBookingResponse::class.java)
                    if (response.isSuccessful) {
                        callback.onSuccess(bookingResponse)
                    } else {
                        val errorMsg = bookingResponse.error?.message ?: "Booking failed (${response.code})"
                        callback.onError(errorMsg)
                    }
                } catch (e: Exception) {
                    Log.e("BookingApi", "Parse error: ${e.message}")
                    callback.onError("Failed to parse response")
                }
            }
        })
    }

    fun getMyBookings(token: String, callback: ApiClient.ApiCallback<ListBookingResponse>) {
        val request = Request.Builder()
            .url("$BASE_URL/bookings/me")
            .get()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                try {
                    val listResponse = gson.fromJson(responseBody, ListBookingResponse::class.java)
                    if (response.isSuccessful) {
                        callback.onSuccess(listResponse)
                    } else {
                        val errorMsg = listResponse.error?.message ?: "Failed to fetch bookings (${response.code})"
                        callback.onError(errorMsg)
                    }
                } catch (e: Exception) {
                    Log.e("BookingApi", "Parse error: ${e.message}")
                    callback.onError("Failed to parse response")
                }
            }
        })
    }
}
