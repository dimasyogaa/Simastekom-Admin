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

    @field:SerializedName("nim")
    var studentIdNumber: String? = null,


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


    @field:SerializedName("id_program_studi")
    var studyProgramId: Int? = null,

    @field:SerializedName("text_kode_program_studi")
    var studyProgramCode: String? = null,

    @field:SerializedName("nama_program_studi")
    var studyProgramName: String? = null,


    @field:SerializedName("angkatan")
    var batch: String? = null,


    @field:SerializedName("id_sesi_kelas")
    var classSessionId: Int? = null,

    @field:SerializedName("nama_sesi_kelas")
    var classSessionName: String? = null,


    @field:SerializedName("id_semester")
    var semesterId: Int? = null,

    @field:SerializedName("nomor_semester")
    var numberSemester: String? = null,


    @field:SerializedName("id_metode_kuliah")
    var lectureMethodId: Int? = null,

    @field:SerializedName("nama_metode_kuliah")
    var lectureMethodName: String? = null,


    @field:SerializedName("id_kampus")
    var campusId: Int? = null,

    @field:SerializedName("text_code_kampus")
    var campusCode: String? = null,

    @field:SerializedName("nama_kampus")
    var campusName: String? = null,


    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,

    ) : Parcelable
