package com.yogadimas.simastekom.core.utils.mapper.auth

import com.yogadimas.simastekom.core.data.source.local.auth.AuthUserEntity
import com.yogadimas.simastekom.core.ui.model.auth.AuthUserUiModel

fun AuthUserEntity.toUiModel(): AuthUserUiModel =
    AuthUserUiModel(token = token, userId = userId, userType = userType)

fun AuthUserUiModel.toEntity(): AuthUserEntity =
    AuthUserEntity(token = token, userId = userId, userType = userType)