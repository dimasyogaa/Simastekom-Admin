package com.yogadimas.simastekom.repository

import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yogadimas.simastekom.api.ApiConfig
import com.yogadimas.simastekom.api.ApiService
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.common.event.Event
import com.yogadimas.simastekom.common.helper.getErrors
import com.yogadimas.simastekom.common.paging.Constant
import com.yogadimas.simastekom.common.paging.GenericPagingSource
import com.yogadimas.simastekom.model.responses.AddressData
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.IdentityPersonalObjectResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminAllUserTypeRoleRepository(private val apiService: ApiService) {


    /** Identity Personal */
    fun getIdentitiesPersonal(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
        role: Role,
        onError: (String) -> Unit,
    ): Flow<PagingData<IdentityPersonalData>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constant.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    token = token,
                    keyword = keyword,
                    sortBy = sortBy,
                    sortDir = sortDir,
                    fetchData = { token, page, size, keyword, sortBy, sortDir ->
                        if (keyword.isNullOrEmpty() && sortBy == null) {
                            apiService.getAllIdentitiesPersonal(
                                token,
                                page,
                                size,
                                role.value
                            )
                        } else {
                            apiService.searchSortIdentitiesPersonal(
                                token,
                                page,
                                size,
                                keyword,
                                sortBy,
                                sortDir,
                                role.value
                            )
                        }
                    },
                    onError = onError
                )
            }
        ).flow
    }


    fun deleteIdentityPersonalAddress(
        token: String,
        userType: String,
        userId: String,
        data: MutableLiveData<Event<IdentityPersonalData?>>,
        isLoading: MutableLiveData<Boolean>,
        errors: MutableLiveData<Event<Errors?>>,
        snackBarText: MutableLiveData<Event<String>>,
    ) {
        isLoading.value = true
        val client =
            ApiConfig.getApiService()
                .deleteIdentityPersonalAddress(token, userType, userId)
        client.enqueue(object : Callback<IdentityPersonalObjectResponse> {
            override fun onResponse(
                call: Call<IdentityPersonalObjectResponse>,
                response: Response<IdentityPersonalObjectResponse>,
            ) {
                isLoading.value = false
                if (response.isSuccessful) {
                    data.value = Event(response.body()?.identityPersonalData)
                } else {
                    errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }
            override fun onFailure(call: Call<IdentityPersonalObjectResponse>, t: Throwable) {
                isLoading.value = false
                snackBarText.value = Event(t.message.toString())
            }
        })

    }

    /** Address */
    fun getAddresses(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
        role: Role,
        onError: (String) -> Unit,
    ): Flow<PagingData<AddressData>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constant.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    token = token,
                    keyword = keyword,
                    sortBy = sortBy,
                    sortDir = sortDir,
                    fetchData = { token, page, size, keyword, sortBy, sortDir ->
                        if (keyword.isNullOrEmpty() && sortBy == null) {
                            apiService.getAllAddresses(
                                token,
                                page,
                                size,
                                role.value
                            )
                        } else {
                            apiService.searchSortAddresses(
                                token,
                                page,
                                size,
                                keyword,
                                sortBy,
                                sortDir,
                                role.value
                            )
                        }
                    },
                    onError = onError
                )
            }
        ).flow
    }


}