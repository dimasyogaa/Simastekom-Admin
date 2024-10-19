package com.yogadimas.simastekom.common.helper

import android.content.Context
import android.util.Log
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.enums.SpecialCharacter

class PhoneNumberValidationHelper(
    val context: Context,
    val layout: TextInputLayout,
    editText: EditText,
) {

    private var phoneNumber: String = ""

    init {
        phoneNumber = editText.text.toString()
    }

    fun isValid(): Boolean {
        if (!isValidFormatPhoneNumber().first) {
            layout.isErrorEnabled = true
            layout.error = isValidFormatPhoneNumber().second
        } else {
            layout.error = null
            layout.isErrorEnabled = false
            return true
        }

        return false
    }

    private fun isValidFormatPhoneNumber(): Pair<Boolean, String?> {
        if (phoneNumber.contains(" ")) {
            return Pair(
                false,
                context.getString(
                    R.string.text_cannot_contain_spaces,
                    context.getString(R.string.text_label_phone)
                )
            )
        }

        val unwantedChars = SpecialCharacter.entries
        for (char in unwantedChars) {
            if (phoneNumber.contains(char.symbol)) {
                return Pair(
                    false,
                    context.getString(
                        R.string.text_error_format,
                        context.getString(R.string.text_label_phone),
                        context.getString(R.string.text_sign, char.symbol)
                    )
                )
            }
        }
        return Pair(true, null)
    }


}