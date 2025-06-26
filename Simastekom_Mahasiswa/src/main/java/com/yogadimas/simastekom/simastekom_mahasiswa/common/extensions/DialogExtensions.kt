package com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions

import android.content.Context
import com.yogadimas.simastekom.core.R
import com.yogadimas.simastekom.core.common.customs.DialogAlert
import com.yogadimas.simastekom.core.common.extensions.showErrorDialog

 fun DialogAlert.showUnknownDialog(
    context: Context
) {
    showErrorDialog(
        context,
        context.getString(R.string.text_something_went_wrong),
        context.getString(R.string.text_request_failed),
        actionButton = {
            setPositiveButton(context.getString(R.string.text_ok)) { _, _ -> dismiss() }
        })
}


fun DialogAlert.showUnAuthorizedDialog(context: Context, action: () -> Unit) {
    showErrorDialog(
        context,
        context.getString(R.string.text_login_again),
        context.getString(R.string.text_please_login_again),
        actionButton = {
            setPositiveButton(context.getString(R.string.text_ok)) { _, _ ->
                action()
                context.navigateToLogin()
            }
        }
    )
}



