package edu.cit.pangilinan.stillness

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.model.RegisterResponse

class RegisterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullName = findViewById<EditText>(R.id.et_full_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val tvError = findViewById<TextView>(R.id.tv_error)
        val tvSuccess = findViewById<TextView>(R.id.tv_success)
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_login)
        val ivToggle = findViewById<ImageView>(R.id.iv_toggle_password)
        val progressRegister = findViewById<ProgressBar>(R.id.progress_register)

        // Password toggle (toggles both password and confirm password)
        var isPasswordVisible = false
        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        ivToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val method = if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
            etPassword.transformationMethod = method
            etConfirmPassword.transformationMethod = method
            ivToggle.setImageResource(
                if (isPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
            )
            etPassword.setSelection(etPassword.text.length)
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        btnRegister.setOnClickListener {
            tvError.visibility = View.GONE
            tvSuccess.visibility = View.GONE

            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validation
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                tvError.text = "All fields are required"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                tvError.text = "Passwords do not match"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            progressRegister.visibility = View.VISIBLE

            ApiClient.register(fullName, email, password, confirmPassword,
                object : ApiClient.ApiCallback<RegisterResponse> {
                    override fun onSuccess(result: RegisterResponse) {
                        runOnUiThread {
                            // ── LOGGING FOR PHASE 2 SCREENSHOT ──
                            Log.d("REGISTER_SUCCESS", "Status Code: 201 Created")
                            Log.d("REGISTER_SUCCESS", "Full Name: $fullName")
                            Log.d("REGISTER_SUCCESS", "Email: $email")
                            Log.d("REGISTER_SUCCESS", "Account created successfully!")
                            // ────────────────────────────────────
                            progressRegister.visibility = View.GONE
                            tvSuccess.text = "Account created! Redirecting to login..."
                            tvSuccess.visibility = View.VISIBLE

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                intent.putExtra("registered_email", email)
                                startActivity(intent)
                                finish()
                                overridePendingTransition(
                                    android.R.anim.fade_in,
                                    android.R.anim.fade_out
                                )
                            }, 2000)
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            Log.e("REGISTER_FAIL", "Error: $error")
                            tvError.text = error
                            tvError.visibility = View.VISIBLE
                            btnRegister.isEnabled = true
                            progressRegister.visibility = View.GONE
                        }
                    }
                })
        }

        tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}