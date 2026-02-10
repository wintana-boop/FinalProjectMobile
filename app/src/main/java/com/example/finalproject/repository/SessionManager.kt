package com.example.finalproject.data.repository

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun saveSession(userId: Int, role: String) {
        prefs.edit()
            .putInt("user_id", userId)
            .putString("role", role)
            .apply()
    }

    fun getUserId(): Int? {
        val id = prefs.getInt("user_id", -1)
        return if (id == -1) null else id
    }

    fun getRole(): String? = prefs.getString("role", null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
