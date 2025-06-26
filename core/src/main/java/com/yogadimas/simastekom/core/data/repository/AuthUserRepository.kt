package com.yogadimas.simastekom.core.data.repository

import com.yogadimas.simastekom.core.data.source.local.AuthLocalDataSource
import com.yogadimas.simastekom.core.data.source.local.auth.AuthUserEntity
import com.yogadimas.simastekom.core.domain.repository.auth.IAuthUserRepository
import kotlinx.coroutines.flow.Flow

class AuthUserRepository(
    private val authLocalDataSource: AuthLocalDataSource,
) : IAuthUserRepository {


    override fun getUser(): Flow<AuthUserEntity> =
        authLocalDataSource.getUser()

    override suspend fun setSaveUser(authUser: AuthUserEntity) {
        val authUserEntity = authUser
        authLocalDataSource.setSaveUser(authUserEntity)
    }

    override suspend fun clearUser(): Flow<AuthUserEntity> = authLocalDataSource.clearUser()


}