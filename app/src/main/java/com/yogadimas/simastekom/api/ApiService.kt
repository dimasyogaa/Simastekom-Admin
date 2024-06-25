package com.yogadimas.simastekom.api

import com.yogadimas.simastekom.model.responses.AdminResponse
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.model.responses.IdentityAcademicListResponse
import com.yogadimas.simastekom.model.responses.IdentityAcademicObjectResponse
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.IdentityPersonalResponse
import com.yogadimas.simastekom.model.responses.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
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
    ): Call<AdminResponse>

    @DELETE("admin/logout")
    fun logout(@Header("Authorization") token: String): Call<AdminResponse>

    @GET("admin/current")
    fun getAdminCurrent(@Header("Authorization") token: String): Call<AdminResponse>

    @GET("admin/current/password")
    fun getAdminPassword(@Header("Authorization") token: String): Call<AdminResponse>

    @Multipart
    @POST("admin/current?_method=PATCH")
    fun updateAdminCurrent(
        @Header("Authorization") token: String,
        @Part profilePicture: MultipartBody.Part?,
        @Part("nama_pengguna") username: RequestBody,
        @Part("nama") name: RequestBody,
        @Part("hapus_foto") deletePhoto: RequestBody,
    ): Call<AdminResponse>

    @FormUrlEncoded
    @POST("admin/current/password?_method=PATCH")
    fun updateAdminCurrentPassword(
        @Header("Authorization") token: String,
        @Field("password") password: String,
    ): Call<AdminResponse>

    @GET("identitas-pribadi/{penggunaType}/{penggunaId}")
    fun getIdentityPersonal(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
    ): Call<IdentityPersonalResponse>


    @PUT("identitas-pribadi/{penggunaType}/{penggunaId}")
    fun updateIdentityPersonal(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
        @Body identityPersonal: IdentityPersonalData,
    ): Call<IdentityPersonalResponse>

    @FormUrlEncoded
    @POST("identitas-pribadi/{penggunaType}/{penggunaId}/verify-email")
    fun verifyEmail(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
        @Field("email") email: String,
    ): Call<IdentityPersonalResponse>

    @FormUrlEncoded
    @POST("identitas-pribadi/{penggunaType}/{penggunaId}/verify-email/check-token")
    fun verifyEmailCheckToken(
        @Header("Authorization") token: String,
        @Path("penggunaType") userType: String,
        @Path("penggunaId") userId: String,
        @Field("email") email: String,
        @Field("token") tokenVerifyEmail: String,
    ): Call<IdentityPersonalResponse>

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
        @Header("Authorization") token: String, @Path("id") id: Int
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
        @Header("Authorization") token: String, @Path("id") id: Int
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
        @Header("Authorization") token: String, @Path("id") id: Int
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
        @Header("Authorization") token: String, @Path("id") id: Int
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
        @Header("Authorization") token: String, @Path("id") id: Int
    ): Call<IdentityAcademicObjectResponse>


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