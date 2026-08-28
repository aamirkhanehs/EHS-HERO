package com.ehshero.app.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "ehs_hero_session")

/**
 * Persists the "Remember Me" choice from the login screen (spec section 3)
 * plus a small cache of the signed-in user's role, so MainActivity can
 * decide which nav graph to show before the first Firestore user-doc read
 * completes. The actual authentication state of record is always
 * FirebaseAuth's own session - this is a fast local hint, never a source of
 * truth for access control (see firestore.rules for the real enforcement).
 */
class SessionManager(private val context: Context) {

    private object Keys {
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val CACHED_ROLE = stringPreferencesKey("cached_role")
        val CACHED_UID = stringPreferencesKey("cached_uid")
    }

    val rememberMeFlow: Flow<Boolean> =
        context.sessionDataStore.data.map { it[Keys.REMEMBER_ME] ?: false }

    suspend fun setRememberMe(value: Boolean) {
        context.sessionDataStore.edit { it[Keys.REMEMBER_ME] = value }
    }

    suspend fun cacheSession(uid: String, role: String) {
        context.sessionDataStore.edit {
            it[Keys.CACHED_UID] = uid
            it[Keys.CACHED_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit {
            it.remove(Keys.CACHED_UID)
            it.remove(Keys.CACHED_ROLE)
        }
    }

    suspend fun cachedRole(): String? = context.sessionDataStore.data.first()[Keys.CACHED_ROLE]
}
