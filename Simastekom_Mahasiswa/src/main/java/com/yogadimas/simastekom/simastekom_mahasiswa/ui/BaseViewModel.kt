package com.yogadimas.simastekom.simastekom_mahasiswa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogadimas.simastekom.core.domain.repository.auth.IAuthUserRepository
import kotlinx.coroutines.launch

open class BaseViewModel(private val authUserRepository: IAuthUserRepository) : ViewModel() {
    fun logoutLocal() = viewModelScope.launch { authUserRepository.clearUser() }
}