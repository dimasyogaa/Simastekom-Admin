package com.yogadimas.simastekom.model.responses

import com.google.gson.annotations.SerializedName

data class IdentityPersonalResponse(

    @field:SerializedName("data")
    val identityPersonalData: IdentityPersonalData,

    )

data class IdentityPersonalData(

    @field:SerializedName("id_pengguna")
    val userId: String? = null,

    @field:SerializedName("tipe_pengguna")
    val userType: String? = null,

    @field:SerializedName("no_ktp")
    val idCardNumber: String? = null,

    @field:SerializedName("jenis_kelamin")
    val gender: String? = null,

    @field:SerializedName("agama")
    val religion: String? = null,

    @field:SerializedName("telepon")
    val phone: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("tempat_tanggal_lahir")
    val placeDateBirth: String? = null,

    @field:SerializedName("alamat")
    val address: String? = null,

    @field:SerializedName("is_valid_email")
    val isValidEmail: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,
)
