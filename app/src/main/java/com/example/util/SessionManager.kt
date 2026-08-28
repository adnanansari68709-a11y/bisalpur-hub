package com.example.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException

val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {
    companion object {
        val USER_ID = longPreferencesKey("user_id")
        @Volatile
        var cachedUserId: Long? = null
        private val _sessionFlow = MutableStateFlow<Long?>(null)
    }

    val currentUserId: Flow<Long?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val id = preferences[USER_ID]
            val resolved = if (id == -1L || id == null) null else id
            cachedUserId = resolved
            _sessionFlow.value = resolved
            resolved
        }
        .onStart {
            val cached = cachedUserId ?: _sessionFlow.value
            emit(cached)
        }
        .distinctUntilChanged()

    suspend fun getUserId(): Long? {
        val cached = cachedUserId ?: _sessionFlow.value
        if (cached != null) return cached
        return try {
            val preferences = context.dataStore.data
                .catch { exception ->
                    if (exception is IOException) emit(emptyPreferences()) else throw exception
                }
                .firstOrNull()
            val id = preferences?.get(USER_ID)
            val resolved = if (id == -1L || id == null) null else id
            if (resolved != null) {
                cachedUserId = resolved
                _sessionFlow.value = resolved
            }
            resolved
        } catch (e: Exception) {
            cachedUserId ?: _sessionFlow.value
        }
    }

    suspend fun saveUserId(userId: Long) {
        cachedUserId = userId
        _sessionFlow.value = userId
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        cachedUserId = null
        _sessionFlow.value = null
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID)
        }
    }
}


