package com.yogadimas.simastekom.common.enums

enum class ErrorCode(val value: String) {
    SERVER("500"),
    CLIENT("400"),
    UNAUTHORIZED("401"),
}