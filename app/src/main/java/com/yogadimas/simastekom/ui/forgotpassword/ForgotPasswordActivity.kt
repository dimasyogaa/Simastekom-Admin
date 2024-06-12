package com.yogadimas.simastekom.ui.forgotpassword

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityForgotPasswordBinding
import com.yogadimas.simastekom.helper.isValidFormatEmail
import com.yogadimas.simastekom.helper.onTextChange
import com.yogadimas.simastekom.helper.showLoading
import com.yogadimas.simastekom.ui.identity.personal.email.EmailActivity
import com.yogadimas.simastekom.ui.password.ResetPasswordEditActivity
import com.yogadimas.simastekom.viewmodel.forgotpassword.ForgotPasswordViewModel

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()

    private var dialogHasBeenShow = false

    private var email: String? = null
    private var token: String? = null
    private var isValidEmail: Boolean = false
    private var isValidToken: Boolean = false

    private val digits = 6

    private var dialogAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.edtToken.transformationMethod = null

        if (savedInstanceState != null) {
            email = savedInstanceState.getString(KEY_EMAIL_VALID)
        }

        emailView(true)


    }

    private fun emailView(isVisible: Boolean = false) {
        binding.apply {
            if (isVisible) {
                tvInstructionTokenResetPassword1.visibility = View.GONE
                tvInstructionTokenResetPasswordEmail.visibility = View.GONE
                tvInstructionTokenResetPassword2.visibility = View.GONE
                inputLayoutToken.visibility = View.GONE
                edtToken.visibility = View.GONE
                btnVerifyToken.visibility = View.GONE
                tvTitleForgotPassword.visibility = View.VISIBLE
                cvInstruction.visibility = View.VISIBLE
                inputLayoutEmail.visibility = View.VISIBLE
                edtEmail.visibility = View.VISIBLE
                btnGetTokenViaEmail.visibility = View.VISIBLE
                onTextChange(edtEmail, inputLayoutEmail)
                emailLogic()
            } else {
                tvTitleForgotPassword.visibility = View.GONE
                cvInstruction.visibility = View.GONE
                inputLayoutEmail.visibility = View.GONE
                edtEmail.visibility = View.GONE
                btnGetTokenViaEmail.visibility = View.GONE
                tokenView()
            }
        }

    }

    private fun emailLogic() {

        forgotPasswordViewModel.userData.observe(this) {
            if (!binding.edtToken.isVisible) {
                isValidEmail = it.isValidEmail
                showLoading(false, VIEW_EMAIL)
                if (isValidEmail) {
                    email = it.email
                    emailView()
                }
            }
        }

        forgotPasswordViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    if (!binding.edtToken.isVisible) {
                        val listMessage = data.errors.message.orEmpty()
                        showAlertDialog(listMessage[0])
                        showLoading(false, VIEW_EMAIL)
                    }
                }
            }
        }

        forgotPasswordViewModel.errorsSnackbarText.observe(this) { eventString ->
            if (!binding.edtToken.isVisible) {
                eventString.getContentIfNotHandled()?.let { snackBarText ->
                    Snackbar.make(
                        binding.root as ViewGroup,
                        snackBarText,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    showLoading(false, VIEW_EMAIL)
                }
            }
        }

        binding.btnGetTokenViaEmail.setOnClickListener {
            hideKeyboard(it)
            if (checkValidatedEmail(binding.edtEmail.text.toString().trim())) {
                showLoading(true, VIEW_EMAIL)
                forgotPasswordViewModel.resetPassword(binding.edtEmail.text.toString().trim())
            }

        }


    }

    private fun tokenView() {
        binding.apply {

            tvInstructionTokenResetPassword1.visibility = View.VISIBLE
            tvInstructionTokenResetPasswordEmail.visibility = View.VISIBLE
            tvInstructionTokenResetPassword2.visibility = View.VISIBLE

            tvInstructionTokenResetPasswordEmail.text = email

            inputLayoutToken.visibility = View.VISIBLE
            edtToken.visibility = View.VISIBLE
            btnVerifyToken.visibility = View.VISIBLE
            onTextChange(edtToken, inputLayoutToken)
            tokenLogic()
        }

    }

    private fun tokenLogic() {

        fun minCharacterTokenValidation(
            token: String,
            layout: TextInputLayout,
            message: String,
            min: Int,
            send: Boolean = false,
        ): Boolean {

            var isValid = true
            if (token.isNotEmpty() &&
                token.length >= min
            ) {
                layout.error = null
                layout.isErrorEnabled = false
            } else if (token.isEmpty() && !send) {
                layout.error = null
                layout.isErrorEnabled = false
            } else if (token.isEmpty() && send) {
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

        binding.edtToken.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val token = p0.toString().trim()
                minCharacterTokenValidation(
                    token,
                    binding.inputLayoutToken,
                    stringFormatMin(getString(R.string.text_label_token)),
                    digits
                )
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        forgotPasswordViewModel.userData.observe(this) {
            isValidToken = it.isValidToken
            if (isValidToken) {
                binding.progressBar.visibility = View.GONE
                val intent =
                    Intent(this@ForgotPasswordActivity, ResetPasswordEditActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString(ResetPasswordEditActivity.KEY_USER_ID, it.userId)
                mBundle.putString(ResetPasswordEditActivity.KEY_USER_TYPE, it.userType)
                intent.putExtras(mBundle)
                startActivity(intent)
                finish()
            }
        }

        forgotPasswordViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val listMessage = data.errors.message.orEmpty()
                    showLoading(false, VIEW_TOKEN)
                    if (!listMessage[0].contains("Email")) {
                        showAlertDialog(listMessage[0])
                    }
                }
            }
        }

        forgotPasswordViewModel.errorsSnackbarText.observe(this) { eventString ->
            showLoading(false, VIEW_TOKEN)
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(
                    binding.root as ViewGroup,
                    snackBarText,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnVerifyToken.setOnClickListener {
            hideKeyboard(it)
            token = binding.edtToken.text.toString().trim()
            if (checkValidatedToken(token.orEmpty()) && minCharacterTokenValidation(
                    token.orEmpty(),
                    binding.inputLayoutToken,
                    stringFormatMin(getString(R.string.text_label_token)),
                    digits
                )
            ) {
                showLoading(true, VIEW_TOKEN)
                forgotPasswordViewModel.checkToken(email.orEmpty(), token.orEmpty())
            }
        }
    }

    private fun checkValidatedEmail(email: String): Boolean {

        var isInputFilled = false

        binding.apply {
            if (email.isEmpty()) {
                checkInputIsEmpty(inputLayoutEmail, getString(R.string.text_label_email))
            } else {
                checkInputIsEmpty(inputLayoutEmail)
                if (!isValidFormatEmail(email)) {
                    inputLayoutEmail.isErrorEnabled = true
                    inputLayoutEmail.error = getString(R.string.text_error_format_email)
                } else {
                    inputLayoutEmail.error = null
                    inputLayoutEmail.isErrorEnabled = false
                    isInputFilled = true
                }
            }
        }


        return isInputFilled
    }

    private fun checkValidatedToken(token: String): Boolean {

        var isInputFilled = false

        binding.apply {
            if (token.isEmpty()) {
                checkInputIsEmpty(inputLayoutToken, getString(R.string.text_label_token))
            } else {
                checkInputIsEmpty(inputLayoutToken)
                isInputFilled = true
            }
        }


        return isInputFilled
    }

    private fun checkInputIsEmpty(
        inputView: TextInputLayout,
        message: String? = null,
    ) {
        if (message != null) {
            inputView.isErrorEnabled = true
            inputView.error = stringFormatRequired(message)
        } else {
            inputView.error = null
            inputView.isErrorEnabled = false
        }

    }


    private fun showAlertDialog(errorMessage: String) {
        var errMsg: String = errorMessage
        val errorTitle = if (errorMessage.contains("Email")) {
            errMsg = "$errorMessage. Silahkan hubungi WEB ADMINISTRATOR atau TU"
            "${getString(R.string.text_label_email)} Error"
        } else if (errorMessage.contains("Token")) {
            "${getString(R.string.text_label_token)} Error"
        } else {
            "Server Error"
        }

        if (dialogAlert == null) {
            dialogAlert = MaterialAlertDialogBuilder(this).apply {
                setCancelable(false)
                setIcon(ContextCompat.getDrawable(this@ForgotPasswordActivity, R.drawable.z_ic_warning))
                setTitle(errorTitle)
                setMessage(errMsg)
                setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    dialogHasBeenShow = false
                    dialogAlert = null
                    return@setPositiveButton
                }
            }.create()
        }


        if (!dialogHasBeenShow) {
            dialogHasBeenShow = true
            dialogAlert?.show()
        }
    }

    private fun showLoading(boolean: Boolean, view: String) {
        binding.apply {
            showLoading(progressBar, boolean)
            when (view) {
                VIEW_EMAIL -> {
                    if (boolean) {
                        btnGetTokenViaEmail.visibility = View.GONE
                    } else {
                        btnGetTokenViaEmail.visibility = View.VISIBLE
                    }
                }

                VIEW_TOKEN -> {
                    if (boolean) {
                        btnVerifyToken.visibility = View.GONE
                    } else {
                        btnVerifyToken.visibility = View.VISIBLE
                    }
                }
            }


        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun stringFormatMin(string: String): String {
        return String.format(getString(R.string.six_digits_field), string, digits)
    }

    private fun stringFormatRequired(string: String): String {
        return String.format(getString(R.string.empty_field), string)
    }

    override fun onStop() {
        super.onStop()
        if (dialogAlert != null) {
            dialogHasBeenShow = false
            dialogAlert?.dismiss();
            dialogAlert = null;
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_EMAIL_VALID, email)
        super.onSaveInstanceState(outState)
    }


    companion object {
        private const val KEY_EMAIL_VALID = "key_email_valid"

        const val VIEW_EMAIL = "email"
        const val VIEW_TOKEN = "token"
    }

}