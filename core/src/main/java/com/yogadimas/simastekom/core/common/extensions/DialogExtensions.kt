package com.yogadimas.simastekom.core.common.extensions

import android.R.attr.label
import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yogadimas.simastekom.core.R
import com.yogadimas.simastekom.core.common.customs.DialogAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun DialogAlert.showSuccessDialog(
    context: Context,
    scope: CoroutineScope,
    message: String,
) {
    showDialog(
        icon = ContextCompat.getDrawable(context, R.drawable.z_ic_check_green_core),
        title = context.getString(R.string.text_success),
        message = message,
        actionAuto = {
            scope.launch {
                delay(2100L)
                dismiss()
            }
        }
    )
}

fun DialogAlert.showErrorDialog(
    context: Context,
    title: String,
    message: String,
    actionButton: MaterialAlertDialogBuilder.() -> Unit,
) {
    showDialog(
        icon = ContextCompat.getDrawable(context, R.drawable.z_ic_warning_core),
        title = title,
        message = message,
        actionButton = actionButton
    )
}


fun DialogAlert.showConfirmDeleteDialog(
    context: Context,
    deleteAction: () -> Unit,
) {
    showDialog(
        icon = ContextCompat.getDrawable(context, R.drawable.z_ic_delete_red_core),
        title = context.getString(R.string.text_delete),
        message = context.getString(R.string.text_confirm_deletion),
        actionButton = {
            setPositiveButton(context.getString(R.string.text_ok)) { _, _ -> deleteAction() }
            setNegativeButton(context.getString(R.string.text_cancel)) { _, _ -> dismiss() }
        }
    )
}