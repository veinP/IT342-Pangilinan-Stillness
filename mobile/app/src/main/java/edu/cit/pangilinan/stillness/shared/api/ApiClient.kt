package edu.cit.pangilinan.stillness.shared.api

import edu.cit.pangilinan.stillness.model.User


import android.util.Log
import edu.cit.pangilinan.stillness.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ApiClient {
    // Production backend on Render
    const val BASE_URL = "https://it342-pangilinan-stillness.onrender.com/api/v1"
    
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    interface ApiCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: String)
    }

    fun login(email: String, password: String, callback: ApiCallback<LoginResponse>) {
        val body = gson.toJson(LoginRequest(email, password))
            .toRequestBody(JSON_MEDIA)

        val request = Request.Builder()
            .url("$BASE_URL/auth/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LOGIN_FAIL", "Network Error: ${e.message}")
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                
                if (response.isSuccessful) {
                    try {
                        val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                        Log.d("LOGIN_SUCCESS", "Code: ${response.code}")
                        callback.onSuccess(loginResponse)
                    } catch (e: Exception) {
                        Log.e("LOGIN_FAIL", "Parse error: ${e.message}")
                        callback.onError("Failed to parse response")
                    }
                } else {
                    Log.e("LOGIN_FAIL", "Code: ${response.code}")
                    try {
                        val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                        callback.onError(loginResponse.error?.message ?: "Login failed")
                    } catch (e: Exception) {
                        callback.onError("Login failed (${response.code})")
                    }
                }
            }
        })
    }



    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        callback: ApiCallback<RegisterResponse>
    ) {
        val registerRequest = RegisterRequest(
            fullName = fullName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            role = "ROLE_USER"
        )
        val body = gson.toJson(registerRequest)
            .toRequestBody(JSON_MEDIA)

        val request = Request.Builder()
            .url("$BASE_URL/auth/register")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                try {
                    val registerResponse = gson.fromJson(responseBody, RegisterResponse::class.java)
                    if (response.isSuccessful && registerResponse.success) {
                        callback.onSuccess(registerResponse)
                    } else {
                        val errorMsg = registerResponse.error?.message ?: "Registration failed (${response.code})"
                        callback.onError(errorMsg)
                    }
                } catch (e: Exception) {
                    callback.onError("Failed to parse response")
                }
            }
        })
    }

    fun getProfile(token: String, callback: ApiCallback<User>) {
        val request = Request.Builder()
            .url("$BASE_URL/auth/me")
            .addHeader("Authorization", "Bearer $token")
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
                        val jsonObj = gson.fromJson(responseBody, JsonObject::class.java)
                        val dataObj = jsonObj.getAsJsonObject("data")
                        val userObj = dataObj.getAsJsonObject("user")
                        val user = gson.fromJson(userObj, User::class.java)
                        callback.onSuccess(user)
                    } catch (e: Exception) {
                        callback.onError("Failed to parse response")
                    }
                } else {
                    callback.onError("Failed to get profile (${response.code})")
                }
            }
        })
    }
}
