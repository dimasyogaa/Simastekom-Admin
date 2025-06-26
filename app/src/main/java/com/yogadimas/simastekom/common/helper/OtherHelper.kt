package com.yogadimas.simastekom.common.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.enums.Phone
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.common.enums.SpecialCharacter
import com.yogadimas.simastekom.common.enums.Str
import com.yogadimas.simastekom.model.backup.Address
import com.yogadimas.simastekom.model.responses.UserCurrent
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.mainpage.profile.ProfileFragment
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


fun showLoading(progressBar: ProgressBar, isLoading: Boolean) {
    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
}
fun showLoadingFade(progressBar: ProgressBar, isLoading: Boolean) {
    progressBar.apply {
        if (isLoading) {
            isVisible = true
            animate().alpha(1.0f).setDuration(300)
        } else {
            alpha = 0f
            isVisible = false
        }

    }
}









fun Editable?.dataString(): String = this.toString().trim()
fun String?.setStripIfNull(): String {
    return if (!this.isNullOrEmpty()) this else SpecialCharacter.STRIP.symbol.toString()
}




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

fun formatDataMaterialTextview(label: String, value: String, context: Context): SpannableString {
    return if (value.isNotEmpty()) {
        setBold(
            context.getString(R.string.text_placeholder_value_is_not_empty_format, label, value),
            listOf(value),

            )
    } else {
        SpannableString(label)
    }

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

fun String.capitalizeWords(delimiter: String = " ") =
    split(delimiter).joinToString(delimiter) { word ->
        word.lowercase().replaceFirstChar(Char::titlecaseChar)
    }

fun simpleDateFormatHelper() = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun goToLogin(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
}
fun goToProfileFragment(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent.putExtra(MainActivity.KEY_PAGE, ProfileFragment.NAME_FRAGMENT)
    context.startActivity(intent)
}
fun Context.movePageWithParcelable( key: String, destination: Class<*>?, data: Parcelable?) {
    startActivity( Intent(this, destination).apply {putExtra(key, data)})
}

inline fun <reified T : Parcelable> getParcelableExtra(data: Intent, key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        data.getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        data.getParcelableExtra(key)
    }
}
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        getParcelable(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}

fun sendMessage(
    userCurrent: UserCurrent = UserCurrent(),
    receiverPhoneNumber: String? = null,
    emailAddress: String? = null,
    receiverRole: Role? = null,
    receiverName: String? = null,
    receiverLectureGender: String? = null,
    context: Context,
    isWhatsApp: Boolean = true,
) {
    context.apply {
        val man = context.getString(R.string.text_man).lowercase()
        val woman = context.getString(R.string.text_woman).lowercase()
        val setAdmin: (String?) -> String =
            { name -> getString(R.string.text_admin_name_format, name) }
        val setLecture: (String?) -> String =
            { name -> getString(R.string.text_lecturer_name_format, name) }
        val setStudent: (String?) -> String =
            { name -> getString(R.string.text_student_name_format, name) }
        val setStudentParent: (String?) -> String =
            { name -> getString(R.string.text_student_name_parent_format, name) }
        val misterLecture = getString(R.string.text_lecturer_mister_format, receiverName)
        val madamLecture = getString(R.string.text_lecturer_madam_format, receiverName)
        val technician = getString(R.string.text_technician)
        val technicianNumber = Phone.TECHNICIAN.number
        val emptyString = Str.EMPTY.value

        val userCurrentRole = userCurrent.userType
        val userCurrentName = userCurrent.name
        val userCurrentIdentity = userCurrent.identity


        val roleIntroduction: String = when (userCurrentRole) {
            Role.ADMIN.value -> setAdmin(userCurrentName)
            Role.LECTURE.value -> setLecture(userCurrentName)
            Role.STUDENT.value -> setStudent(userCurrentName)
            else -> emptyString
        }

        val receiverGreeting = when (receiverRole) {
            Role.ADMIN -> setAdmin(receiverName)
            Role.STUDENT -> setStudent(receiverName)
            Role.LECTURE -> when (receiverLectureGender) {
                man -> misterLecture
                woman -> madamLecture
                null -> setLecture(receiverName)
                else -> setLecture(receiverName)
            }
            Role.PARENT -> setStudentParent(receiverName)

            null -> technician
        }

        val identityInfo = if (userCurrentIdentity != null)
            getString(R.string.text_with_identity_format, userCurrent.identity) else emptyString
        val message =
            getString(
                R.string.text_message_send_message_format,
                getTimeOfDayCurrent(this),
                receiverGreeting,
                roleIntroduction,
                identityInfo
            )


        if (isWhatsApp) {
            sendWhatsApp(message, receiverPhoneNumber ?: technicianNumber, this)
        } else {
            val senderRole: Role? = Role.entries.find { it.value == userCurrentRole }
            sendEmail(message, emailAddress.orEmpty(), senderRole, userCurrentName.orEmpty(), this)
        }
    }
}

private fun sendWhatsApp(message: String, receiverPhoneNumber: String, context: Context) {

    val url = "https://api.whatsapp.com/send?phone=${formatPhoneNumber(receiverPhoneNumber)}&text=${
        URLEncoder.encode(message, "UTF-8")
    }"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(webIntent)
    }
}

private fun sendEmail(
    message: String,
    emailAddress: String,
    senderRole: Role?,
    senderName: String,
    context: Context,
) {
    context.apply {
        val simastekom = "SIMASTEKOM"
        val admin = getString(R.string.text_admin)
        val lecture = getString(R.string.text_lecturer)
        val student = getString(R.string.text_student)

        val mailTo = Str.MAILTO.value
        val setSubject: (String, String) -> String =
            { role, name -> "$simastekom | ${role.uppercase()} - $name" }
        val subject = when (senderRole) {
            Role.ADMIN -> setSubject(admin, senderName)
            Role.LECTURE -> setSubject(lecture, senderName)
            Role.STUDENT -> setSubject(student, senderName)
            else -> simastekom
        }


        val uriText = "$mailTo${Uri.encode(emailAddress)}" +
                "?subject=${Uri.encode(subject)}" +
                "&body=${Uri.encode(message)}"
        val mailUri: Uri = Uri.parse(uriText)

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = mailUri
        }


        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            val webIntent = Intent(Intent.ACTION_VIEW, mailUri)
            startActivity(webIntent)
        }
    }
}


private fun getTimeOfDayCurrent(context: Context): String {
    val hour =
        SimpleDateFormat("HH", Locale.getDefault()).format(Calendar.getInstance().time).toInt()
    return when (hour) {
        in 6..11 -> context.getString(R.string.text_time_morning)
        in 12..16 -> context.getString(R.string.text_time_afternoon)
        in 17..18 -> context.getString(R.string.text_time_Evening)
        else -> context.getString(R.string.text_time_NIght)
    }
}


private fun formatPhoneNumber(input: String): String {
    // Menghapus semua spasi atau karakter non-digit selain tanda +
    val cleanInput = input.replace(Regex("[^+\\d]"), "")

    return when {
        // Jika nomor dimulai dengan 0, ubah menjadi format 62 (untuk Indonesia)
        cleanInput.startsWith("0") -> {
            cleanInput.replaceFirst("0", "62")
        }
        // Jika nomor sudah diawali dengan +, hapus tanda + untuk format internasional
        cleanInput.startsWith("+") -> {
            cleanInput.removePrefix("+")
        }

        else -> cleanInput // Jika format sudah benar, kembalikan nomor aslinya
    }
}

 fun colorToHex(color: Int): String {
    return String.format("#%06X", 0xFFFFFF and color)
}




