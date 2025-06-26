package com.yogadimas.simastekom.core.ui.model.auth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthUserUiModel(
    val token: String? = null,
    val userId: String? = null,
    val userType: String? = null,
) : Parcelable