package edu.cit.pangilinan.stillness

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import edu.cit.pangilinan.stillness.api.ApiClient
import edu.cit.pangilinan.stillness.auth.SessionManager
import edu.cit.pangilinan.stillness.model.User
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Profile fields
        val tvAvatarInitials = findViewById<TextView>(R.id.tv_avatar_initials)
        val tvFullNameSummary = findViewById<TextView>(R.id.tv_full_name_summary)
        val tvRoleSummary = findViewById<TextView>(R.id.tv_role_summary)
        val tvFullName = findViewById<TextView>(R.id.tv_full_name)
        val tvEmail = findViewById<TextView>(R.id.tv_email)
        val tvMemberSince = findViewById<TextView>(R.id.tv_member_since)
        val progressDashboard = findViewById<ProgressBar>(R.id.progress_dashboard)

        // Navbar: Welcome text + Logout
        val navWelcome = findViewById<TextView>(R.id.nav_welcome)
        val navLogout = findViewById<TextView>(R.id.nav_logout)

        navLogout.setOnClickListener { showLogoutDialog() }
        
        findViewById<Button>(R.id.btnNavSessions).setOnClickListener {
            startActivity(Intent(this, SessionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnNavBookings).setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        // Load profile data
        loadProfile(
            tvAvatarInitials,
            tvFullNameSummary,
            tvRoleSummary,
            tvFullName,
            tvEmail,
            tvMemberSince,
            navWelcome,
            progressDashboard
        )
    }

    private fun loadProfile(
        tvInitials: TextView,
        tvFullNameSummary: TextView,
        tvRoleSummary: TextView,
        tvFullName: TextView,
        tvEmail: TextView,
        tvMemberSince: TextView,
        navWelcome: TextView,
        progress: ProgressBar
    ) {
        val token = SessionManager.getToken(this)
        if (token == null) {
            goToLogin()
            return
        }

        progress.visibility = View.VISIBLE

        ApiClient.getProfile(token, object : ApiClient.ApiCallback<User> {
            override fun onSuccess(result: User) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    
                    // Populate main details
                    tvFullName.text = result.fullName
                    tvEmail.text = result.email
                    tvMemberSince.text = formatDate(result.createdAt)
                    
                    // Populate summary/avatar section
                    tvFullNameSummary.text = result.fullName
                    tvRoleSummary.text = result.role.replace("ROLE_", "").lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                    navWelcome.text = "Welcome, ${result.fullName.split(" ").firstOrNull() ?: "User"}"
                    
                    // Generate initials for avatar
                    tvInitials.text = result.fullName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .joinToString("")
                        .take(2)
                        .uppercase()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    // If unauthorized, clear session and go to login
                    if (error.contains("401") || error.contains("403")) {
                        SessionManager.clearSession(this@DashboardActivity)
                        goToLogin()
                    }
                }
            }
        })
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "—"
        return try {
            // Assuming ISO 8601 format from backend: "2023-10-27T10:00:00Z"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr // Return raw string if parsing fails
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)

        val btnLogout = dialog.findViewById<Button>(R.id.btn_logout_confirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_logout_cancel)

        btnLogout.setOnClickListener {
            SessionManager.clearSession(this)
            goToLogin()
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
