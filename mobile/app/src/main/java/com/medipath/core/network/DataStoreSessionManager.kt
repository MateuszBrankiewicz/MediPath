package com.medipath.core.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class DataStoreSessionManager(private val context: Context) {

    private object PreferencesKeys {
        val SESSION_ID = stringPreferencesKey("session_id")
    }

    suspend fun saveSessionId(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SESSION_ID] = sessionId
        }
    }

    suspend fun getSessionId(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SESSION_ID]
        }.first()
    }

    suspend fun deleteSessionId() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SESSION_ID)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return getSessionId() != null
    }
}