package com.yogadimas.simastekom.common.helper

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar

class SnackBarHelper private constructor() : DefaultLifecycleObserver {

    private var snackBar: Snackbar? = null
    private var lifecycleOwner: LifecycleOwner? = null

    companion object {
        @Volatile
        private var INSTANCE: SnackBarHelper? = null

        fun getInstance(): SnackBarHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SnackBarHelper().also { INSTANCE = it }
            }
        }

        fun display(
            viewGroup: ViewGroup,
            message: String,
            lifecycleOwner: LifecycleOwner,
            anchorView: View? = null
        ) {
            getInstance().displaySnackBar(viewGroup, message, lifecycleOwner, anchorView)
        }
    }

    private fun displaySnackBar(
        viewGroup: ViewGroup,
        message: String,
        lifecycleOwner: LifecycleOwner,
        anchorView: View? = null
    ) {
        if (this.lifecycleOwner == null) {
            this.lifecycleOwner = lifecycleOwner
            lifecycleOwner.lifecycle.addObserver(this)
        }

        try {
            snackBar?.dismiss() // Dismiss existing snackbar if it's still showing
            snackBar = Snackbar.make(
                viewGroup,
                message,
                Snackbar.LENGTH_LONG
            ).apply {
                anchorView?.let { setAnchorView(it) } // Set the anchor view if provided
            }
            snackBar?.show()
        } catch (_: Exception) { }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        dismiss()
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = null // Clear the reference to avoid memory leaks
    }

    fun dismiss() {
        snackBar?.dismiss()
        snackBar = null
    }
}






