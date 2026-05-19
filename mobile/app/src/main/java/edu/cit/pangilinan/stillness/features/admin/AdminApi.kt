package edu.cit.pangilinan.stillness.features.admin

import edu.cit.pangilinan.stillness.model.*
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonObject

object AdminApi {
    private val BASE_URL = ApiClient.BASE_URL
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getAdminPayments(token: String, callback: ApiClient.ApiCallback<AdminPaymentsResponse>) {
        val request = Request.Builder()
            .url("$BASE_URL/admin/payments?page=0&limit=100")
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
                    val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)
                    if (response.isSuccessful && jsonObj.has("data")) {
                        val dataObj = jsonObj.getAsJsonObject("data")
                        val summaryObj = dataObj.getAsJsonObject("summary")
                        val recordsArray = dataObj.getAsJsonArray("records") ?: dataObj.getAsJsonArray("content")

                        val summary = gson.fromJson(summaryObj, PaymentSummary::class.java)
                        val listType = object : TypeToken<List<PaymentRecord>>() {}.type
                        val records: List<PaymentRecord> = gson.fromJson(recordsArray, listType) ?: emptyList()

                        callback.onSuccess(AdminPaymentsResponse(summary, records))
                    } else {
                        callback.onError("Failed to fetch payments")
                    }
                } catch (e: Exception) {
                    callback.onError("Failed to parse response")
                }
            }
        })
    }

    fun getAdminAttendees(token: String, sessionId: String, callback: ApiClient.ApiCallback<List<Attendee>>) {
        val request = Request.Builder()
            .url("$BASE_URL/admin/sessions/$sessionId/attendees")
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
                    val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)
                    if (response.isSuccessful && jsonObj.has("data")) {
                        val dataElem = jsonObj.get("data")
                        val listType = object : TypeToken<List<Attendee>>() {}.type
                        val attendees: List<Attendee> = if (dataElem.isJsonArray) {
                            gson.fromJson(dataElem.asJsonArray, listType)
                        } else {
                            gson.fromJson(dataElem.asJsonObject.getAsJsonArray("attendees"), listType)
                        } ?: emptyList()

                        callback.onSuccess(attendees)
                    } else {
                        callback.onError("Failed to fetch attendees")
                    }
                } catch (e: Exception) {
                    callback.onError("Failed to parse response")
                }
            }
        })
    }

    fun createSession(token: String, requestDto: SessionRequest, callback: ApiClient.ApiCallback<GenericResponse>) {
        val body = gson.toJson(requestDto).toRequestBody(JSON_MEDIA)
        val request = Request.Builder()
            .url("$BASE_URL/sessions")
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(GenericResponse(response.isSuccessful, null, null))
            }
        })
    }

    fun updateSession(token: String, sessionId: String, requestDto: SessionRequest, callback: ApiClient.ApiCallback<GenericResponse>) {
        val body = gson.toJson(requestDto).toRequestBody(JSON_MEDIA)
        val request = Request.Builder()
            .url("$BASE_URL/sessions/$sessionId")
            .put(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(GenericResponse(response.isSuccessful, null, null))
            }
        })
    }

    fun deleteSession(token: String, sessionId: String, callback: ApiClient.ApiCallback<GenericResponse>) {
        val request = Request.Builder()
            .url("$BASE_URL/sessions/$sessionId")
            .delete()
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(GenericResponse(response.isSuccessful, null, null))
            }
        })
    }
}
