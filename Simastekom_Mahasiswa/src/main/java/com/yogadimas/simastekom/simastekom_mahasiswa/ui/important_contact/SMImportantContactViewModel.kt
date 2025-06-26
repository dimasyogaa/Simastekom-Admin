package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_contact

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
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

class SMImportantContactViewModel(
    authUserRepository: IAuthUserRepository,
    private val repository: IImportantContactRepository
) : BaseViewModel(authUserRepository) {


    fun getImportantContacts(searchKeyword: String?): SharedFlow<PagingData<ImportantContactData>> {
        return repository.getImportantContacts(searchKeyword)
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily
            )
    }

    val importantContactState: SharedFlow<UiState<BaseResponse<ImportantContactData>>> =
        repository.importantContactState

    fun getImportantContactById(id: Int) = viewModelScope.launch {
        repository.getImportantContactById(id)
    }

    fun updateImportantContact(id: Int, data: ImportantContactRequest) = viewModelScope.launch {
        repository.updateImportantContact(id, data)
    }

    fun createImportantContact(data: ImportantContactRequest) = viewModelScope.launch {
        repository.createImportantContact(data)
    }

    fun deleteImportantContact(id: Int) = viewModelScope.launch {
        repository.deleteImportantContact(id)
    }


}