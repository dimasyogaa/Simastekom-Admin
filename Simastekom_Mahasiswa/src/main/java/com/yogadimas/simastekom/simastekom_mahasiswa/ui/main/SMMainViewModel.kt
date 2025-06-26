package com.yogadimas.simastekom.simastekom_mahasiswa.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogadimas.simastekom.core.domain.repository.auth.IAuthUserRepository
import com.yogadimas.simastekom.core.ui.model.auth.AuthUserUiModel
import com.yogadimas.simastekom.core.utils.mapper.auth.toEntity
import com.yogadimas.simastekom.core.utils.mapper.auth.toUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class SMMainViewModel(
    private val authUserRepository: IAuthUserRepository
) : ViewModel() {

    fun setSaveUser(authUserUiModel: AuthUserUiModel) = viewModelScope.launch {
        authUserRepository.setSaveUser(authUserUiModel.toEntity())
    }


    fun getUser() = authUserRepository.getUser().map { it.toUiModel() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            replay = 1
        )

}