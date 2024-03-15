package com.yogadimas.simastekom.model

import com.google.gson.annotations.SerializedName


data class Errors(
    @SerializedName("errors")
    val errors: ErrorDetails? = null
)

data class ErrorDetails(
    @SerializedName("message")
    val message: List<String>? = null,

    @SerializedName("nama_pengguna")
    val namaPengguna: List<String>? = null,

    @SerializedName("password")
    val password: List<String>? = null
)