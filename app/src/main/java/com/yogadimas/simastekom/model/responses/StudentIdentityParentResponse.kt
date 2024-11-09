package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class StudentIdentityParentObjectResponse(

    @field:SerializedName("data")
    val studentIdentityParentData: StudentIdentityParentData

    )

@Parcelize
data class StudentIdentityParentData(

    @field:SerializedName("pengguna_saat_ini")
    val userCurrent: UserCurrent? = null,

    @field:SerializedName("id_pengguna")
    var userId: String? = null,

    @field:SerializedName("tipe_pengguna")
    val userType: String? = null,

    @field:SerializedName("nim")
    var studentIdNumber: String? = null,

    @field:SerializedName("nama_mahasiswa")
    var studentName: String? = null,

    @field:SerializedName("nama_pengguna")
    var username: String? = null,

    @field:SerializedName("nama_ayah")
    var nameFather: String? = null,

    @field:SerializedName("no_ktp_ayah")
    var idCardNumberFather: String? = null,

    @field:SerializedName("nama_ibu")
    var nameMother: String? = null,

    @field:SerializedName("no_ktp_ibu")
    var idCardNumberMother: String? = null,

    @field:SerializedName("pekerjaan")
    var occupation: String? = null,

    @field:SerializedName("telepon")
    var phone: String? = null,

    @field:SerializedName("alamat")
    var address: AddressData? = null,

    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,

    val isFromAdminStudent: Boolean = false,
) : Parcelable