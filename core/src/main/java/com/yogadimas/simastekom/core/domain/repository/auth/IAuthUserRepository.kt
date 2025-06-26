package com.yogadimas.simastekom.core.domain.repository.auth

import com.yogadimas.simastekom.core.data.source.local.auth.AuthUserEntity
import kotlinx.coroutines.flow.Flow

interface IAuthUserRepository {
    fun getUser(): Flow<AuthUserEntity>
    suspend fun setSaveUser(authUser: AuthUserEntity)
    suspend fun clearUser(): Flow<AuthUserEntity>
}