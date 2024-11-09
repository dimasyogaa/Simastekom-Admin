package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class IdentityPersonalObjectResponse(

    @field:SerializedName("data")
    val identityPersonalData: IdentityPersonalData,

    )

@Parcelize
data class IdentityPersonalData(

    @field:SerializedName("pengguna_saat_ini")
    val userCurrent: UserCurrent? = null,

    @field:SerializedName("id_pengguna")
    val userId: String? = null,

    @field:SerializedName("tipe_pengguna")
    val userType: String? = null,

    @field:SerializedName("nim")
    var studentIdNumber: String? = null,

    @field:SerializedName("nidn")
    var lectureIdNumber: String? = null,

    @field:SerializedName("nama_pengguna")
    var username: String? = null,

    @field:SerializedName("nama")
    var name: String? = null,

    @field:SerializedName("no_ktp")
    val idCardNumber: String? = null,

    @field:SerializedName("jenis_kelamin")
    var gender: String? = null,

    @field:SerializedName("agama")
    val religion: String? = null,

    @field:SerializedName("telepon")
    val phone: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("tempat_tanggal_lahir")
    val placeDateBirth: String? = null,

    @field:SerializedName("alamat")
    val address: AddressData? = null,

    @field:SerializedName("is_valid_email")
    val isValidEmail: Boolean = false,

    @field:SerializedName("foto_profil")
    val profilePicture: String? = null,

    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,

    val isFromAdminStudent: Boolean = false,

): Parcelable
