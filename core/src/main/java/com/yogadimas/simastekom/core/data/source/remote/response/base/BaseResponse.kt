package com.yogadimas.simastekom.core.data.source.remote.response.base

import com.google.gson.annotations.SerializedName

data class BaseResponse< T>(
    @field:SerializedName("code")
    val code: Int? = null,

    @field:SerializedName("messageCode")
    val messageCode: String? = null,

    @field:SerializedName("data")
    val data: T? = null,
)

