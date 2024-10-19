package com.yogadimas.simastekom.common.helper

import android.view.View

fun animateFade(view: View, boolean: Boolean){
    view.apply {
        if (boolean) {
            animate().alpha(1.0f).setDuration(600)
        } else {
            animate().alpha(0.0f).setDuration(600)
        }
    }
}