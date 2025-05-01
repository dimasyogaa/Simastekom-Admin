package com.yogadimas.simastekom.viewmodel.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yogadimas.simastekom.common.enums.HttpResponseType
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.LecturerData
import com.yogadimas.simastekom.repository.AdminLecturerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminLecturerViewModel(
    private val repository: AdminLecturerRepository,
) :
    ViewModel() {


    fun getLecturers(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
    ): StateFlow<PagingData<LecturerData>> {
        val flow = if (keyword.isNullOrEmpty() && sortBy.isNullOrEmpty()) {
            repository.getLecturers(
                token,
                sortDir = sortDir,
                onError = ::handleError
            )
        }  else {
            repository.getLecturers(
                token,
                keyword,
                sortBy,
                sortDir,
                onError = ::handleError
            )
        }

        return flow
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = PagingData.empty()
            )
    }

    private val _errorStateFlow = MutableStateFlow<String?>(null)
    val errorStateFlow: StateFlow<String?> = _errorStateFlow
    private fun handleError(errorMessage: String) {
        _errorStateFlow.value = errorMessage
    }



    val lecturerState : SharedFlow<State<LecturerData>> = repository.lectureState
    fun getLecturerById(token: String, id: String) = viewModelScope.launch {
        repository.getLecturerById(token, id)
    }

    fun updateLecturer(token: String, id: String, data: LecturerData) = viewModelScope.launch {
        repository.updateLecturer(token, id, data)
    }

    fun addLecturer(token: String, data: LecturerData) = viewModelScope.launch {
        repository.addLecturer(token, data)
    }

    fun deleteLecturer(token: String, id: String) = viewModelScope.launch {
        repository.deleteLecturer(token, id)
    }


    fun addLecturerDummy(status: HttpResponseType,
                         data: LecturerData,) = viewModelScope.launch {
        repository.addLecturerDummy(status, data)
    }



}