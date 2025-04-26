package com.yogadimas.simastekom.common.helper

import android.view.View
import androidx.collection.emptyObjectList
import androidx.viewbinding.ViewBinding

fun animateFade(view: View, boolean: Boolean){
    view.apply {
        if (boolean) {
            animate().alpha(1.0f).setDuration(600)
        } else {
            animate().alpha(0.0f).setDuration(600)
        }
    }
}


fun <T : ViewBinding?> animateViewStub(binding: T) {
    // Mengatur alpha dan translationY
    binding?.root?.alpha = 0f
    binding?.root?.translationY = -100f

    // Memulai animasi
    binding?.root?.animate()
        ?.alpha(1f)
        ?.translationY(0f)
        ?.setDuration(210)
        ?.start()
}
