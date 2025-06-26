package com.yogadimas.simastekom.core.common.customs

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

class DialogAlert(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
) : DefaultLifecycleObserver {

    private val contextRef = WeakReference(context)
    private var alertDialog: AlertDialog? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun showDialog(
        theme: Int? = null,
        icon: Drawable? = null,
        title: String? = null,
        message: String? = null,
        view: Int? = null,
        layoutWidth: Int? = null,
        layoutHeight: Int? = null,
        cancelable: Boolean = false,
        actionAuto: () -> Unit = {},
        actionButton: MaterialAlertDialogBuilder.() -> Unit = {},
    ) {
        dismiss()
        val context = contextRef.get() ?: return
        val activity = context as? Activity ?: return

        // Hindari window leak
        if (activity.isFinishing || activity.isDestroyed) return
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) return

        actionAuto()

        val builder = if (theme != null)
            MaterialAlertDialogBuilder(context, theme)
        else
            MaterialAlertDialogBuilder(context)

        builder.setCancelable(cancelable)
        icon?.let { builder.setIcon(it) }
        title?.let { builder.setTitle(it) }
        message?.let { builder.setMessage(it) }
        view?.let { builder.setView(it) }

        builder.actionButton()


        alertDialog = builder.create().apply {
            show()
            if (layoutWidth != null && layoutHeight != null) {
                window?.setLayout(layoutWidth, layoutHeight)
            }
        }


    }

    fun dismiss() {
        alertDialog?.takeIf { it.isShowing }?.dismiss()
        alertDialog = null
    }

    // Hapus dialog saat lifecycle selesai (aman dari leak)
    override fun onDestroy(owner: LifecycleOwner) {
        dismiss()
        lifecycleOwner.lifecycle.removeObserver(this)
    }




}









