package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class Errors(
    @SerializedName("errors")
    val errors: ErrorDetail? = null,
)

@Parcelize
data class ErrorDetail(
    @SerializedName("message")
    val message: List<String>? = null,

    @SerializedName("nama_pengguna")
    val username: List<String>? = null,

    @SerializedName("password")
    val password: List<String>? = null,

    @SerializedName("nama")
    val name: List<String>? = null,

    @SerializedName("token")
    val token: List<String>? = null,

    @SerializedName("jenis_kelamin")
    val gender: List<String>? = null,
): Parcelable