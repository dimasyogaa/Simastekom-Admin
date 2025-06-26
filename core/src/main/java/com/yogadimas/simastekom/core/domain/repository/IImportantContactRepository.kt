package com.yogadimas.simastekom.core.domain.repository

import androidx.paging.PagingData
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactCategoryRequest
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.ui.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface IImportantContactRepository {
    fun getImportantContacts(searchKeyword: String?): Flow<PagingData<ImportantContactData>>
    val importantContactState: SharedFlow<UiState<BaseResponse<ImportantContactData>>>
    suspend fun getImportantContactById(id: Int)
    suspend fun updateImportantContact(id: Int, data: ImportantContactRequest)
    suspend fun createImportantContact(data: ImportantContactRequest)
    suspend fun deleteImportantContact(id: Int)

    fun getImportantContactCategories(searchKeyword: String?): Flow<PagingData<ImportantContactCategoryData>>
    val importantContactCategoryState: SharedFlow<UiState<BaseResponse<ImportantContactCategoryData>>>
    suspend fun getImportantContactCategoryById(id: Int)
    suspend fun updateImportantContactCategory(id: Int, data: ImportantContactCategoryRequest)
    suspend fun createImportantContactCategory(data: ImportantContactCategoryRequest)
    suspend fun deleteImportantContactCategory(id: Int)

}