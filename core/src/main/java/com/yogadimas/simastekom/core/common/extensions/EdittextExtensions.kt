package com.yogadimas.simastekom.core.common.extensions

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.result.ActivityResultLauncher
import com.google.android.material.textfield.TextInputEditText

fun Editable?.getValue(): String? = this?.toString()?.trim()

fun <T> TextInputEditText.setInputLauncher(
    host: Activity,
    key: String,
    activityClass: Class<T>,
    launcher: ActivityResultLauncher<Intent>
) {
    setOnClickListener {
        host.hideKeyBoard()
        val intent = Intent(host, activityClass)
        intent.putExtra(key, true)
        launcher.launch(intent)
    }
}


fun TextInputEditText.setupTextWatchers(
    buttonState: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onTextChanged(p0.toString())
            buttonState()
        }

        override fun afterTextChanged(p0: Editable?) {}
    })
}


