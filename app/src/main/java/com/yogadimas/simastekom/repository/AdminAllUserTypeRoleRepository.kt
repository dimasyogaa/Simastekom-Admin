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
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.AddressData
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.IdentityPersonalObjectResponse
import com.yogadimas.simastekom.model.responses.ProfilePictureData
import com.yogadimas.simastekom.model.responses.ProfilePictureObjectResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.MultipartBody
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


    private val _profilePictureState = MutableSharedFlow<State<ProfilePictureData>>()
    val profilePictureState: SharedFlow<State<ProfilePictureData>> get() = _profilePictureState.asSharedFlow()
    suspend fun getProfilePicture(token: String, userType: String, userId: String) {
        _profilePictureState.emit(State.Loading)
        val response = runCatching { apiService.getProfilePicture(token, userType, userId) }
        handleApiResponse(response)
    }

    suspend fun setManipulationProfilePicture(
        token: String,
        userType: String,
        userId: String,
        profilePicture: MultipartBody.Part?,
        isDeleted: Boolean,
    ) {
        _profilePictureState.emit(State.Loading)
        val response = runCatching {
            apiService.setManipulationProfilePicture(
                token,
                userType,
                userId,
                profilePicture,
                isDeleted
            )
        }
        handleApiResponse(response)
    }

    private suspend fun handleApiResponse(response: Result<Response<ProfilePictureObjectResponse>>) {
        response.onSuccess {
            val data = it.body()?.profilePictureData ?: ProfilePictureData()
            if (it.isSuccessful) {
                _profilePictureState.emit(State.Success(data))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _profilePictureState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure {
            _profilePictureState.emit(State.ErrorServer(it.message.toString()))
        }
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