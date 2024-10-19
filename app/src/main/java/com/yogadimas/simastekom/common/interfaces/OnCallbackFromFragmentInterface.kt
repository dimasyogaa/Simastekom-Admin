package com.yogadimas.simastekom.common.interfaces

interface OnCallbackFromFragmentInterface {
    fun getData(message: String)
    fun getError(message: String, code: Int)
}