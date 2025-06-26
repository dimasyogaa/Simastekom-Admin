package com.yogadimas.simastekom.core.data.repository

import android.util.Log
import androidx.paging.PagingData
import com.yogadimas.simastekom.core.data.source.remote.datasource.ImportantContactRemoteDataSource
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactCategoryRequest
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.domain.repository.IImportantContactRepository
import com.yogadimas.simastekom.core.ui.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

class ImportantContactRepository(
    private val remoteDataSource: ImportantContactRemoteDataSource
) : IImportantContactRepository {


    override fun getImportantContacts(searchKeyword: String?): Flow<PagingData<ImportantContactData>> =
        remoteDataSource.getImportantContacts(searchKeyword)

    override val importantContactState: SharedFlow<UiState<BaseResponse<ImportantContactData>>> =
        remoteDataSource.importantContactState

    override suspend fun getImportantContactById(id: Int) {
        remoteDataSource.getImportantContactById(id)
    }

    override suspend fun updateImportantContact(id: Int, data: ImportantContactRequest) {
        remoteDataSource.updateImportantContact(id, data)
    }

    override suspend fun createImportantContact(data: ImportantContactRequest) {
        remoteDataSource.createImportantContact(data)
    }

    override suspend fun deleteImportantContact(id: Int) {
        remoteDataSource.deleteImportantContact(id)
    }

    override fun getImportantContactCategories(searchKeyword: String?): Flow<PagingData<ImportantContactCategoryData>> =
        remoteDataSource.getImportantContactCategories(searchKeyword)

    override val importantContactCategoryState: SharedFlow<UiState<BaseResponse<ImportantContactCategoryData>>>
            = remoteDataSource.importantContactCategoryState

    override suspend fun getImportantContactCategoryById(id: Int) {
        remoteDataSource.getImportantContactCategoryById(id)
    }

    override suspend fun updateImportantContactCategory(
        id: Int,
        data: ImportantContactCategoryRequest
    ) {
        remoteDataSource.updateImportantContactCategory(id, data)
    }

    override suspend fun createImportantContactCategory(data: ImportantContactCategoryRequest) {
        remoteDataSource.createImportantContactCategory(data)
    }

    override suspend fun deleteImportantContactCategory(id: Int) {
        remoteDataSource.deleteImportantContactCategory(id)
    }


}