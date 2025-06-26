package com.yogadimas.simastekom.core.data.source.local

import com.yogadimas.simastekom.core.data.source.local.auth.AuthPreferences
import com.yogadimas.simastekom.core.data.source.local.auth.AuthUserEntity
import kotlinx.coroutines.flow.Flow

class AuthLocalDataSource(private val authPreferences: AuthPreferences) {

    fun getUser(): Flow<AuthUserEntity> = authPreferences.getUser()

    suspend fun setSaveUser(authUser: AuthUserEntity) = authPreferences.saveUser(authUser)

    suspend fun clearUser(): Flow<AuthUserEntity> = authPreferences.clearUser()

}