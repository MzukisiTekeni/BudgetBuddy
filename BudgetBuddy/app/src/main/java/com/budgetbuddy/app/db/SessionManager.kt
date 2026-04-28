package com.budgetbuddy.app.db

import android.content.Context

/**
 * SessionManager
 * ──────────────
 * Stores the logged-in user's ID in SharedPreferences so the app remembers
 * who is logged in across restarts. No sensitive data stored here — only the
 * numeric user ID from the Room database.
 */
object SessionManager {

    private const val PREFS_NAME  = "budgetbuddy_session"
    private const val KEY_USER_ID = "user_id"
    private const val NO_USER     = -1

    fun saveUserId(context: Context, userId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, NO_USER)

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != NO_USER

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_USER_ID)
            .apply()
    }
}
