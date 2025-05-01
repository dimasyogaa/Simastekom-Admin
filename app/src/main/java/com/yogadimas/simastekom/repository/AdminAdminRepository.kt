package com.yogadimas.simastekom.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yogadimas.simastekom.api.ApiService
import com.yogadimas.simastekom.common.helper.getData
import com.yogadimas.simastekom.common.helper.handleApiCall
import com.yogadimas.simastekom.common.paging.Constant
import com.yogadimas.simastekom.common.paging.GenericPagingSource
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.model.responses.AdminObjectResponse
import com.yogadimas.simastekom.model.responses.PaginationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class AdminAdminRepository(private val apiService: ApiService) {

    /** Admin */
    fun getAdmins(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
        onError: (String) -> Unit,
    ): Flow<PagingData<AdminData>> {
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
                    fetchData = createFetchFunction(token),
                    onError = onError
                )
            }
        ).flow.flowOn(Dispatchers.IO)
    }

    suspend fun getAdminById(token: String, id: String) {
        handleLectureApiCall { apiService.getAdminById(token, id) }
    }

    suspend fun updateAdmin(token: String, id: String, data: AdminData) {
        handleLectureApiCall { apiService.updateAdmin(token, id, data) }
    }

    suspend fun addAdmin(token: String, data: AdminData) {
        handleLectureApiCall { apiService.addAdmin(token, data) }
    }

    suspend fun deleteAdmin(token: String, id: String) {
        handleLectureApiCall { apiService.deleteAdmin(token, id) }
    }

    private fun createFetchFunction(token: String): suspend (
        token: String,
        page: Int,
        size: Int,
        keyword: String?,
        sortBy: String?,
        sortDir: String?,
    ) -> PaginationResponse<AdminData> = { _, page, size, keyword, sortBy, sortDir ->
        when {
            keyword.isNullOrEmpty() && sortBy.isNullOrEmpty() -> {
                apiService.getAllAdmins(token, page, size, sortDir)
            }

            else -> {
                apiService.searchSortAdmins(token, page, size, keyword, sortBy, sortDir)
            }
        }
    }

    private val _adminState = MutableSharedFlow<State<AdminData>>()
    val adminState: SharedFlow<State<AdminData>> get() = _adminState.asSharedFlow()

    private suspend fun handleLectureApiCall(
        apiCall: suspend () -> Response<AdminObjectResponse>,
    ) {
        handleApiCall(
            sharedFlow = _adminState,
            apiCall = apiCall,
            extractData = { getData(it, { data -> data?.adminData }, AdminData()) }
        )
    }

}