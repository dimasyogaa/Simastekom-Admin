package com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions

import android.content.Context
import android.content.Intent
import com.yogadimas.simastekom.ui.login.LoginActivity

fun Context.navigateToLogin() {
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}





