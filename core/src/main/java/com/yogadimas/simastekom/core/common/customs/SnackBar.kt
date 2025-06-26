package com.yogadimas.simastekom.core.common.customs

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar

class SnackBar private constructor() : DefaultLifecycleObserver {

    private var snackBar: Snackbar? = null
    private var lifecycleOwner: LifecycleOwner? = null

    companion object {
        @Volatile
        private var INSTANCE: SnackBar? = null

        fun getInstance(): SnackBar {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SnackBar().also { INSTANCE = it }
            }
        }

        fun display(
            viewGroup: ViewGroup,
            message: String,
            duration: Int = Snackbar.LENGTH_LONG,
            lifecycleOwner: LifecycleOwner,
            anchorView: View? = null,
        ) {
            getInstance().displaySnackBar(viewGroup, message, duration, lifecycleOwner, anchorView)
        }
    }

    private fun displaySnackBar(
        viewGroup: ViewGroup,
        message: String,
        duration: Int,
        lifecycleOwner: LifecycleOwner,
        anchorView: View?,
    ) {
        if (this.lifecycleOwner == null) {
            this.lifecycleOwner = lifecycleOwner
            lifecycleOwner.lifecycle.addObserver(this)
        }

        try {
            snackBar?.dismiss()
            snackBar = Snackbar.make(
                viewGroup,
                message,
                duration
            ).apply {
                anchorView?.let { setAnchorView(it) }
            }
            snackBar?.show()
        } catch (_: Exception) {
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        dismiss()
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = null
    }

    fun dismiss() {
        snackBar?.dismiss()
        snackBar = null
    }
}






