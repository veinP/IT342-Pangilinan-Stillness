package edu.cit.pangilinan.stillness.api

import android.util.Log
import edu.cit.pangilinan.stillness.model.SessionResponse
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import okhttp3.OkHttpClient

object SessionApi {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1"
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getSessions(token: String?, callback: ApiClient.ApiCallback<SessionResponse>) {
        val requestBuilder = Request.Builder().url("$BASE_URL/sessions").get()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        client.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val sessionResponse = gson.fromJson(responseBody, SessionResponse::class.java)
                        callback.onSuccess(sessionResponse)
                    } catch (e: Exception) {
                        Log.e("SessionApi", "Parse error: ${e.message}")
                        callback.onError("Failed to parse response")
                    }
                } else {
                    callback.onError("Failed to fetch sessions (${response.code})")
                }
            }
        })
    }
}
