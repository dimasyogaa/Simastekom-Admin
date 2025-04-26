package com.yogadimas.simastekom.ui.identity.personal.email

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.isValidFormatEmail
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.databinding.ActivityEmailBinding
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity.Companion.KEY_ADMIN_STUDENT
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.mainpage.profile.ProfileFragment
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailActivity : AppCompatActivity() {


    private lateinit var binding: ActivityEmailBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var isLoading = false
    private var isAlertDialogShow = false

    private var dialog: AlertDialog? = null

    private var email: String? = null
    private var tokenMode: Boolean = false

    private val digits = 6

    private var identityPersonalData: IdentityPersonalData? = IdentityPersonalData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        identityPersonalData = getParcelableExtra(intent, KEY_ADMIN_STUDENT)

        if (savedInstanceState != null) {
            email = savedInstanceState.getString(KEY_EMAIL_VALID)
            tokenMode = savedInstanceState.getBoolean(KEY_BUNDLE_MODE_TOKEN)
        }

//        if (email == null) {
//            emailView(true)
//        } else {
//            emailView()
//        }

        allViewGone()
        if (emailIsNull()) emailObserver() else tokenObserver()

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
                val intent = Intent(this@EmailActivity, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra(MainActivity.KEY_PAGE, ProfileFragment.NAME_FRAGMENT)
                startActivity(intent)
                true
            }

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener {
                if (!binding.edtToken.isVisible) {
                    emailObserver()
                } else {
                    tokenObserver()
                }
            }
        }
    }




    private fun emailObserver() {
        authViewModel.getUser().observe(this) { user ->
            if (!emailIsNull()) return@observe

            val (token, defaultUserId, defaultUserType) = user

            if (token == AuthPreferences.DEFAULT_VALUE) {
                goToLogin()
                return@observe
            }

            adminViewModel.token = token

            val (userType, userId) = if (identityPersonalData?.isFromAdmin == true) {
                val type = identityPersonalData?.userType.orEmpty()
                val id = identityPersonalData?.userId.orEmpty()
                type to id
            } else {
                defaultUserType to defaultUserId
            }

            adminViewModel.getIdentityPersonal(userType, userId)
            setupEmailVerification(userType, userId)
        }
        observeEmailViewModel()
    }

    private fun setupEmailVerification(userType: String, userId: String) {
        binding.btnGetTokenViaEmail.setOnClickListener { view ->
            hideKeyboard(view)
            val emailInput = binding.edtEmail.text.toString().trim()
            if (checkValidatedEmail(emailInput)) {
                adminViewModel.verifyEmail(userType, userId, emailInput)
            }
        }
    }

    private fun observeEmailViewModel() {
        if (tokenMode) return

        observeEmailLoading()
        observeEmailIdentityPersonal()
        observeEmailErrors()
        observeEmailSnackBar()
    }

    private fun observeEmailLoading() {
        adminViewModel.isLoading.observe(this) { isLoading ->
            if (binding.edtToken.isVisible) return@observe
            this.isLoading = isLoading
            showLoadingMain(isLoading)
        }
    }

    private fun observeEmailIdentityPersonal() {
        adminViewModel.identityPersonal.observe(this) { eventData ->
            if (!emailIsNull()) return@observe

            eventData.getContentIfNotHandled()?.let { identity ->
                emailView(!isLoading)

                failedToConnect(false)

                if (identity.isValidEmail) {
                    email = identity.email
                    tokenView()
                    tokenObserver()
                }
            }
        }
    }

    private fun observeEmailErrors() {
        adminViewModel.errors.observe(this) { eventError ->
            if (!emailIsNull() || binding.edtToken.isVisible) return@observe

            eventError.getContentIfNotHandled()?.errors?.message?.firstOrNull()?.let { message ->
                emailView()
                failedToConnect(false)
                showAlertDialog(message)
            }
        }
    }

    private fun observeEmailSnackBar() {
        adminViewModel.errorsSnackbarText.observe(this) { event ->
            if (!emailIsNull()) return@observe

            event.getContentIfNotHandled()?.let { message ->
                hideKeyboard()
                emailView(false)
                failedToConnect(true)
                Snackbar.make(binding.root as ViewGroup, message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun tokenObserver() {
        authViewModel.getUser().observe(this) { user ->
            if (emailIsNull()) return@observe

            val (token, defaultUserId, defaultUserType) = user

            if (token == AuthPreferences.DEFAULT_VALUE) {
                goToLogin()
                return@observe
            }

            adminViewModel.token = token

            val (userType, userId) = if (identityPersonalData?.isFromAdmin == true) {
                val type = identityPersonalData?.userType.orEmpty()
                val id = identityPersonalData?.userId.orEmpty()
                type to id
            } else {
                defaultUserType to defaultUserId
            }

            adminViewModel.getIdentityPersonal(userType, userId)
            setupTokenVerification(userType, userId)
        }
        observeTokenViewModel()
    }


    private  fun minCharacterTokenValidation(
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

        binding.btnVerifyToken.isEnabled = isValid

        return isValid
    }

    private fun setupTokenVerification(userType: String, userId: String) {
        binding.btnVerifyToken.setOnClickListener { view ->
            hideKeyboard(view)
            if (checkValidatedToken(
                    binding.edtToken.text.toString().trim()
                ) && minCharacterTokenValidation(
                    binding.edtToken.text.toString().trim(),
                    binding.inputLayoutToken,
                    stringFormatMin(getString(R.string.text_label_token)),
                    digits
                )
            ) {
                adminViewModel.verifyEmailCheckToken(
                    userType,
                    userId,
                    email.orEmpty(),
                    binding.edtToken.text.toString().trim()
                )
            }
        }
    }

    private fun observeTokenViewModel() {
        if (emailIsNull()) return

        observeTokenLoading()
        observeTokenIdentityPersonal()
        observeTokenErrors()
        observeTokenSnackBar()
    }

    private fun observeTokenLoading() {
        adminViewModel.isLoading.observe(this) { isLoading ->
            if (emailIsNull()) return@observe
            this.isLoading = isLoading
            showLoadingMain(isLoading)
        }
    }

    private fun observeTokenIdentityPersonal() {
        adminViewModel.identityPersonal.observe(this) { eventData ->
            if (emailIsNull()) return@observe

            eventData.getContentIfNotHandled()?.let { identity ->
                tokenView(!isLoading)
                failedToConnect(false)

                if (identity.isUpdated) {
                    val resultIntent = Intent()
                    resultIntent.putExtra(
                        KEY_EXTRA_SUCCESS,
                        getString(R.string.text_label_email)
                    )
                    setResult(KEY_RESULT_CODE, resultIntent)
                    finish()
                }
            }
        }
    }

    private fun observeTokenErrors() {
        adminViewModel.errors.observe(this) { eventError ->
            if (emailIsNull()) return@observe

            eventError.getContentIfNotHandled()?.errors?.message?.firstOrNull()?.let { message ->
                tokenView()
                failedToConnect(false)
                showAlertDialog(message)
            }
        }
    }

    private fun observeTokenSnackBar() {
        adminViewModel.errorsSnackbarText.observe(this) { event ->
            if (emailIsNull()) return@observe

            event.getContentIfNotHandled()?.let { message ->
                hideKeyboard()
                tokenView(false)
                failedToConnect(true)
                Snackbar.make(binding.root as ViewGroup, message, Snackbar.LENGTH_SHORT).show()
            }

        }
    }


    private fun emailIsNull(): Boolean = email == null


    private fun emailView(isVisible: Boolean = true) {
        showEmailView(isVisible)
        setupEmailListener()
    }

    private fun showEmailView(isVisible: Boolean) = binding.apply {
        appBarLayout.isVisible = isVisible
        toolbar.isVisible = isVisible
        inputLayoutEmail.isVisible = isVisible
        edtEmail.isVisible = isVisible
        layoutInstruction.isVisible = isVisible
        btnGetTokenViaEmail.isVisible = isVisible
    }

    private fun setupEmailListener() = binding.apply {
        btnGetTokenViaEmail.isEnabled = edtEmail.text.toString().isNotEmpty()
        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isNotEmpty()) {
                    inputLayoutEmail.error = null
                    inputLayoutEmail.isErrorEnabled = false
                    btnGetTokenViaEmail.isEnabled = true
                }
                if (p0.toString().isEmpty()) {
                    btnGetTokenViaEmail.isEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }

    private fun tokenView(isVisible: Boolean = true) {
        tokenMode = isVisible
        showEmailView(!isVisible)
        showTokenView(isVisible)
        setupTokenListener()
    }


    private fun showTokenView(isVisible: Boolean) = binding.apply {
        appBarLayout.isVisible = isVisible
        toolbar.isVisible = isVisible
        tvInstructionTokenResetPassword1.isVisible = isVisible
        tvInstructionTokenResetPasswordEmail.isVisible = isVisible
        tvInstructionTokenResetPasswordEmail.text = email
        tvInstructionTokenResetPassword2.isVisible = isVisible
        inputLayoutToken.isVisible = isVisible
        edtToken.isVisible = isVisible
        btnVerifyToken.isVisible = isVisible
    }


    private fun setupTokenListener() = binding.apply {
        btnVerifyToken.isEnabled = edtToken.text.toString().isNotEmpty()
        edtToken.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isNotEmpty() && minCharacterTokenValidation(
                        p0.toString().trim(),
                        binding.inputLayoutToken,
                        stringFormatMin(getString(R.string.text_label_token)),
                        digits
                    )
                ) {
                    inputLayoutEmail.error = null
                    inputLayoutEmail.isErrorEnabled = false
                    btnVerifyToken.isEnabled = true
                }
                if (p0.toString().isEmpty()) {
                    btnVerifyToken.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }


    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
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


    private fun showAlertDialog(msg: String = "", status: String = STATUS_ERROR) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null

        when (status) {
            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error_format, "")
                    message = msg
                }
            }
        }

        if (dialog == null) {
            dialog = MaterialAlertDialogBuilder(this).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (status == STATUS_ERROR) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        isAlertDialogShow = false
                        dialog = null
                        if (unauthorized) authViewModel.saveUser(
                            null,
                            null,
                            null
                        ) else return@setPositiveButton
                    }
                }
            }.create()

        }

        if (!isAlertDialogShow) {
            isAlertDialogShow = true
            dialog?.show()
        }


    }

    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            binding.apply {
                inputLayoutEmail.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
    }

    private fun showLoadingMain(boolean: Boolean) = binding.apply {
        showLoading(mainProgressBar, boolean)
        if (boolean) {
            allViewGone()
            failedToConnect(false)
        }
    }

    private fun allViewGone() {
        binding.apply {
                appBarLayout.visibility = View.INVISIBLE
                toolbar.visibility = View.INVISIBLE
                cvInstruction.visibility = View.GONE
                inputLayoutEmail.visibility = View.GONE
                edtEmail.visibility = View.GONE
                btnGetTokenViaEmail.visibility = View.GONE
                tvInstructionTokenResetPassword1.visibility = View.GONE
                tvInstructionTokenResetPasswordEmail.visibility = View.GONE
                tvInstructionTokenResetPassword2.visibility = View.GONE
                inputLayoutToken.visibility = View.GONE
                edtToken.visibility = View.GONE
                btnVerifyToken.visibility = View.GONE
        }
    }

    private fun failedToConnect(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    private fun stringFormatMin(string: String): String {
        return String.format(getString(R.string.text_six_digits_field_format), string, digits)
    }

    private fun stringFormatRequired(string: String): String {
        return String.format(getString(R.string.text_empty_field_format), string)
    }

    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_EMAIL_VALID, email)
        outState.putBoolean(KEY_BUNDLE_MODE_TOKEN, tokenMode)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val KEY_EMAIL_VALID = "key_email_valid"

        private const val KEY_BUNDLE_MODE_TOKEN = "key_bundle_mode_token"

        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 211
    }


}