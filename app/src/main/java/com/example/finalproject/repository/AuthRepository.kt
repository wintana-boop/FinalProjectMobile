package com.example.finalproject.data.repository

import android.content.Context
import com.example.finalproject.data.AppDatabase
import com.example.finalproject.data.model.UserEntity
import java.security.MessageDigest

class AuthRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val session = SessionManager(context)

    // ---- Public API ----

    suspend fun login(email: String, password: String): Boolean {
        val user = userDao.getByEmail(email) ?: return false
        val hash = hashPassword(password)
        val ok = user.passwordHash == hash
        if (ok) {
            session.saveSession(user.id, user.role)
        }
        return ok
    }

    suspend fun register(email: String, password: String, role: String = "USER"): Boolean {
        val existing = userDao.getByEmail(email)
        if (existing != null) return false

        val newUser = UserEntity(
            email = email,
            passwordHash = hashPassword(password),
            role = role
        )
        val id = userDao.insert(newUser)
        // אם נרצה: להתחבר ישר אחרי הרשמה
        session.saveSession(id.toInt(), role)
        return true
    }

    fun logout() {
        session.clearSession()
    }

    fun isLoggedIn(): Boolean = session.getUserId() != null

    fun currentRole(): String? = session.getRole()

    // ---- Hash helper ----
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
