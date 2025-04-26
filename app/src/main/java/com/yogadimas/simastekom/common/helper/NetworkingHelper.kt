package com.yogadimas.simastekom.common.helper

import android.util.Log
import com.google.gson.Gson
import com.yogadimas.simastekom.common.enums.HttpResponseType
import com.yogadimas.simastekom.common.enums.ManipulationType
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.LecturerData
import com.yogadimas.simastekom.model.responses.UserResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

suspend fun <T, R> handleApiCall(
    sharedFlow: MutableSharedFlow<State<R>>,
    apiCall: suspend () -> Response<T>,
    extractData: (T?) -> R,
) {
    Log.e("TAG", "handleApiCall: loading", )
    sharedFlow.emit(State.Loading)
    val response = runCatching { apiCall() }

    response.onSuccess { result ->
        if (result.isSuccessful) {
            val data = extractData(result.body())
            Log.e("TAG", "handleApiCall: data", )
            sharedFlow.emit(State.Success(data))
        } else {
            val errorResponse = result.errorBody()?.string().orEmpty()
            sharedFlow.emit(State.ErrorClient(getErrors(errorResponse)))
        }
    }.onFailure { exception ->
        sharedFlow.emit(State.ErrorServer(exception.message.toString()))
    }
}

fun <T, R> getData(response: T?, extractor: (T?) -> R?, defaultValue: R): R {
    return extractor(response) ?: defaultValue
}


suspend fun <T, R> handleApiCallDummy(
    sharedFlow: MutableSharedFlow<State<R>>,
    status: HttpResponseType,
    data: T,
    extractData: (T?) -> R,
) {
    sharedFlow.emit(State.Loading)
    val response = runCatching { apiCallDummy(status = status, data = data) }
    delay(3000)
    response.onSuccess { result ->
        if (result.isSuccessful) {
            val dataResult = extractData(result.body())
            sharedFlow.emit(State.Success(dataResult))
        } else {
            val errorResponse = result.errorBody()?.string().orEmpty()
            sharedFlow.emit(State.ErrorClient(getErrors(errorResponse)))
        }
    }.onFailure { exception ->
        sharedFlow.emit(State.ErrorServer(exception.message.toString()))
    }
}

/*************************************************************************************************/

fun <T> apiCallDummy(
    status: HttpResponseType,
    data: T,
): Response<T> {
    val success = HttpResponseType.SUCCESS
    val errorClient = HttpResponseType.ERROR_CLIENT
    val unauthorized = HttpResponseType.UNAUTHORIZED
    val errorServer = HttpResponseType.ERROR_SERVER
    return when (status) {
        success -> Response.success(200, data)
        errorClient -> createErrorJsonWithMap(errorClient.statusCode, errorClient.message)
        unauthorized -> createErrorJsonWithMap(unauthorized.statusCode, unauthorized.message)
        errorServer -> createErrorJsonWithMap(errorServer.statusCode, errorServer.message)
        else -> throw IllegalArgumentException("Status tidak valid: $status")
    }

}


private fun <T> createSuccess(data: T, type: ManipulationType): Response<T> {
    return Response.success(200, data)
}

private fun <T> createErrorJsonWithMap(statusCode: Int, message: String): Response<T> {
    val errorJson = mapOf(
        "errors" to mapOf(
            "message" to listOf(message)
        )
    )
    val responseBody = Gson()
        .toJson(errorJson)
        .toResponseBody("application/json".toMediaType())

    return Response.error(statusCode, responseBody)
}

