package edu.cit.pangilinan.stillness

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.auth.SessionManager
import edu.cit.pangilinan.stillness.model.LoginResponse

class LoginActivity : Activity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com") // Replace with actual Web Client ID
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnGoogleLogin = findViewById<LinearLayout>(R.id.btn_google_login)
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
                        if (!token.isNullOrEmpty()) {
                            // ── LOGGING FOR PHASE 2 SCREENSHOT ──
                            Log.d("LOGIN_SUCCESS", "Status Code: 200 OK")
                            Log.d("LOGIN_SUCCESS", "Token: $token")
                            Log.d("LOGIN_SUCCESS", "User Email: $email")
                            // ────────────────────────────────────
                            SessionManager.saveToken(this@LoginActivity, token)
                            SessionManager.saveEmail(this@LoginActivity, email)
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            Log.e("LOGIN_FAIL", "Login failed: ${result.error?.message}")
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

        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    handleGoogleSignIn(idToken)
                } else {
                    Log.e("GOOGLE_SIGN_IN", "ID Token is null")
                    Toast.makeText(this, "Google Sign-In failed: Token error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e("GOOGLE_SIGN_IN", "Sign-In failed: ${e.statusCode}")
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignIn(idToken: String) {
        findViewById<ProgressBar>(R.id.progress_login).visibility = View.VISIBLE
        ApiClient.googleLogin(idToken, object : ApiClient.ApiCallback<LoginResponse> {
            override fun onSuccess(result: LoginResponse) {
                runOnUiThread {
                    val token = result.data?.token
                    if (!token.isNullOrEmpty()) {
                        // ── LOGGING FOR PHASE 2 SCREENSHOT ──
                        Log.d("LOGIN_SUCCESS", "Google Login Status Code: 200 OK")
                        Log.d("LOGIN_SUCCESS", "Google Token: $token")
                        Log.d("LOGIN_SUCCESS", "Google User Email: ${result.data.user.email}")
                        // ────────────────────────────────────
                        SessionManager.saveToken(this@LoginActivity, token)
                        SessionManager.saveEmail(this@LoginActivity, result.data.user.email)
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        Log.e("LOGIN_FAIL", "Google login failed: empty token")
                        Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                        findViewById<ProgressBar>(R.id.progress_login).visibility = View.GONE
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("LOGIN_FAIL", "Google Login Error: $error")
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_SHORT).show()
                    findViewById<ProgressBar>(R.id.progress_login).visibility = View.GONE
                }
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAffinity()
    }
}