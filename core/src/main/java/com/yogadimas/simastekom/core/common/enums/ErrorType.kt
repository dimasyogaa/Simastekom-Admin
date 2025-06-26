package com.yogadimas.simastekom.core.common.enums

enum class ErrorType(val value: Int) {
    SERVER(500),
    CLIENT(400),
    CLIENT_UNKNOWN(450),
    UNAUTHORIZED(401),
}