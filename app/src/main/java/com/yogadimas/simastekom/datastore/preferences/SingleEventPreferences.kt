package com.yogadimas.simastekom.datastore.preferences

import android.provider.Contacts.SettingsColumns.KEY
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SingleEventPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun setServerError(serverError: Boolean) {
        dataStore.edit { preferences ->
            preferences[SERVER_ERROR_KEY] = serverError
        }
    }

    fun getServerError(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            val serverError = preferences[SERVER_ERROR_KEY]  ?: DEFAULT_VALUE
            serverError
        }
    }

    companion object {
        private val SERVER_ERROR_KEY = booleanPreferencesKey("a")
        const val DEFAULT_VALUE = false

        @Volatile
        private var INSTANCE: SingleEventPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): SingleEventPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SingleEventPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

}