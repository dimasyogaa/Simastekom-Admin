package com.yogadimas.simastekom.common.state

import com.yogadimas.simastekom.model.responses.Errors

sealed class State<out R> private constructor() {

    data class Success<out T>(val data: T) : State<T>()

    data class ErrorClient(val error: Errors) : State<Nothing>()
    data class ErrorServer(val error: String) : State<Nothing>()

    data object Loading : State<Nothing>()

}
