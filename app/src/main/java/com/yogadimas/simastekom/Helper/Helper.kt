package com.yogadimas.simastekom.Helper

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.yogadimas.simastekom.model.Errors
import org.json.JSONObject

fun getErrors(response: String): Errors {

    val gson = Gson()
    val jsonParser = JsonParser()

    val jsonObjectErrors = JSONObject(response)

    val jsonElementErrors = jsonParser.parse(jsonObjectErrors.toString())

    return gson.fromJson(jsonElementErrors, Errors::class.java)
}

 fun showLoading(progressBar: ProgressBar, isLoading: Boolean) {
    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
}

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
