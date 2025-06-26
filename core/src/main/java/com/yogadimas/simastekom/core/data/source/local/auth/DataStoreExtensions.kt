package com.yogadimas.simastekom.core.data.source.local.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.security.crypto.MasterKeys
import io.github.osipxd.security.crypto.encryptedPreferencesDataStore

val Context.authDataStore: DataStore<Preferences> by encryptedPreferencesDataStore(
    name = "auth",
    masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
)

