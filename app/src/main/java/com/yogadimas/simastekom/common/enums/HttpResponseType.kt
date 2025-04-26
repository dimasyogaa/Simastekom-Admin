package com.yogadimas.simastekom.common.enums

enum class HttpResponseType(val statusCode: Int, val message: String) {
    SUCCESS(200, "success"),
    ERROR_CLIENT(400,"error client"),
    UNAUTHORIZED(401,"unauthorized"),
    ERROR_SERVER(500,"error server")
}