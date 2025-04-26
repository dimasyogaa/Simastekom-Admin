package com.yogadimas.simastekom.model.responses


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class LecturerObjectResponse(
    @field:SerializedName("data")
    val data: LecturerData,
)

@Parcelize
data class LecturerData(

    @field:SerializedName("id_pengguna")
    var userId: String? = null,
    @field:SerializedName("tipe_pengguna")
    var userType: String? = null,
    @field:SerializedName("nidn")
    var lecturerIdNumber: String? = null,
    @field:SerializedName("nama_lengkap")
    var fullName: String? = null,
    @field:SerializedName("password")
    var password: String? = null,
    @field:SerializedName("confirm_password")
    var confirmPassword: String? = null,
    @field:SerializedName("gelar")
    var degree: String? = null,

    @field:SerializedName("jenis_kelamin")
    var gender: String? = null,

    @field:SerializedName("is_added")
    var isAdded: Boolean = false,
    @field:SerializedName("is_updated")
    var isUpdated: Boolean = false,
    @field:SerializedName("is_deleted")
    var isDeleted: Boolean = false,
    @field:SerializedName("is_logout")
    val isLogout: Boolean = false,

    ) : Parcelable




