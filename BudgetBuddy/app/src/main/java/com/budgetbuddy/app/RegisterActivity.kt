package com.budgetbuddy.app

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgetbuddy.app.db.BudgetRepository
import com.budgetbuddy.app.db.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var repo: BudgetRepository
    private var passwordVisible = false
    private var confirmVisible  = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        repo = BudgetRepository(this)

        findViewById<TextView>(R.id.tv_bar_title).text = "Registration"
        findViewById<ImageView>(R.id.iv_back).setOnClickListener { finish() }

        // Password toggles
        val etPassword = findViewById<android.widget.EditText>(R.id.et_password)
        val etConfirm  = findViewById<android.widget.EditText>(R.id.et_confirm_password)

        findViewById<ImageView>(R.id.iv_toggle_password).setOnClickListener {
            passwordVisible = !passwordVisible
            etPassword.transformationMethod =
                if (passwordVisible) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            etPassword.setSelection(etPassword.text.length)
        }
        findViewById<ImageView>(R.id.iv_toggle_confirm).setOnClickListener {
            confirmVisible = !confirmVisible
            etConfirm.transformationMethod =
                if (confirmVisible) HideReturnsTransformationMethod.getInstance()
                else PasswordTransformationMethod.getInstance()
            etConfirm.setSelection(etConfirm.text.length)
        }

        // Register button
        findViewById<Button>(R.id.btn_register).setOnClickListener {
            val username = findViewById<android.widget.EditText>(R.id.et_username).text.toString().trim()
            val email    = findViewById<android.widget.EditText>(R.id.et_email).text.toString().trim()
            val password = etPassword.text.toString()
            val confirm  = etConfirm.text.toString()

            if (username.isEmpty()) { showError("Username is required"); return@setOnClickListener }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Enter a valid email"); return@setOnClickListener
            }
            if (password.length < 6) { showError("Password must be 6+ characters"); return@setOnClickListener }
            if (password != confirm)  { showError("Passwords do not match"); return@setOnClickListener }

            lifecycleScope.launch {
                // Check username not taken
                if (repo.userDao.getUserByUsername(username) != null) {
                    runOnUiThread { showError("Username already taken") }
                    return@launch
                }
                val userId = repo.registerUser(username, email, password)
                SessionManager.saveUserId(this@RegisterActivity, userId.toInt())
                runOnUiThread {
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                }
            }
        }

        // Already have account
        findViewById<TextView>(R.id.tv_go_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)); finish()
        }
    }

    private fun showError(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
