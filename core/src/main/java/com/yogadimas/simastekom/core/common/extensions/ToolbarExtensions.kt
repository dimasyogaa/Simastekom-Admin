package com.yogadimas.simastekom.core.common.extensions

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import com.yogadimas.simastekom.core.R

fun Toolbar.setManipulation(context: Activity, isCreatedMode: Boolean) = apply {
    title =
        if (isCreatedMode) context.getString(R.string.text_add) else context.getString(R.string.text_change_or_delete)
    setNavigationOnClickListener { context.finish() }
}
