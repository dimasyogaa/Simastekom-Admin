package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserCurrent(

    @field:SerializedName("tipe_pengguna")
    val userType: String? = null,

    @field:SerializedName("nama")
    val name: String? = null,

    @field:SerializedName("identitas")
    val identity: String? = null,

): Parcelable

