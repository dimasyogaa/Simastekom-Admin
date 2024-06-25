package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class IdentityAcademicListResponse(

    @field:SerializedName("data")
    val identityAcademicData: List<IdentityAcademicData>,
)

data class IdentityAcademicObjectResponse(

    @field:SerializedName("data")
    val identityAcademicData: IdentityAcademicData,
)

@Parcelize
data class IdentityAcademicData(

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("kode")
    var code: String? = null,

    @field:SerializedName("nama")
    var name: String? = null,


    @field:SerializedName("kode_fakultas")
    var facultyId: Int? = null,

    @field:SerializedName("text_kode_fakultas")
    var facultyCode: String? = null,

    @field:SerializedName("nama_fakultas")
    var facultyName: String? = null,


    @field:SerializedName("kode_jenjang")
    var levelId: Int? = null,

    @field:SerializedName("text_kode_jenjang")
    var levelCode: String? = null,

    @field:SerializedName("nama_jenjang")
    var levelName: String? = null,


    @field:SerializedName("kode_jurusan")
    var majorId: Int? = null,

    @field:SerializedName("text_kode_jurusan")
    var majorCode: String? = null,

    @field:SerializedName("nama_jurusan")
    var majorName: String? = null,


    @field:SerializedName("kode_gelar")
    var degreeId: Int? = null,

    @field:SerializedName("text_kode_gelar")
    var degreeCode: String? = null,

    @field:SerializedName("nama_gelar")
    var degreeName: String? = null,

    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,

    ): Parcelable
