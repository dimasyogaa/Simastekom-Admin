package com.yogadimas.simastekom.core.common.extensions

import android.content.Intent
import android.os.Build
import android.os.Parcelable

inline fun <reified T : Parcelable> Intent.getParcelableData(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        getParcelableExtra(key)
    }
}


fun Intent.getIntData(key: String): Int = getIntExtra(key, 0)
fun Intent.getStringData(key: String): String = getStringExtra(key).orEmpty()
fun Intent.getBooleanData(key: String): Boolean = getBooleanExtra(key, false)


