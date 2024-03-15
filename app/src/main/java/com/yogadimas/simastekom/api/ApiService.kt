package com.yogadimas.simastekom.api

import com.yogadimas.simastekom.model.AdminResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface ApiService {

    @FormUrlEncoded
    @POST("api/admin/login")
    fun postLoginAdmin(
        @Field("nama_pengguna") namaPengguna: String,
        @Field("password") password: String,
    ): Call<AdminResponse>
}