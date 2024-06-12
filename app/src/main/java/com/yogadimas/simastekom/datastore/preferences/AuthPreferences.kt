package com.yogadimas.simastekom.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveUser(token: String?, userId: String?, userType: String?) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token ?: DEFAULT_VALUE
            preferences[USER_ID_KEY] = userId.orEmpty()
            preferences[USER_TYPE_KEY] = userType.orEmpty()
        }
    }

    fun getUser(): Flow<Triple<String, String, String>> {
        return dataStore.data.map { preferences ->
            val token = preferences[TOKEN_KEY] ?: DEFAULT_VALUE
            val userId = preferences[USER_ID_KEY].orEmpty()
            val userType = preferences[USER_TYPE_KEY].orEmpty()
            Triple(token, userId, userType)
        }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token_key")
        private val USER_ID_KEY = stringPreferencesKey("user_id_key")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type_key")
        const val DEFAULT_VALUE = "Tidak ada token"

        @Volatile
        private var INSTANCE: AuthPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): AuthPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

}