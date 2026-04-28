package com.budgetbuddy.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.budgetbuddy.app.db.SessionManager

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If user is already logged in, skip straight to Dashboard
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_welcome)

        findViewById<android.widget.Button>(R.id.btn_get_started).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btn_log_in).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<android.widget.TextView>(R.id.tv_privacy_link).setOnClickListener {
            // Open privacy policy if needed
        }
    }
}
