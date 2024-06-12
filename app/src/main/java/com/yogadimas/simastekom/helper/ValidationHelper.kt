package com.yogadimas.simastekom.helper

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class OnTextChanged(private val inputLayout: TextInputLayout) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // Kosongkan karena tidak ada tindakan yang diambil sebelum teks berubah
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString().isNotEmpty()) {
            inputLayout.error = null
            inputLayout.isErrorEnabled = false
        }
    }

    override fun afterTextChanged(s: Editable?) {
        // Kosongkan karena tidak ada tindakan yang diambil setelah teks berubah
    }
}


fun onTextChange(edt: TextInputEditText, layout: TextInputLayout) {
    edt.addTextChangedListener(OnTextChanged(layout))
}

var newPasswordHelper: String = ""
var newConfirmPasswordHelper: String = ""


fun minCharacterPasswordValidation(
    edtPassword: EditText,
    password: String,
    layout: TextInputLayout,
    message: String,
    min: Int,
    started: Boolean,
    layoutOther: TextInputLayout? = null,
): Boolean {

    edtPassword.addTextChangedListener(object : TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


        }

        override fun afterTextChanged(p0: Editable?) {
            minCharacterPasswordValidation(p0.toString(), layout, message, min)
            if (layoutOther != null &&
                newConfirmPasswordHelper.isNotEmpty()
            ) {
                if (newConfirmPasswordHelper != newPasswordHelper) {
                    newPasswordSameWithNewConfirmPassword(
                        p0.toString(),
                        newConfirmPasswordHelper,
                        layoutOther
                    )
                } else {
                    newPasswordSameWithNewConfirmPassword(
                        p0.toString(),
                        newConfirmPasswordHelper,
                        layoutOther
                    )
                }

            }

        }
    })

    if (started) return true

    return minCharacterPasswordValidation(password, layout, message, min, true)
}


fun minCharacterPasswordValidation(
    password: String,
    layout: TextInputLayout,
    message: String,
    min: Int,
    save: Boolean = false,
): Boolean {

    var isValid = true
    if (password.isNotEmpty() &&
        password.length >= min
    ) {
        layout.error = null
        layout.isErrorEnabled = false
        isValid = true
        newPasswordHelper = password
    } else if (password.isEmpty() && !save) {
        layout.error = null
        layout.isErrorEnabled = false
    } else if (password.isEmpty() && save) {
        layout.isErrorEnabled = true
        layout.error = message
        isValid = false
    } else {
        layout.isErrorEnabled = true
        layout.error = message
        isValid = false
    }
    return isValid
}


fun newPasswordSameWithNewConfirmPassword(
    edtPassword: EditText,
    password: String,
    layout: TextInputLayout,
    started: Boolean,
): Boolean {
    edtPassword.addTextChangedListener(object : TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            if (newPasswordHelper == newConfirmPasswordHelper) {
                newPasswordSameWithNewConfirmPassword(newPasswordHelper, p0.toString(), layout)
            } else {
                newPasswordSameWithNewConfirmPassword(newPasswordHelper, p0.toString(), layout)
            }
        }

    })

    if (started) return true

    return newPasswordSameWithNewConfirmPassword(newPasswordHelper, password, layout, true)
}

fun newPasswordSameWithNewConfirmPassword(
    newPassword: String,
    password: String,
    layout: TextInputLayout,
    save: Boolean = false,
): Boolean {
    newConfirmPasswordHelper = password
    val isValid: Boolean
    if (password.isNotEmpty()) {
        if (password == newPassword) {
            layout.error = null
            layout.isErrorEnabled = false
            layout.isHelperTextEnabled = true
            layout.helperText =
                "Konfirmasi Password Baru sesuai dengan Password Baru yang anda masukkan"
            isValid = true
        } else {
            layout.isErrorEnabled = true
            layout.error =
                "Pastikan Konfirmasi Password Baru sesuai dengan Password Baru yang anda masukkan"
            isValid = false
        }


    } else if (password.isEmpty() && !save) {
        layout.error = null
        layout.isErrorEnabled = false
        layout.helperText = null
        layout.isHelperTextEnabled = false
        isValid = false
    } else if (password.isEmpty() && save) {
        layout.isErrorEnabled = true
        layout.error =
            "Pastikan Konfirmasi Password Baru sesuai dengan Password Baru yang anda masukkan sebelumnya"
        isValid = false
    } else {
        layout.error = null
        layout.isErrorEnabled = false
        layout.helperText = null
        layout.isHelperTextEnabled = false
        isValid = false
    }

    return isValid
}


val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun isValidFormatEmail(str: String): Boolean {
    return EMAIL_ADDRESS_PATTERN.matcher(str).matches()
}

fun isContainsSpace(text: CharSequence): Boolean {
    return text.contains(" ")
}