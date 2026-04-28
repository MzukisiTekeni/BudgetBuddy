package com.budgetbuddy.app

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgetbuddy.app.db.BudgetRepository
import com.budgetbuddy.app.db.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var repo: BudgetRepository
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        repo = BudgetRepository(this)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)

        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // Toggle password visibility
        findViewById<ImageView>(R.id.iv_toggle_password).setOnClickListener {
            passwordVisible = !passwordVisible
            etPassword.transformationMethod =
                if (passwordVisible) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.text.length)
        }

        // Forgot password
        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Log In
        findViewById<Button>(R.id.btn_log_in).setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()
            if (username.isEmpty()) { toast("Enter your username"); return@setOnClickListener }
            if (password.isEmpty()) { toast("Enter your password"); return@setOnClickListener }

            lifecycleScope.launch {
                val user = repo.loginUser(username, password)
                runOnUiThread {
                    if (user == null) {
                        toast("Invalid username or password")
                    } else {
                        SessionManager.saveUserId(this@LoginActivity, user.id)
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }
        }

        // Go to Register
        findViewById<TextView>(R.id.tv_go_register).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java)); finish()
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
