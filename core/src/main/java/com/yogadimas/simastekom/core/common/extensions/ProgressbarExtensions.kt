package com.yogadimas.simastekom.core.common.extensions

import android.widget.ProgressBar
import androidx.core.view.isVisible

fun ProgressBar.showLoadingFade(isLoading: Boolean) {
    if (isLoading) {
        isVisible = true
        animate().alpha(1.0f).setDuration(300)
    } else {
        alpha = 0f
        isVisible = false
    }
}