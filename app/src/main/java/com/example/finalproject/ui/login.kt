package com.example.finalproject.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.R
import com.example.finalproject.data.repository.AuthRepository
import com.example.finalproject.ui.home.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authRepo = AuthRepository(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvError = findViewById<TextView>(R.id.tvError)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvForgot = findViewById<TextView>(R.id.tvForgotPassword)

        fun showError(msg: String) {
            tvError.text = msg
            tvError.visibility = View.VISIBLE
        }

        fun clearError() {
            tvError.visibility = View.GONE
        }

        // אם כבר מחובר/ת — לדלג ישר ל-Home
        if (authRepo.isLoggedIn()) {
            val isAdmin = authRepo.currentRole() == "ADMIN"
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("isAdmin", isAdmin)
            startActivity(intent)
            finish()
            return
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            clearError()

            if (email.isEmpty()) return@setOnClickListener showError("Please enter email")
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@setOnClickListener showError("Invalid email address")
            if (password.isEmpty()) return@setOnClickListener showError("Please enter password")

            lifecycleScope.launch(Dispatchers.IO) {
                val ok = authRepo.login(email, password)

                withContext(Dispatchers.Main) {
                    if (!ok) {
                        showError("Incorrect password/email")
                        return@withContext
                    }

                    val isAdmin = authRepo.currentRole() == "ADMIN"
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.putExtra("isAdmin", isAdmin)
                    startActivity(intent)
                    finish()
                }
            }
        }

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            clearError()

            if (email.isEmpty()) return@setOnClickListener showError("Please enter email")
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@setOnClickListener showError("Invalid email address")
            if (password.isEmpty()) return@setOnClickListener showError("Please enter password")

            lifecycleScope.launch(Dispatchers.IO) {
                val ok = authRepo.register(email, password, role = "USER")

                withContext(Dispatchers.Main) {
                    if (!ok) {
                        showError("User already exists")
                        return@withContext
                    }

                    Toast.makeText(this@LoginActivity, "Registered & logged in!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.putExtra("isAdmin", false)
                    startActivity(intent)
                    finish()
                }
            }
        }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Forgot Password - coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}
