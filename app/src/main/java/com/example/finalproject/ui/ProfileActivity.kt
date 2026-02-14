package com.example.finalproject.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.R
import com.example.finalproject.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: AuthRepository

    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfileRole: TextView

    private lateinit var etNewEmail: EditText
    private lateinit var btnSaveEmail: Button

    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnSavePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = AuthRepository(this)

        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        tvProfileRole = findViewById(R.id.tvProfileRole)

        etNewEmail = findViewById(R.id.etNewEmail)
        btnSaveEmail = findViewById(R.id.btnSaveEmail)

        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnSavePassword = findViewById(R.id.btnSavePassword)

        loadUser()

        btnSaveEmail.setOnClickListener {
            val newEmail = etNewEmail.text.toString().trim()
            if (newEmail.isBlank() || !newEmail.contains("@")) {
                Toast.makeText(this, "אימייל לא תקין", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val ok = auth.changeEmail(newEmail)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        Toast.makeText(this@ProfileActivity, "האימייל עודכן", Toast.LENGTH_SHORT).show()
                        loadUser()
                    } else {
                        Toast.makeText(this@ProfileActivity, "לא ניתן לעדכן אימייל (אולי כבר קיים)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnSavePassword.setOnClickListener {
            val oldP = etOldPassword.text.toString()
            val newP = etNewPassword.text.toString()

            if (oldP.isBlank() || newP.isBlank()) {
                Toast.makeText(this, "נא למלא סיסמה ישנה וחדשה", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newP.length < 6) {
                Toast.makeText(this, "סיסמה חדשה חייבת לפחות 6 תווים", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val ok = auth.changePassword(oldP, newP)
                withContext(Dispatchers.Main) {
                    if (ok) {
                        Toast.makeText(this@ProfileActivity, "הסיסמה עודכנה", Toast.LENGTH_SHORT).show()
                        etOldPassword.text?.clear()
                        etNewPassword.text?.clear()
                    } else {
                        Toast.makeText(this@ProfileActivity, "סיסמה ישנה שגויה / עדכון נכשל", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadUser() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = auth.getCurrentUser()
            withContext(Dispatchers.Main) {
                if (user == null) {
                    Toast.makeText(this@ProfileActivity, "אין משתמש מחובר", Toast.LENGTH_SHORT).show()
                    finish()
                    return@withContext
                }
                tvProfileEmail.text = "Email: ${user.email}"
                tvProfileRole.text = "Role: ${user.role}"
                etNewEmail.setText(user.email)
            }
        }
    }
}
