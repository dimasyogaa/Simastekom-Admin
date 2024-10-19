package com.yogadimas.simastekom.model.responses



import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class StudentListResponse(
    @field:SerializedName("data")
    val studentData: List<StudentData>,
)

data class StudentObjectResponse(
    @field:SerializedName("data")
    val studentData: StudentData,
)

@Parcelize
data class StudentData(


//    @field:SerializedName("id")
//    val id: Int? = null,

    @field:SerializedName("id_pengguna")
    var id: String? = null,
    @field:SerializedName("tipe_pengguna")
    var userType: String? = null,
    @field:SerializedName("nim")
    var studentIdNumber: String? = null,
    @field:SerializedName("nama_lengkap")
    var fullName: String? = null,
    @field:SerializedName("password")
    var password: String? = null,
    @field:SerializedName("confirm_password")
    var confirmPassword: String? = null,

    @field:SerializedName("jenis_kelamin")
    var gender: String? = null,

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

    @field:SerializedName("id_status_mahasiswa")
    var studentStatusId: Int? = null,
    @field:SerializedName("nama_status_mahasiswa")
    var studentStatusName: String? = null,

    @field:SerializedName("id_status_pekerjaan")
    var employmentStatusId: Int? = null,
    @field:SerializedName("nama_status_pekerjaan")
    var employmentStatusName: String? = null,

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
    @field:SerializedName("is_logout")
    val isLogout: Boolean = false,
    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,

    ): Parcelable




