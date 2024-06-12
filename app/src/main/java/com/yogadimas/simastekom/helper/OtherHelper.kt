package com.yogadimas.simastekom.helper

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.model.Address
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


fun showLoading(progressBar: ProgressBar, isLoading: Boolean) {
    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

}

fun Editable?.dataString(): String = this.toString().trim()

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}


fun Address.getFormattedString(): String {
    return "$province,$cityRegency,$district,$village,$rw,$rt,$street,$otherDetailAddress"
}


fun isLandscape(): Boolean {
    return Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun setBold(sentence: String, keywords: List<String>, context: Context? = null): SpannableString {
    val spannableString = SpannableString(sentence)

    val setBoldSpan = { startIndex: Int, endIndex: Int ->
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    val setPrimarySpan = { startIndex: Int, endIndex: Int ->
        if (context != null) {
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.md_theme_primary)),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    for (keyword in keywords) {
        val startIndex = sentence.indexOf(keyword)
        if (startIndex != -1) {
            val endIndex = startIndex + keyword.length
            setBoldSpan(startIndex, endIndex)
            setPrimarySpan(startIndex, endIndex)
        }
    }
    return spannableString
}

fun String.capitalizeWords(delimiter: String = " ") = split(delimiter).joinToString(delimiter) { word -> word.lowercase().replaceFirstChar(Char::titlecaseChar)}

fun simpleDateFormatHelper() = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }


