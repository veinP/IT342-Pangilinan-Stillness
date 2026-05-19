package edu.cit.pangilinan.stillness.features.auth

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.dashboard.DashboardActivity
import edu.cit.pangilinan.stillness.shared.api.ApiClient
import edu.cit.pangilinan.stillness.shared.auth.SessionManager


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import edu.cit.pangilinan.stillness.model.LoginResponse

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvError = findViewById<TextView>(R.id.tv_error)
        val tvGoRegister = findViewById<TextView>(R.id.tv_go_register)
        val ivToggle = findViewById<ImageView>(R.id.iv_toggle_password)
        val progressLogin = findViewById<ProgressBar>(R.id.progress_login)

        // Password toggle
        var isPasswordVisible = false
        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        ivToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.transformationMethod = if (isPasswordVisible) null
            else PasswordTransformationMethod.getInstance()
            ivToggle.setImageResource(
                if (isPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
            )
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            tvError.visibility = View.GONE
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvError.text = "Please fill in both email and password"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            progressLogin.visibility = View.VISIBLE

            ApiClient.login(email, password, object : ApiClient.ApiCallback<LoginResponse> {
                override fun onSuccess(result: LoginResponse) {
                    runOnUiThread {
                        val token = result.data?.token
                        val role = result.data?.user?.role
                        if (!token.isNullOrEmpty()) {
                            Log.d("LOGIN_SUCCESS", "Status Code: 200 OK")
                            SessionManager.saveToken(this@LoginActivity, token)
                            SessionManager.saveEmail(this@LoginActivity, email)
                            if (role != null) {
                                SessionManager.saveRole(this@LoginActivity, role)
                            }
                            
                            val targetActivity = if (role == "ROLE_INSTRUCTOR") {
                                Class.forName("edu.cit.pangilinan.stillness.features.admin.AdminSessionsActivity")
                            } else {
                                DashboardActivity::class.java
                            }
                            
                            startActivity(Intent(this@LoginActivity, targetActivity))
                            finish()
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            tvError.text = result.error?.message ?: "Login failed"
                            tvError.visibility = View.VISIBLE
                            btnLogin.isEnabled = true
                            progressLogin.visibility = View.GONE
                        }
                    }
                }

                override fun onError(error: String) {
                    runOnUiThread {
                        Log.e("LOGIN_FAIL", "Error: $error")
                        tvError.text = error
                        tvError.visibility = View.VISIBLE
                        btnLogin.isEnabled = true
                        progressLogin.visibility = View.GONE
                    }
                }
            })
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAffinity()
    }
}
