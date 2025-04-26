package com.yogadimas.simastekom.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yogadimas.simastekom.api.ApiService
import com.yogadimas.simastekom.common.enums.HttpResponseType
import com.yogadimas.simastekom.common.enums.ManipulationType
import com.yogadimas.simastekom.common.helper.getData
import com.yogadimas.simastekom.common.helper.handleApiCall
import com.yogadimas.simastekom.common.helper.handleApiCallDummy
import com.yogadimas.simastekom.common.paging.Constant
import com.yogadimas.simastekom.common.paging.GenericPagingSource
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.LecturerData
import com.yogadimas.simastekom.model.responses.LecturerObjectResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class AdminLecturerRepository(private val apiService: ApiService) {

    /** Lecture */
    fun getLecturers(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
        onError: (String) -> Unit,
    ): Flow<PagingData<LecturerData>> {
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
                        if (keyword.isNullOrEmpty() && sortBy.isNullOrEmpty()) {
                            apiService.getAllLecturers(
                                token,
                                page,
                                size,
                                sortDir
                            )
                        } else {
                            apiService.searchSortLecturers(
                                token,
                                page,
                                size,
                                keyword,
                                sortBy,
                                sortDir,
                            )
                        }
                    },
                    onError = onError
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }


    private val _lectureState = MutableSharedFlow<State<LecturerData>>()
    val lectureState: SharedFlow<State<LecturerData>> get() = _lectureState.asSharedFlow()


    private suspend fun handleLectureApiCall(
        apiCall: suspend () -> Response<LecturerObjectResponse>,
    ) {
        handleApiCall(
            sharedFlow = _lectureState,
            apiCall = apiCall,
            extractData = { getData(it, { data -> data?.data }, LecturerData()) }
        )
    }


    suspend fun getLecturerById(token: String, id: String) {
        handleLectureApiCall { apiService.getLecturerById(token, id) }
    }

    suspend fun updateLecturer(token: String, id: String, data: LecturerData) {
        handleLectureApiCall { apiService.updateLecturer(token, id, data) }
    }

    suspend fun addLecturer(token: String, data: LecturerData) {
        handleLectureApiCall { apiService.addLecturer(token, data) }
    }

    suspend fun deleteLecturer(token: String, id: String) {
        handleLectureApiCall { apiService.deleteLecturer(token, id) }
    }


    private suspend fun handleLectureApiCallDummy(
        status: HttpResponseType,
        data: LecturerData,
    ) {
        handleApiCallDummy(
            sharedFlow = _lectureState,
            status = status,
            data = data,
            extractData = { getData(it, { data -> data }, LecturerData()) }
        )
    }

    suspend fun addLecturerDummy(
        status: HttpResponseType,
        data: LecturerData,

    ) {
        handleLectureApiCallDummy (status, data)
    }


}