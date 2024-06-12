package com.yogadimas.simastekom.model.responses

import com.google.gson.annotations.SerializedName

data class AdminResponse(

    @field:SerializedName("data")
    val adminData: AdminData,

    )


data class AdminData(

    @field:SerializedName("id_pengguna")
    val userId: String,

    @field:SerializedName("tipe_pengguna")
    val userType: String,

    @field:SerializedName("nama_pengguna")
    val username: String,

    @field:SerializedName("password")
    val password: String?,

    @field:SerializedName("nama")
    val name: String,

    @field:SerializedName("foto_profil")
    val profilePicture: String?,

    @field:SerializedName("token")
    val token: String,

    @field:SerializedName("no_ktp")
    val idCardNumber: String?,

    @field:SerializedName("jenis_kelamin")
    val gender: String?,

    @field:SerializedName("alamat")
    val address: String?,

    @field:SerializedName("tempat_tanggal_lahir")
    val placeDateBirth: String?,

    @field:SerializedName("agama")
    val religion: String?,

    @field:SerializedName("telepon")
    val phone: String?,

    @field:SerializedName("email")
    val email: String?,

    @field:SerializedName("logout")
    val logout: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false


    )


