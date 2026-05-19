package edu.cit.pangilinan.stillness

import edu.cit.pangilinan.stillness.R




import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import edu.cit.pangilinan.stillness.features.dashboard.DashboardActivity
import edu.cit.pangilinan.stillness.features.landing.LandingActivity
import edu.cit.pangilinan.stillness.features.landing.RippleLogoView
import edu.cit.pangilinan.stillness.shared.auth.SessionManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_splash)

        val rippleLogoView = findViewById<RippleLogoView>(R.id.rippleLogoView)
        val tvWordmark = findViewById<TextView>(R.id.tvWordmark)
        val tvTagline = findViewById<TextView>(R.id.tvTagline)
        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)

        rippleLogoView.onBoxComplete = {
            // Trigger wordmark slide and fade
            val animWordmark = AnimationUtils.loadAnimation(this, R.anim.slide_fade_in)
            tvWordmark.startAnimation(animWordmark)
            tvWordmark.alpha = 1f
            tvWordmark.translationY = 0f

            // Trigger tagline with 100ms delay
            val animTagline = AnimationUtils.loadAnimation(this, R.anim.slide_fade_in)
            animTagline.startOffset = 100
            tvTagline.startAnimation(animTagline)
            tvTagline.alpha = 1f
            tvTagline.translationY = 0f
        }

        // Start 3500ms custom animation
        rippleLogoView.startAnimation()

        // Start loading dots at 3100ms
        Handler(Looper.getMainLooper()).postDelayed({
            dot1.alpha = 1f
            dot2.alpha = 1f
            dot3.alpha = 1f
            dot1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dot_pulse_1))
            dot2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dot_pulse_2))
            dot3.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dot_pulse_3))
        }, 3100)

        // Navigate at 3500ms
        Handler(Looper.getMainLooper()).postDelayed({
            if (SessionManager.isLoggedIn(this)) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, LandingActivity::class.java))
            }
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 3500)
    }
}
