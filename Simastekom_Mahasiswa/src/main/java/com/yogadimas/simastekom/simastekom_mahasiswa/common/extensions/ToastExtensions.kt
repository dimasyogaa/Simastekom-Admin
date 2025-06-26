package com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions

import android.app.Activity
import com.yogadimas.simastekom.core.R
import com.yogadimas.simastekom.core.common.customs.ToastHelper

fun Activity.showToastErrorServer() {
    ToastHelper.show(this, getString(R.string.text_there_is_an_error_on_server))
}

fun Activity.showToast(message: String) {
    ToastHelper.show(this, message)
}