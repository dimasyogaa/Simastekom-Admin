package com.yogadimas.simastekom.common.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object ObjectDataStore {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "datastore")
}