package edu.cit.pangilinan.stillness.api

import android.util.Log
import edu.cit.pangilinan.stillness.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1"
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()
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
                Log.e("LOGIN_FAIL", "Code: 0")
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                
                if (response.isSuccessful) {
                    try {
                        val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                        Log.d("LOGIN_SUCCESS", "Code: ${response.code}")
                        Log.d("LOGIN_SUCCESS", "Token: ${loginResponse.data?.token}")
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

    fun googleLogin(idToken: String, callback: ApiCallback<LoginResponse>) {
        val json = JsonObject()
        json.addProperty("idToken", idToken)
        val body = json.toString().toRequestBody(JSON_MEDIA)

        val request = Request.Builder()
            .url("$BASE_URL/auth/google")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LOGIN_FAIL", "Code: 0")
                callback.onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    try {
                        val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                        Log.d("LOGIN_SUCCESS", "Code: ${response.code}")
                        Log.d("LOGIN_SUCCESS", "Token: ${loginResponse.data?.token}")
                        callback.onSuccess(loginResponse)
                    } catch (e: Exception) {
                        Log.e("LOGIN_FAIL", "Parse error: ${e.message}")
                        callback.onError("Failed to parse response")
                    }
                } else {
                    Log.e("LOGIN_FAIL", "Code: ${response.code}")
                    try {
                        val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)
                        callback.onError(loginResponse.error?.message ?: "Google login failed")
                    } catch (e: Exception) {
                        callback.onError("Google login failed (${response.code})")
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
