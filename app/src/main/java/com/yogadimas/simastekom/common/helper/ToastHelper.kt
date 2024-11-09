package com.yogadimas.simastekom.common.helper

import android.app.Activity
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast

object ToastHelper {
    private var currentToast: Toast? = null

    fun showCustomToast(activity: Activity, message: String) {
        // Batalkan Toast sebelumnya jika ada
        currentToast?.cancel()

        // Buat Toast baru
        val toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT)
        currentToast = toast // Simpan referensi ke Toast yang sekarang
        toast.show()

        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)

        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val heightDiff = rootView.height - (rect.bottom - rect.top)

                // Cek apakah keyboard terbuka
                if (heightDiff > 200) {
                    currentToast?.cancel() // Batalkan Toast jika keyboard terbuka
                    currentToast = null // Reset referensi Toast

                    // Hapus listener untuk mencegah kebocoran memori
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }
}
