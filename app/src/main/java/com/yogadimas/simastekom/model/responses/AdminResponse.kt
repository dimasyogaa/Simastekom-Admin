package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class AdminObjectResponse(

    @field:SerializedName("data")
    val adminData: AdminData,

    )


@Parcelize
data class AdminData(

    @field:SerializedName("id_pengguna")
    var userId: String? = null,

    @field:SerializedName("tipe_pengguna")
    var userType: String? = null,

    @field:SerializedName("nama_pengguna")
    var username: String? = null,

    @field:SerializedName("password")
    var password: String? = null,

    @field:SerializedName("confirm_password")
    var confirmPassword: String? = null,

    @field:SerializedName("nama")
    var name: String? = null,

    @field:SerializedName("foto_profil")
    val profilePicture: String? = null,

    @field:SerializedName("token")
    val token: String? = null,

    @field:SerializedName("no_ktp")
    val idCardNumber: String? = null,

    @field:SerializedName("jenis_kelamin")
    var gender: String? = null,

    @field:SerializedName("alamat")
    val address: String? = null,

    @field:SerializedName("tempat_tanggal_lahir")
    val placeDateBirth: String? = null,

    @field:SerializedName("agama")
    val religion: String? = null,

    @field:SerializedName("telepon")
    val phone: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("logout")
    val isLogout: Boolean = false,

    @field:SerializedName("is_added")
    var isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    var isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    var isDeleted: Boolean = false,


    ): Parcelable


