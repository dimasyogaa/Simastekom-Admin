package com.yogadimas.simastekom.ui.password

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityResetPasswordEditBinding
import com.yogadimas.simastekom.helper.dataString
import com.yogadimas.simastekom.helper.minCharacterPasswordValidation
import com.yogadimas.simastekom.helper.newPasswordSameWithNewConfirmPassword
import com.yogadimas.simastekom.helper.showLoading
import com.yogadimas.simastekom.viewmodel.forgotpassword.ForgotPasswordViewModel

class ResetPasswordEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordEditBinding

    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()

    private var hashPassword: String = ""
    private var newPasswordTemp: String = ""

    private val digits = 6

    private lateinit var userId: String
    private lateinit var userType: String

    private lateinit var newEdtPassword: TextInputEditText
    private lateinit var newPassword: String
    private lateinit var newLayoutPassword: TextInputLayout
    private lateinit var newMessage: String

    private lateinit var newConfirmEdtPassword: TextInputEditText
    private lateinit var newConfirmPassword: String
    private lateinit var newConfirmLayoutPassword: TextInputLayout

    private var dialogHasBeenShow = false
    private var isSuccessDialogShowingOrientation = false

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            userId = bundle.getString(KEY_USER_ID).orEmpty()
            userType = bundle.getString(KEY_USER_TYPE).orEmpty()
        }

        if (savedInstanceState != null) {
            isSuccessDialogShowingOrientation = savedInstanceState.getBoolean(
                KEY_SUCCESS_DIALOG_SHOWING
            )
            if (isSuccessDialogShowingOrientation) {
                showAlertDialog(status = STATUS_SUCCESS)
            }
        }

        isValid(true)

        getUser()

        binding.btnCancel.setOnClickListener { finish() }

        binding.btnOk.setOnClickListener { savePassword(it) }
    }

    private fun getUser() {
        forgotPasswordViewModel.userData.observe(this) {
            hashPassword = it.password.orEmpty()
            if (it.isUpdated && verifyPassword(newPasswordTemp)) {
                isSuccessDialogShowingOrientation = true
                showAlertDialog(status = STATUS_SUCCESS)
                showLoading(false)
            }

        }

        forgotPasswordViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val listMessage = data.errors.message.orEmpty()
                    showAlertDialog(listMessage[0], STATUS_ERROR)
                    showLoading(false)
                }
            }
        }

        forgotPasswordViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(
                    binding.root as ViewGroup,
                    snackBarText,
                    Snackbar.LENGTH_SHORT
                ).show()
                showLoading(false)
            }
        }
    }

    private fun isValid(started: Boolean): Boolean {

        fun checkNewPassword(
            edtPassword: TextInputEditText,
            password: String,
            layoutPassword: TextInputLayout,
            message: String,
            started: Boolean,
            layoutOther: TextInputLayout? = null,
        ): Boolean {

            return minCharacterPasswordValidation(
                edtPassword,
                password,
                layoutPassword,
                message,
                digits,
                started,
                layoutOther
            )


        }

        fun checkNewConfirmPassword(
            newConfirmEdtPassword: TextInputEditText,
            newConfirmPassword: String,
            newConfirmLayoutPassword: TextInputLayout,
            started: Boolean,
        ): Boolean {

            return newPasswordSameWithNewConfirmPassword(
                newConfirmEdtPassword,
                newConfirmPassword,
                newConfirmLayoutPassword,
                started
            )

        }

        fun stringFormat(string: String): String {
            return String.format(getString(R.string.min_character_field), string, digits)
        }


        newEdtPassword = binding.edtNewPassword
        newPassword = newEdtPassword.text.dataString()
        newLayoutPassword = binding.inputLayoutNewPassword
        newMessage = stringFormat(getString(R.string.text_label_new_password))

        newConfirmEdtPassword = binding.edtConfirmNewPassword
        newConfirmPassword = newConfirmEdtPassword.text.dataString()
        newConfirmLayoutPassword = binding.inputLayoutConfirmNewPassword

        var checkNewConfirmPassword = false

        val checkNewPassword: Boolean = checkNewPassword(
            newEdtPassword,
            newPassword,
            newLayoutPassword,
            newMessage,
            started,
            newConfirmLayoutPassword
        )
        if (checkNewPassword) {
            checkNewConfirmPassword = checkNewConfirmPassword(
                newConfirmEdtPassword, newConfirmPassword, newConfirmLayoutPassword, started
            )
        }



        return checkNewPassword && checkNewConfirmPassword

    }

    private fun savePassword(view: View) {
        if (isValid(false)) {
            hideKeyboard(view)
            showLoading(true)
            newPasswordTemp = binding.edtConfirmNewPassword.text.toString()
            forgotPasswordViewModel.updatePassword(userId, userType, newPasswordTemp)
        }
    }

    private fun verifyPassword(plainPassword: String): Boolean {
        return BCrypt.verifyer()
            .verify(plainPassword.toCharArray(), hashPassword.toCharArray()).verified
    }

    private fun showAlertDialog(msg: String = "", status: String) {

        var title = ""
        var message = msg
        var icon: Drawable? = null
        when (status) {
            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(this, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = getString(R.string.text_alert_create_new_password, title)
            }

            STATUS_ERROR -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                title = getString(R.string.text_error, getString(R.string.text_label_new_password))
            }
        }

        if (dialog == null) {
            dialog = MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setIcon(icon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    dialogHasBeenShow = false
                    dialog = null
                    when (status) {
                        STATUS_SUCCESS -> {
                            isSuccessDialogShowingOrientation = false
                            finish()
                            return@setPositiveButton
                        }

                        STATUS_ERROR -> {
                            return@setPositiveButton
                        }
                    }
                }.create()
        }


        if (!dialogHasBeenShow) {
            dialogHasBeenShow = true
            dialog?.show()
        }


    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showLoading(boolean: Boolean) {
        showLoading(binding.progressBar, boolean)
        if (boolean) {
            binding.btnCancel.visibility = View.GONE
            binding.btnOk.visibility = View.GONE
        } else {
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnOk.visibility = View.VISIBLE
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_SUCCESS_DIALOG_SHOWING, isSuccessDialogShowingOrientation)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            dialogHasBeenShow = false
            dialog?.dismiss();
            dialog = null;
        }
    }


    companion object {
        const val KEY_USER_ID = "key_user_id"
        const val KEY_USER_TYPE = "key_user_type"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"
    }
}