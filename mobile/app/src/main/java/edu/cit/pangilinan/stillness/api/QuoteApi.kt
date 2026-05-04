package edu.cit.pangilinan.stillness.api

import com.google.gson.Gson
import edu.cit.pangilinan.stillness.model.QuoteResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object QuoteApi {
    private val BASE_URL = ApiClient.BASE_URL.replace("/auth", "").replace("/bookings", "").replace("/sessions", "")
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getRandomQuote(callback: ApiClient.ApiCallback<QuoteResponse>) {
        val request = Request.Builder()
            .url("${ApiClient.BASE_URL.substringBeforeLast("/")}/quotes/random")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val quoteResponse = gson.fromJson(responseBody, QuoteResponse::class.java)
                        callback.onSuccess(quoteResponse)
                    } catch (e: Exception) {
                        callback.onError("Failed to parse response")
                    }
                } else {
                    callback.onError("Failed to fetch quote (${response.code})")
                }
            }
        })
    }
}
