package com.yogadimas.simastekom.core.data.source.remote.datasource

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yogadimas.simastekom.core.common.constants.PAGE_SIZE
import com.yogadimas.simastekom.core.data.source.remote.datasource.paging.GenericPagingSource
import com.yogadimas.simastekom.core.data.source.remote.datasource.paging.ImportantContactPagingSource
import com.yogadimas.simastekom.core.data.source.remote.network.SimastekomMahasiswaApiService
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactCategoryRequest
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactRequest
import com.yogadimas.simastekom.core.data.source.remote.request.paging.PagingRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.ui.UiState
import com.yogadimas.simastekom.core.utils.getData
import com.yogadimas.simastekom.core.utils.handleApiCall
import com.yogadimas.simastekom.core.utils.toQueryMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

class ImportantContactRemoteDataSource(private val apiService: SimastekomMahasiswaApiService) {

    fun getImportantContacts(searchKeyword: String?): Flow<PagingData<ImportantContactData>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ImportantContactPagingSource(apiService, searchKeyword)
            }
        ).flow.flowOn(Dispatchers.IO)
    }

    private val _importantContactState =
        MutableSharedFlow<UiState<BaseResponse<ImportantContactData>>>()
    val importantContactState: SharedFlow<UiState<BaseResponse<ImportantContactData>>> =
        _importantContactState

    suspend fun getImportantContactById(id: Int) {
        fetchImportantContact { apiService.getImportantContactById(id) }
    }

    suspend fun updateImportantContact(id: Int, data: ImportantContactRequest) {
        fetchImportantContact { apiService.updateImportantContact(id, data) }
    }

    suspend fun createImportantContact(data: ImportantContactRequest) {
        fetchImportantContact { apiService.createImportantContact(data) }
    }

    suspend fun deleteImportantContact(id: Int) {
        fetchImportantContact { apiService.deleteImportantContact(id) }
    }

    private suspend fun fetchImportantContact(
        apiCall: suspend () -> Response<BaseResponse<ImportantContactData>>,
    ) {
        handleApiCall(
            sharedFlow = _importantContactState,
            apiCall = apiCall,
            transform = {
                getData(
                    response = it,
                    extractor = { data -> data },
                    defaultValue = ImportantContactData()
                )
            }
        )
    }


    fun getImportantContactCategories(searchKeyword: String?): Flow<PagingData<ImportantContactCategoryData>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GenericPagingSource { page, size ->
                    val request =
                        PagingRequest(page = page, size = size, searchKeyword = searchKeyword)
                    apiService.getImportantContactCategories(request.toQueryMap())
                }
            }
        ).flow.flowOn(Dispatchers.IO)
    }

    private val _importantContactCategoryState =
        MutableSharedFlow<UiState<BaseResponse<ImportantContactCategoryData>>>()
    val importantContactCategoryState: SharedFlow<UiState<BaseResponse<ImportantContactCategoryData>>> =
        _importantContactCategoryState

    suspend fun getImportantContactCategoryById(id: Int) {
        fetchImportantContactCategory { apiService.getImportantContactCategoryById(id) }
    }

    suspend fun updateImportantContactCategory(id: Int, data: ImportantContactCategoryRequest) {
        fetchImportantContactCategory { apiService.updateImportantContactCategory(id, data) }
    }

    suspend fun createImportantContactCategory(data: ImportantContactCategoryRequest) {
        fetchImportantContactCategory { apiService.createImportantContactCategory(data) }
    }

    suspend fun deleteImportantContactCategory(id: Int) {
        fetchImportantContactCategory { apiService.deleteImportantContactCategory(id) }
    }

    private suspend fun fetchImportantContactCategory(
        apiCall: suspend () -> Response<BaseResponse<ImportantContactCategoryData>>,
    ) {
        handleApiCall(
            sharedFlow = _importantContactCategoryState,
            apiCall = apiCall,
            transform = {
                getData(
                    response = it,
                    extractor = { data -> data },
                    defaultValue = ImportantContactCategoryData()
                )
            }
        )
    }


}


