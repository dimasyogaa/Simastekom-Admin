package com.yogadimas.simastekom.core.data.source.local.auth


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class AuthPreferences(private val dataStore: DataStore<Preferences>) {

    @Volatile
    private var cachedToken: String? = null

    suspend fun saveUser(authUserEntity: AuthUserEntity) {
        Log.e("TAG", "AuthPreferences saveUser: $authUserEntity", )
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = authUserEntity.token.orEmpty()
            preferences[USER_ID_KEY] = authUserEntity.userId.orEmpty()
            preferences[USER_TYPE_KEY] = authUserEntity.userType.orEmpty()
        }
    }

    fun getUser(): Flow<AuthUserEntity> {
        return dataStore.data.map { preferences ->
            val token = preferences[TOKEN_KEY]
            val userId = preferences[USER_ID_KEY]
            val userType = preferences[USER_TYPE_KEY]
            Log.e("TAG", "AuthPreferences getUser: 1 $cachedToken", )
            if (cachedToken == null && !token.isNullOrEmpty()) {

                cachedToken = token
                Log.e("TAG", "AuthPreferences getUser: 2 $cachedToken", )
            }

            AuthUserEntity(token, userId, userType)
        }
    }

    fun getCachedTokenOnly(): String? = cachedToken

    suspend fun clearUser(): Flow<AuthUserEntity> {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_TYPE_KEY)
        }
        cachedToken = null
        return getUser()
    }


    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token_key")
        private val USER_ID_KEY = stringPreferencesKey("user_id_key")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type_key")
    }
}

