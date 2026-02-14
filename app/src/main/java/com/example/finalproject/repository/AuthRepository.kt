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
        //  להתחבר ישר אחרי הרשמה
        session.saveSession(id.toInt(), role)
        return true
    }

    fun logout() {
        session.clearSession()
    }

    suspend fun getCurrentUser(): UserEntity? {
        val id = session.getUserId() ?: return null
        return userDao.getById(id)
    }

    suspend fun changeEmail(newEmail: String): Boolean {
        val id = session.getUserId() ?: return false

        // בדיקה בסיסית: שלא קיים כבר משתמש עם אותו אימייל
        val existing = userDao.getByEmail(newEmail)
        if (existing != null && existing.id != id) return false

        val rows = userDao.updateEmail(id, newEmail)
        return rows > 0
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        val id = session.getUserId() ?: return false
        val user = userDao.getById(id) ?: return false

        if (user.passwordHash != hashPassword(oldPassword)) return false

        val rows = userDao.updatePassword(id, hashPassword(newPassword))
        return rows > 0
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
