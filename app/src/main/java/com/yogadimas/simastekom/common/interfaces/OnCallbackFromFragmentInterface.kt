package com.yogadimas.simastekom.common.interfaces

import com.yogadimas.simastekom.common.enums.ErrorCode

interface OnCallbackFromFragmentInterface {
    fun getData(message: String)
    fun getError(message: String, code: ErrorCode)
}