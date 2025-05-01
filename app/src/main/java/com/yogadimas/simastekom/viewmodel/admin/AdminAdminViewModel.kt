package com.yogadimas.simastekom.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.repository.AdminAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminAdminViewModel(
    private val repository: AdminAdminRepository,
) : ViewModel() {

    private val _errorStateFlow = MutableStateFlow<String?>(null)
    val errorStateFlow: StateFlow<String?> = _errorStateFlow

    fun getAdmins(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
    ): StateFlow<PagingData<AdminData>> {
        val flow = repository.getAdmins(
            token = token,
            keyword = keyword.takeIf { !it.isNullOrEmpty() },
            sortBy = sortBy.takeIf { !it.isNullOrEmpty() },
            sortDir = sortDir,
            onError = ::handleError
        )

        return flow
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = PagingData.empty()
            )
    }

    private fun handleError(errorMessage: String) {
        _errorStateFlow.value = errorMessage
    }


    val adminState : SharedFlow<State<AdminData>> = repository.adminState
    fun getAdminById(token: String, id: String) = viewModelScope.launch {
        repository.getAdminById(token, id)
    }
    fun updateAdmin(token: String, id: String, data: AdminData) = viewModelScope.launch {
        repository.updateAdmin(token, id, data)
    }

    fun addAdmin(token: String, data: AdminData) = viewModelScope.launch {
        repository.addAdmin(token, data)
    }

    fun deleteAdmin(token: String, id: String) = viewModelScope.launch {
        repository.deleteAdmin(token, id)
    }


}
