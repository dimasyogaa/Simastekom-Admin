package com.yogadimas.simastekom.common.helper

import android.app.Activity
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast

object ToastHelper {
    fun showCustomToast(activity: Activity, message: String) {
        val toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT)

        toast.show()

        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val heightDiff = rootView.height - (rect.bottom - rect.top)

                // Check if keyboard is opened
                if (heightDiff > 200) {
                    toast.cancel() // Cancel the toast if keyboard is open
                    rootView.viewTreeObserver.removeOnGlobalLayoutListener(this) // Remove listener to prevent memory leaks
                }
            }
        })
    }
}