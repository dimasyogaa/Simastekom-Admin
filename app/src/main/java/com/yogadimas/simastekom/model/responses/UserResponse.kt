package com.yogadimas.simastekom.model.responses

import com.google.gson.annotations.SerializedName

data class UserResponse(

    @field:SerializedName("data")
    val userData: UserData,

    )


data class UserData(

    @field:SerializedName("pengguna_id")
    val userId: String,

    @field:SerializedName("pengguna_type")
    val userType: String,

    @field:SerializedName("password")
    val password: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("logout")
    val logout: Boolean = false,

    @field:SerializedName("is_valid_email")
    val isValidEmail: Boolean = false,

    @field:SerializedName("is_valid_token")
    val isValidToken: Boolean = false,


    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,


    )