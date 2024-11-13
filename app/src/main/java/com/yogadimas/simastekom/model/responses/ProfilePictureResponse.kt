package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ProfilePictureObjectResponse(

    @field:SerializedName("data")
    val profilePictureData: ProfilePictureData,

    )

@Parcelize
data class ProfilePictureData(

    @field:SerializedName("id_pengguna")
    var userId: String? = null,

    @field:SerializedName("tipe_pengguna")
    var userType: String? = null,

    @field:SerializedName("foto_profil")
    var profilePicture: String? = null,

    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,

    val isFromAdminStudent: Boolean = false,

    ) : Parcelable