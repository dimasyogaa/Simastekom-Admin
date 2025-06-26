package com.yogadimas.simastekom.core.utils

import android.util.Log
import com.google.gson.Gson
import com.yogadimas.simastekom.core.common.constants.UNKNOWN_ERROR
import com.yogadimas.simastekom.core.common.enums.ErrorType
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.ui.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend fun <ApiResponse : BaseResponse<ApiResponse>> fetchResponse(
    sharedFlow: MutableSharedFlow<UiState<BaseResponse<ApiResponse>>>,
    apiCall: suspend () -> Response<BaseResponse<ApiResponse>>,
    transform: (ApiResponse?) -> ApiResponse,
) {
    handleApiCall(
        sharedFlow = sharedFlow,
        apiCall = apiCall,
        transform = transform
    )
}



suspend fun <ApiResponse> handleApiCall(
    sharedFlow: MutableSharedFlow<UiState<BaseResponse<ApiResponse>>>,
    apiCall: suspend () -> Response<BaseResponse<ApiResponse>>,
    transform: (ApiResponse?) -> ApiResponse
) {
    val TAG = "ApiHandler"

    Log.e(TAG, "Emit Loading state")
    sharedFlow.emit(UiState.Loading)

    val response = runCatching {
        Log.e(TAG, "Calling API...")
        apiCall()
    }

    response.onSuccess { result ->
        Log.e(TAG, "|||||||| API call success: isSuccessful=${result.isSuccessful}")

        if (result.isSuccessful) {
            val body = result.body()
            Log.e(TAG, "|||||||| Response body: $body")

            val code = body?.code
            val messageCode = body?.messageCode
            val data = transform(body?.data)
            Log.e(TAG, "|||||||| Transformed data: $data")

            val wrappedData = BaseResponse(code, messageCode, data)

            Log.e(TAG, "|||||||| Emit Success state")
            sharedFlow.emit(UiState.Success(wrappedData))
        } else {
            val errorBodyString = result.errorBody()?.string().orEmpty()
            Log.e(TAG, "|||||||| API error body: $errorBodyString")

            val errorClient = getErrorsClient(errorBodyString)
            Log.e(TAG, "|||||||| Parsed error client: $errorClient")

            Log.e(TAG, "|||||||| Emit ErrorClient state")
            sharedFlow.emit(UiState.ErrorClient(ErrorClient.toClient(errorClient)))
        }
    }.onFailure { exception ->
        Log.e(TAG, "|||||||| API call failed with exception: ${exception.message}", exception)

        Log.e(TAG, "|||||||| Emit ErrorServer state")
        sharedFlow.emit(UiState.ErrorServer(exception.message.orEmpty()))
    }
}
//
//suspend fun <ApiResponse> handleApiCall(
//    sharedFlow: MutableSharedFlow<UiState<BaseResponse<ApiResponse>>>,
//    apiCall: suspend () -> Response<BaseResponse<ApiResponse>>,
//    transform: (ApiResponse?) -> ApiResponse
//) {
//
//    sharedFlow.emit(UiState.Loading)
//    val response = runCatching { apiCall() }
//
//    response.onSuccess { result ->
//
//        if (result.isSuccessful) {
//            val body = result.body()
//
//            val code = body?.code
//            val messageCode = body?.messageCode
//            val data = transform(body?.data)
//            val wrappedData = BaseResponse(code, messageCode, data)
//
//            sharedFlow.emit(UiState.Success(wrappedData))
//        } else {
//            val errorBodyString = result.errorBody()?.string().orEmpty()
//
//            val errorClient = getErrorsClient(errorBodyString)
//            sharedFlow.emit(UiState.ErrorClient(ErrorClient.toClient(errorClient)))
//        }
//    }.onFailure { exception ->
//        sharedFlow.emit(UiState.ErrorServer(exception.message.orEmpty()))
//    }
//}


fun <ApiResponse> getData(
    response: ApiResponse?,
    extractor: (ApiResponse?) -> ApiResponse?,
    defaultValue: ApiResponse
): ApiResponse {
    return extractor(response) ?: defaultValue
}


//
//suspend fun <T, R> fetchResponse(
//    sharedFlow: MutableSharedFlow<UiState<R>>,
//    apiCall: suspend () -> Response<T>,
//    extractMessageCode: (T?) -> String?,
//    transform: (T?) -> R,
//) {
//    handleApiCall(
//        sharedFlow = sharedFlow,
//        apiCall = apiCall,
//        extractMessageCode = extractMessageCode,
//        transform = transform
//    )
//}
//
//suspend fun <ApiResponse, DomainData> handleApiCall(
//    sharedFlow: MutableSharedFlow<UiState<DomainData>>,
//    apiCall: suspend () -> Response<ApiResponse>,
//    extractMessageCode: ((ApiResponse?) -> String?)? = null,
//    transform: (ApiResponse?) -> DomainData
//) {
//    sharedFlow.emit(UiState.Loading)
//    val response = runCatching { apiCall() }
//
//    response.onSuccess { result ->
//        if (result.isSuccessful) {
//            val body = result.body()
//            val messageCode = extractMessageCode?.invoke(body)
//            val data = transform(body)
//            sharedFlow.emit(UiState.Success(messageCode = messageCode, data = data))
//        } else {
//            val errorResponse = result.errorBody()?.string().orEmpty()
//            val errorClient = getErrorsClient(errorResponse)
//            sharedFlow.emit(UiState.ErrorClient(ErrorClient.toClient(errorClient)))
//        }
//    }.onFailure { exception ->
//        sharedFlow.emit(UiState.ErrorServer(exception.message.orEmpty()))
//    }
//}


data class ErrorClient(
    val code: Int? = null,
    val messageCode: String? = null,
    val message: String? = null,
) {
    companion object {
        fun toClient(input: ErrorClient): ErrorClient {
            val code = when (input.code) {
                ErrorType.SERVER.value -> ErrorType.CLIENT_UNKNOWN.value
                else -> input.code
            }
            return ErrorClient(
                code = code,
                messageCode = input.messageCode,
                message = input.message
            )
        }
    }
}


fun getErrorsClient(response: String): ErrorClient {
    val gson = Gson()
    return try {
        gson.fromJson(response, ErrorClient::class.java)
    } catch (e: Exception) {
        ErrorClient(code = ErrorType.CLIENT_UNKNOWN.value)
    }
}


fun getErrorCode(exception: Exception): Int = getExceptionType(exception).value


private fun getExceptionType(exception: Exception): ErrorType {
    return when (exception) {
        is HttpException -> getCodeHttpException(exception)
        is UnknownHostException, is SocketTimeoutException -> ErrorType.SERVER
        else -> ErrorType.CLIENT_UNKNOWN
    }
}


private fun getCodeHttpException(exception: Exception): ErrorType {
    val errorMessage = exception.localizedMessage ?: UNKNOWN_ERROR

    val parts = errorMessage.split(" ")
    val partsValid = if (parts.size > 1) parts else UNKNOWN_ERROR.split(" ")
    var partString = partsValid[1]

    val code = try {
        partString.toInt()
    } catch (_: Exception) {
        UNKNOWN_ERROR.split(" ")[1].toInt()
    }

    return when (code) {
        ErrorType.UNAUTHORIZED.value -> ErrorType.UNAUTHORIZED
        ErrorType.CLIENT.value -> ErrorType.CLIENT
        else -> ErrorType.CLIENT_UNKNOWN
    }
}


data class CustomPagingSourceException(val code: Int? = null) : Exception()





