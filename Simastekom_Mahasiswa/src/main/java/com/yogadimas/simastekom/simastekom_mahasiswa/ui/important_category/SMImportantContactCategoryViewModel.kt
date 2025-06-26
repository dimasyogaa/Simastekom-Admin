package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_category

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactCategoryRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactCategoryData
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.domain.repository.IImportantContactRepository
import com.yogadimas.simastekom.core.domain.repository.auth.IAuthUserRepository
import com.yogadimas.simastekom.core.ui.UiState
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.BaseViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class SMImportantContactCategoryViewModel(
    authUserRepository: IAuthUserRepository,
    private val repository: IImportantContactRepository
) : BaseViewModel(authUserRepository) {

    fun getImportantContactCategories(searchKeyword: String?): SharedFlow<PagingData<ImportantContactCategoryData>> {
        return repository.getImportantContactCategories(searchKeyword)
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily
            )
    }

    val importantContactCategoryState: SharedFlow<UiState<BaseResponse<ImportantContactCategoryData>>> =
        repository.importantContactCategoryState

    fun getImportantContactCategoryById(id: Int) = viewModelScope.launch {
        repository.getImportantContactCategoryById(id)
    }

    fun updateImportantContactCategory(id: Int, data: ImportantContactCategoryRequest) =
        viewModelScope.launch {
            repository.updateImportantContactCategory(id, data)
        }

    fun createImportantContactCategory(data: ImportantContactCategoryRequest) =
        viewModelScope.launch {
            repository.createImportantContactCategory(data)
        }

    fun deleteImportantContactCategory(id: Int) = viewModelScope.launch {
        repository.deleteImportantContactCategory(id)
    }
}