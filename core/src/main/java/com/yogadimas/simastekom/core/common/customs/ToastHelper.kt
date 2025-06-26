package com.yogadimas.simastekom.core.common.customs


import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast

object ToastHelper {
    private var currentToast: Toast? = null
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    fun show(activity: Activity, message: String) {
        // Batalkan Toast sebelumnya
        currentToast?.cancel()

        val toast = Toast.makeText(activity.applicationContext, message, Toast.LENGTH_SHORT)
        currentToast = toast
        toast.show()

        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)

        // Bersihkan listener lama jika masih ada
        globalLayoutListener?.let {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }

        // Buat listener baru
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val heightDiff = rootView.height - (rect.bottom - rect.top)

                // Keyboard terbuka? Batalkan Toast
                if (heightDiff > 200) {
                    currentToast?.cancel()
                    currentToast = null

                    // Hapus listener untuk mencegah leak
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    globalLayoutListener = null
                }
            }
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(listener)
        globalLayoutListener = listener
    }

    fun dismiss() {
        currentToast?.cancel()
        currentToast = null
    }
}
