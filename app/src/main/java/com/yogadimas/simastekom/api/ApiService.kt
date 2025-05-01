package com.yogadimas.simastekom.api

import com.yogadimas.simastekom.model.responses.AddressData
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.model.responses.AdminObjectResponse
import com.yogadimas.simastekom.model.responses.CampusData
import com.yogadimas.simastekom.model.responses.CampusListResponse
import com.yogadimas.simastekom.model.responses.CampusObjectResponse
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.model.responses.IdentityAcademicListResponse
import com.yogadimas.simastekom.model.responses.IdentityAcademicObjectResponse
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.IdentityPersonalObjectResponse
import com.yogadimas.simastekom.model.responses.LecturerData
import com.yogadimas.simastekom.model.responses.LecturerObjectResponse
import com.yogadimas.simastekom.model.responses.NameData
import com.yogadimas.simastekom.model.responses.NameListResponse
import com.yogadimas.simastekom.model.responses.NameObjectResponse
import com.yogadimas.simastekom.model.responses.PaginationResponse
import com.yogadimas.simastekom.model.responses.ProfilePictureObjectResponse
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.model.responses.StudentIdentityParentData
import com.yogadimas.simastekom.model.responses.StudentIdentityParentObjectResponse
import com.yogadimas.simastekom.model.responses.StudentObjectResponse
import com.yogadimas.simastekom.model.responses.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @FormUrlEncoded
    @POST("admin/login")
    fun login(
        @Field("nama_pengguna") namaPengguna: String,
        @Field("password") password: String,
    ): Call<AdminObjectResponse>

    @DELETE("admin/logout")
    fun logout(@Header("Authorization") token: String): Call<AdminObjectResponse>

    @GET("admin/current")
    fun getAdminCurrent(@Header("Authorization") token: String): Call<AdminObjectResponse>

    @GET("admin/current/password")
    fun getAdminPassword(@Header("Authorization") token: String): Call<AdminObjectResponse>

    @Multipart
    @POST("admin/current?_method=PATCH")
    fun updateAdminCurrent(
        @Header("Authorization") token: String,
        @Part profilePicture: MultipartBody.Part?,
        @Part("nama_pengguna") username: RequestBody,
        @Part("nama") name: RequestBody,
        @Part("hapus_foto") deletePhoto: RequestBody,
    ): Call<AdminObjectResponse>

    @FormUrlEncoded
    @POST("admin/current/password?_method=PATCH")
    fun updateAdminCurrentPassword(
        @Header("Authorization") token: String,
        @Field("password") password: String,
    ): Call<AdminObjectResponse>


    /** Admin */
    @GET("admin")
    suspend fun getAllAdmins(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort-dir") sortDir: String?,
    ): PaginationResponse<AdminData>

    @GET("admin/search-sort")
    suspend fun searchSortAdmins(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): PaginationResponse<AdminData>

    @GET("admin/{id}")
    suspend fun getAdminById(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<AdminObjectResponse>


    @PUT("admin/{id}")
    suspend fun updateAdmin(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body data: AdminData,
    ): Response<AdminObjectResponse>

    @POST("admin")
    suspend fun addAdmin(
        @Header("Authorization") token: String,
        @Body data: AdminData,
    ): Response<AdminObjectResponse>

    @DELETE("admin/{id}")
    suspend fun deleteAdmin(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<AdminObjectResponse>



    @GET("identitas-pribadi/{penggunaType}/{penggunaId}")
    fun getIdentityPersonal(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
    ): Call<IdentityPersonalObjectResponse>


    @PUT("identitas-pribadi/{penggunaType}/{penggunaId}")
    fun updateIdentityPersonal(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
        @Body identityPersonal: IdentityPersonalData,
    ): Call<IdentityPersonalObjectResponse>

    @DELETE("identitas-pribadi/{penggunaType}/{penggunaId}/address")
    fun deleteIdentityPersonalAddress(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
    ): Call<IdentityPersonalObjectResponse>

    @FormUrlEncoded
    @POST("identitas-pribadi/{penggunaType}/{penggunaId}/verify-email")
    fun verifyEmail(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
        @Field("email") email: String,
    ): Call<IdentityPersonalObjectResponse>

    @FormUrlEncoded
    @POST("identitas-pribadi/{penggunaType}/{penggunaId}/verify-email/check-token")
    fun verifyEmailCheckToken(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
        @Field("email") email: String,
        @Field("token") tokenVerifyEmail: String,
    ): Call<IdentityPersonalObjectResponse>


    /** Student */
    @GET("student")
    suspend fun getAllStudents(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort_dir") sortDir: String,
    ): PaginationResponse<StudentData>

    @GET("student/search-sort")
    suspend fun searchSortStudents(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): PaginationResponse<StudentData>

    @GET("student/{id}")
    suspend fun getStudentById(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<StudentObjectResponse>


    @PUT("student/{id}")
    suspend fun updateStudent(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body studentData: StudentData,
    ): Response<StudentObjectResponse>

    @POST("student")
    suspend fun addStudent(
        @Header("Authorization") token: String,
        @Body studentData: StudentData,
    ): Response<StudentObjectResponse>

    @DELETE("student/{id}")
    suspend fun deleteStudent(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<StudentObjectResponse>


    /** Identity Personal */
    @GET("identity-personal")
    suspend fun getAllIdentitiesPersonal(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("user-type") userType: String,
    ): PaginationResponse<IdentityPersonalData>

    @GET("identity-personal/search-sort")
    suspend fun searchSortIdentitiesPersonal(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
        @Query("user-type") userType: String,
    ): PaginationResponse<IdentityPersonalData>

    @GET("identity-personal/{userType}/{userId}/profile-picture")
    suspend fun getProfilePicture(
        @Header("Authorization") token: String,
        @Path("userType") userType: String,
        @Path("userId") userId: String,
    ): Response<ProfilePictureObjectResponse>

    @Multipart
    @POST("identity-personal/{userType}/{userId}/profile-picture?_method=PUT")
    suspend fun setManipulationProfilePicture(
        @Header("Authorization") token: String,
        @Path("userType") userType: String,
        @Path("userId") userId: String,
        @Part profilePicture: MultipartBody.Part?,
        @Query("is_deleted") isDeleted: Boolean
    ): Response<ProfilePictureObjectResponse>


    /** Identity Academic */
    @GET("identity-academic")
    suspend fun getAllIdentitiesAcademic(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): PaginationResponse<IdentityAcademicData>

    @GET("identity-academic/search-sort")
    suspend fun searchSortIdentitiesAcademic(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): PaginationResponse<IdentityAcademicData>

    /** Identity Parent */
    @POST("identity-parent")
    suspend fun addStudentIdentityParent(
        @Header("Authorization") token: String,
        @Body studentIdentityParentData: StudentIdentityParentData,
    ): Response<StudentIdentityParentObjectResponse>

    @GET("identity-parent")
    suspend fun getAllStudentIdentitiesParent(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): PaginationResponse<StudentIdentityParentData>

    @GET("identity-parent/search-sort")
    suspend fun searchSortStudentIdentitiesParent(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): PaginationResponse<StudentIdentityParentData>

    @GET("identity-parent/{id}")
    suspend fun getStudentIdentityParentById(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<StudentIdentityParentObjectResponse>

    @PUT("identity-parent/{id}")
    suspend fun updateStudentIdentityParent(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body studentIdentityParentData: StudentIdentityParentData,
    ): Response<StudentIdentityParentObjectResponse>


    @DELETE("identity-parent/{id}")
    suspend fun deleteStudentIdentityParent(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<StudentIdentityParentObjectResponse>

    @DELETE("identity-parent/{id}/address")
    suspend fun deleteStudentIdentityParentAddress(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<StudentIdentityParentObjectResponse>

    /** Address */
    @GET("address")
    suspend fun getAllAddresses(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("user-type") userType: String,
    ): PaginationResponse<AddressData>

    @GET("address/search-sort")
    suspend fun searchSortAddresses(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
        @Query("user-type") userType: String,
    ): PaginationResponse<AddressData>


    /** Employment Status */
    @POST("employment-status")
    fun addEmploymentStatus(
        @Header("Authorization") token: String,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @GET("employment-status")
    fun getAllEmploymentStatus(@Header("Authorization") token: String): Call<NameListResponse>

    @GET("employment-status/search-sort")
    fun searchSortEmploymentStatus(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<NameListResponse>

    @GET("employment-status/{id}")
    fun getEmploymentStatusById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<NameObjectResponse>

    @PUT("employment-status/{id}")
    fun updateEmploymentStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @DELETE("employment-status/{id}")
    fun deleteEmploymentStatus(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<NameObjectResponse>

    /** Student Status */
    @POST("student-status") // Mengubah dari class-session menjadi student-status
    fun addStudentStatus(
        @Header("Authorization") token: String,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @GET("student-status") // Mengubah dari class-session menjadi student-status
    fun getAllStudentStatus(@Header("Authorization") token: String): Call<NameListResponse>

    @GET("student-status/search-sort") // Mengubah dari class-session/search-sort menjadi student-status/search-sort
    fun searchSortStudentStatus(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<NameListResponse>

    @GET("student-status/{id}") // Mengubah dari class-session/{id} menjadi student-status/{id}
    fun getStudentStatusById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<NameObjectResponse>

    @PUT("student-status/{id}") // Mengubah dari class-session/{id} menjadi student-status/{id}
    fun updateStudentStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @DELETE("student-status/{id}") // Mengubah dari class-session/{id} menjadi student-status/{id}
    fun deleteStudentStatus(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<NameObjectResponse>

    /** Lecture Method */
    @POST("lecture-method")
    fun addLectureMethod(
        @Header("Authorization") token: String,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @GET("lecture-method")
    fun getAllLectureMethods(@Header("Authorization") token: String): Call<NameListResponse>

    @GET("lecture-method/search-sort")
    fun searchSortLectureMethod(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<NameListResponse>

    @GET("lecture-method/{id}")
    fun getLectureMethodById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<NameObjectResponse>

    @PUT("lecture-method/{id}")
    fun updateLectureMethod(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @DELETE("lecture-method/{id}")
    fun deleteLectureMethod(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<NameObjectResponse>


    /** Semester */
    @POST("semester")
    fun addSemester(
        @Header("Authorization") token: String,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @GET("semester")
    fun getAllSemesters(@Header("Authorization") token: String): Call<NameListResponse>

    @GET("semester/search-sort")
    fun searchSortSemester(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<NameListResponse>

    @GET("semester/{id}")
    fun getSemesterById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<NameObjectResponse>

    @PUT("semester/{id}")
    fun updateSemester(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @DELETE("semester/{id}")
    fun deleteSemester(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<NameObjectResponse>


    /** Class Session */
    @POST("class-session")
    fun addClassSession(
        @Header("Authorization") token: String,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @GET("class-session")
    fun getAllClassSession(@Header("Authorization") token: String): Call<NameListResponse>

    @GET("class-session/search-sort")
    fun searchSortClassSession(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<NameListResponse>

    @GET("class-session/{id}")
    fun getClassSessionById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<NameObjectResponse>

    @PUT("class-session/{id}")
    fun updateClassSession(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body name: NameData,
    ): Call<NameObjectResponse>

    @DELETE("class-session/{id}")
    fun deleteClassSession(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<NameObjectResponse>

    /** Campus */
    @POST("campus")
    fun addCampus(
        @Header("Authorization") token: String,
        @Body campus: CampusData,
    ): Call<CampusObjectResponse>

    @GET("campus")
    fun getAllCampus(@Header("Authorization") token: String): Call<CampusListResponse>

    @GET("campus/search-sort")
    fun searchSortCampus(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<CampusListResponse>

    @GET("campus/{id}")
    fun getCampusById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<CampusObjectResponse>

    @PUT("campus/{id}")
    fun updateCampus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body campus: CampusData,
    ): Call<CampusObjectResponse>

    @DELETE("campus/{id}")
    fun deleteCampus(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<CampusObjectResponse>

    /** STUDY PROGRAM */
    @POST("study-programs")
    fun addStudyProgram(
        @Header("Authorization") token: String,
        @Body identityAcademic: IdentityAcademicData,
    ): Call<IdentityAcademicObjectResponse>

    @GET("study-programs")
    fun getAllStudyPrograms(@Header("Authorization") token: String): Call<IdentityAcademicListResponse>

    @GET("study-programs/search-sort")
    fun searchSortStudyProgram(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<IdentityAcademicListResponse>

    @GET("study-programs/{id}")
    fun getStudyProgramById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>

    @PUT("study-programs/{id}")
    fun updateStudyProgram(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body identityAcademic: IdentityAcademicData,
    ): Call<IdentityAcademicObjectResponse>

    @DELETE("study-programs/{id}")
    fun deleteStudyProgram(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>


    /** FACULTY */
    @FormUrlEncoded
    @POST("faculties")
    fun addFaculty(
        @Header("Authorization") token: String,
        @Field("kode") kode: String,
        @Field("nama") nama: String,
    ): Call<IdentityAcademicObjectResponse>

    @GET("faculties")
    fun getAllFaculties(@Header("Authorization") token: String): Call<IdentityAcademicListResponse>

    @GET("faculties/{id}")
    fun getFacultyById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>

    @PUT("faculties/{id}")
    fun updateFaculty(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body identityAcademic: IdentityAcademicData,
    ): Call<IdentityAcademicObjectResponse>

    @DELETE("faculties/{id}")
    fun deleteFaculty(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>


    /** LEVEL */
    @FormUrlEncoded
    @POST("levels")
    fun addLevel(
        @Header("Authorization") token: String,
        @Field("kode") kode: String,
        @Field("nama") nama: String,
    ): Call<IdentityAcademicObjectResponse>

    @GET("levels")
    fun getAllLevels(@Header("Authorization") token: String): Call<IdentityAcademicListResponse>

    @GET("levels/search-sort")
    fun searchSortLevel(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<IdentityAcademicListResponse>

    @GET("levels/{id}")
    fun getLevelById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>

    @PUT("levels/{id}")
    fun updateLevel(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body identityAcademic: IdentityAcademicData,
    ): Call<IdentityAcademicObjectResponse>

    @DELETE("levels/{id}")
    fun deleteLevel(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>


    /** MAJOR */
    @FormUrlEncoded
    @POST("majors")
    fun addMajor(
        @Header("Authorization") token: String,
        @Field("kode") kode: String,
        @Field("nama") nama: String,
    ): Call<IdentityAcademicObjectResponse>

    @GET("majors")
    fun getAllMajors(@Header("Authorization") token: String): Call<IdentityAcademicListResponse>

    @GET("majors/search-sort")
    fun searchSortMajor(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<IdentityAcademicListResponse>

    @GET("majors/{id}")
    fun getMajorById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>

    @PUT("majors/{id}")
    fun updateMajor(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body identityAcademic: IdentityAcademicData,
    ): Call<IdentityAcademicObjectResponse>

    @DELETE("majors/{id}")
    fun deleteMajor(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>

    /** DEGREE */
    @FormUrlEncoded
    @POST("degrees")
    fun addDegree(
        @Header("Authorization") token: String,
        @Field("kode") kode: String,
        @Field("nama") nama: String,
    ): Call<IdentityAcademicObjectResponse>

    @GET("degrees")
    fun getAllDegrees(@Header("Authorization") token: String): Call<IdentityAcademicListResponse>

    @GET("degrees/search-sort")
    fun searchSortDegree(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): Call<IdentityAcademicListResponse>

    @GET("degrees/{id}")
    fun getDegreeById(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>

    @PUT("degrees/{id}")
    fun updateDegree(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body identityAcademic: IdentityAcademicData,
    ): Call<IdentityAcademicObjectResponse>

    @DELETE("degrees/{id}")
    fun deleteDegree(
        @Header("Authorization") token: String, @Path("id") id: Int,
    ): Call<IdentityAcademicObjectResponse>


    /** Lecture */
    @GET("lecturer")
    suspend fun getAllLecturers(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort-dir") sortDir: String?,
    ): PaginationResponse<LecturerData>

    @GET("lecturer/search-sort")
    suspend fun searchSortLecturers(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("keyword") keyword: String?,
        @Query("sort-by") sortBy: String?,
        @Query("sort-dir") sortDir: String?,
    ): PaginationResponse<LecturerData>

    @GET("lecturer/{id}")
    suspend fun getLecturerById(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<LecturerObjectResponse>


    @PUT("lecturer/{id}")
    suspend fun updateLecturer(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body data: LecturerData,
    ): Response<LecturerObjectResponse>

    @POST("lecturer")
    suspend fun addLecturer(
        @Header("Authorization") token: String,
        @Body data: LecturerData,
    ): Response<LecturerObjectResponse>

    @DELETE("lecturer/{id}")
    suspend fun deleteLecturer(
        @Header("Authorization") token: String,
        @Path("id") id: String,
    ): Response<LecturerObjectResponse>


    /** PASSWORD */
    @FormUrlEncoded
    @POST("reset-password")
    fun resetPassword(
        @Field("email") email: String,
    ): Call<UserResponse>

    @FormUrlEncoded
    @POST("reset-password/check-token")
    fun resetPasswordCheckToken(
        @Field("email") email: String,
        @Field("token") token: String,
    ): Call<UserResponse>


    @FormUrlEncoded
    @POST("reset-password/update?_method=PATCH")
    fun updateUserCurrentPassword(
        @Field("pengguna_id") userId: String,
        @Field("pengguna_type") userType: String,
        @Field("password") password: String,
    ): Call<UserResponse>


}