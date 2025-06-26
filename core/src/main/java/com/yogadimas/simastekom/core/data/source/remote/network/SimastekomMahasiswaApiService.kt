package com.yogadimas.simastekom.core.data.source.remote.network

import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactCategoryRequest
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.data.source.remote.response.paging.PagingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap


interface SimastekomMahasiswaApiService {

    @GET("kontak-penting")
    suspend fun getImportantContacts(@QueryMap queryMap: Map<String, String>): PagingResponse<ImportantContactData>

    @GET("kontak-penting/{id}")
    suspend fun getImportantContactById(@Path("id") id: Int): Response<BaseResponse<ImportantContactData>>

    @POST("kontak-penting")
    suspend fun createImportantContact(@Body contact: ImportantContactRequest): Response<BaseResponse<ImportantContactData>>

    @PUT("kontak-penting/{id}")
    suspend fun updateImportantContact(
        @Path("id") id: Int,
        @Body contact: ImportantContactRequest
    ): Response<BaseResponse<ImportantContactData>>

    @DELETE("kontak-penting/{id}")
    suspend fun deleteImportantContact(@Path("id") id: Int): Response<BaseResponse<ImportantContactData>>

    @GET("kontak-penting-kategori")
    suspend fun getImportantContactCategories(@QueryMap queryMap: Map<String, String>): PagingResponse<ImportantContactCategoryData>

    @GET("kontak-penting-kategori/{id}")
    suspend fun getImportantContactCategoryById(@Path("id") id: Int): Response<BaseResponse<ImportantContactCategoryData>>

    @POST("kontak-penting-kategori")
    suspend fun createImportantContactCategory(@Body contact: ImportantContactCategoryRequest): Response<BaseResponse<ImportantContactCategoryData>>

    @PUT("kontak-penting-kategori/{id}")
    suspend fun updateImportantContactCategory(
        @Path("id") id: Int,
        @Body contact: ImportantContactCategoryRequest
    ): Response<BaseResponse<ImportantContactCategoryData>>

    @DELETE("kontak-penting-kategori/{id}")
    suspend fun deleteImportantContactCategory(@Path("id") id: Int): Response<BaseResponse<ImportantContactCategoryData>>

}