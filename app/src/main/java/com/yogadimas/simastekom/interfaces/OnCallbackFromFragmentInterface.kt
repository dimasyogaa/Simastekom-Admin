package com.yogadimas.simastekom.interfaces

interface OnCallbackFromFragmentInterface {
    fun getData(message: String)
    fun getError(message: String, code: Int)
}