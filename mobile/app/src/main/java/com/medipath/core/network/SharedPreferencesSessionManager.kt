package com.medipath.core.network

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesSessionManager(context: Context) {

    private object PreferencesKeys {
        const val PREFS_NAME = "session_prefs"
        const val SESSION_ID = "session_id"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSessionId(sessionId: String) {
        prefs.edit().putString(PreferencesKeys.SESSION_ID, sessionId).apply()
    }

    fun getSessionId(): String? {
        return prefs.getString(PreferencesKeys.SESSION_ID, null)
    }

    fun deleteSessionId() {
        prefs.edit().remove(PreferencesKeys.SESSION_ID).apply()
    }

    fun isLoggedIn(): Boolean {
        return getSessionId() != null
    }
}