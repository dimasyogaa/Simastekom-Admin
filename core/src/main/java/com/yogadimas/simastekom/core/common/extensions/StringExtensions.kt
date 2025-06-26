package com.yogadimas.simastekom.core.common.extensions

import android.R.attr.data
import android.content.Context
import com.yogadimas.simastekom.core.R
import com.yogadimas.simastekom.core.common.enums.MessageCode

private fun String.containsMessageCode(code: MessageCode): Boolean =
    this.contains(code.value, ignoreCase = true)

fun String.dialogMessageSuccess(context: Context, label: String): String? {
    return when {
        containsMessageCode(MessageCode.CREATE_SUCCESS) -> context.getString(
            R.string.text_dialog_added_successfully_format,
            label
        )

        containsMessageCode(MessageCode.UPDATE_SUCCESS) -> context.getString(
            R.string.text_dialog_updated_successfully_format,
            label
        )

        containsMessageCode(MessageCode.DELETE_SUCCESS) -> context.getString(
            R.string.text_dialog_deleted_successfully_format,
            label
        )

        else -> return null
    }
}

