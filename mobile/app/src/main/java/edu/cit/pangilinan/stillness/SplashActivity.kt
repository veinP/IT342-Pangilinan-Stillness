package edu.cit.pangilinan.stillness

import edu.cit.pangilinan.stillness.R

import edu.cit.pangilinan.stillness.features.dashboard.DashboardActivity
import edu.cit.pangilinan.stillness.features.landing.LandingActivity
import edu.cit.pangilinan.stillness.shared.auth.SessionManager


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (SessionManager.isLoggedIn(this)) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, LandingActivity::class.java))
            }
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 1500)
    }
}
