package com.yogadimas.simastekom.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    suspend fun saveSetting(
        toggleIdentityPersonal: Boolean,
        togglePlaceDateBirth: Boolean,
        toggleAddressHome: Boolean,
    ) {
        dataStore.edit { preferences ->
            preferences[TOGGLE_IDENTITY_PERSONAL_KEY] = toggleIdentityPersonal
            preferences[TOGGLE_PLACE_DATE_BIRTH_KEY] = togglePlaceDateBirth
            preferences[TOGGLE_ADDRESS_HOME_KEY] = toggleAddressHome
        }
    }

    fun getSetting(): Flow<Triple<Boolean, Boolean, Boolean>> {
        return dataStore.data.map { preferences ->
            val toggleIdentityPersonal = preferences[TOGGLE_IDENTITY_PERSONAL_KEY] ?: DEFAULT_VALUE
            val togglePlaceDateBirth =
                preferences[TOGGLE_PLACE_DATE_BIRTH_KEY] ?: DEFAULT_VALUE
            val toggleAddressHome =
                preferences[TOGGLE_ADDRESS_HOME_KEY] ?: DEFAULT_VALUE
            Triple(toggleIdentityPersonal, togglePlaceDateBirth, toggleAddressHome)
        }
    }

    companion object {
        private val TOGGLE_IDENTITY_PERSONAL_KEY =
            booleanPreferencesKey("toggle_identity_personal_key")
        private val TOGGLE_PLACE_DATE_BIRTH_KEY =
            booleanPreferencesKey("toggle_identity_personal_place_date_birth_key")
        private val TOGGLE_ADDRESS_HOME_KEY =
            booleanPreferencesKey("toggle_identity_personal_address_home-key")

        const val DEFAULT_VALUE = false

        @Volatile
        private var INSTANCE: SettingPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): SettingPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }

}