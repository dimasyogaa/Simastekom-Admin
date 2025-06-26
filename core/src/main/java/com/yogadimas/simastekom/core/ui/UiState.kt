package com.yogadimas.simastekom.core.ui

import com.yogadimas.simastekom.core.utils.ErrorClient as ClientError


sealed class UiState<out R> {

    data class Success<T>(val data: T) : UiState<T>()

    data class ErrorClient(val errorClient: ClientError) : UiState<Nothing>()
    data class ErrorServer(val message: String) : UiState<Nothing>()

    data object Loading : UiState<Nothing>()

}