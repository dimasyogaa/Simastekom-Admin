package com.yogadimas.simastekom.common.enums

enum class ErrorMessage(val value: String) {
    REGISTERED("sudah didaftarkan"),
    UNREGISTERED("tidak terdaftar"),
    USED("sudah digunakan"),
    UNAUTHORIZED("unauthorized"),
    ERROR_CLIENT("Client Error"),
    ERROR_SERVER("Server Error"),
}