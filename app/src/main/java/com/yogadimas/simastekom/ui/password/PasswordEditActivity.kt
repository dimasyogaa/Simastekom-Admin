package com.yogadimas.simastekom.ui.password

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
import com.yogadimas.simastekom.databinding.ActivityPasswordEditBinding
import com.yogadimas.simastekom.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.helper.dataString
import com.yogadimas.simastekom.helper.hideKeyboard
import com.yogadimas.simastekom.helper.minCharacterPasswordValidation
import com.yogadimas.simastekom.helper.newPasswordSameWithNewConfirmPassword
import com.yogadimas.simastekom.helper.showLoading
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory

class PasswordEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordEditBinding

    private var hashPassword: String = ""
    private var newPasswordTemp: String = ""

    private val min = 6

    private lateinit var oldEdtPassword: TextInputEditText
    private lateinit var oldPassword: String
    private lateinit var oldLayoutPassword: TextInputLayout
    private lateinit var oldMessage: String

    private lateinit var newEdtPassword: TextInputEditText
    private lateinit var newPassword: String
    private lateinit var newLayoutPassword: TextInputLayout
    private lateinit var newMessage: String

    private lateinit var newConfirmEdtPassword: TextInputEditText
    private lateinit var newConfirmPassword: String
    private lateinit var newConfirmLayoutPassword: TextInputLayout

    private var isLoading = false
    private var dialogHasBeenShow = false
    private var hasBeenClicked = false

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModels()

    private var isSuccessDialogShowingOrientation = false

    private var dialogAlert: AlertDialog? = null
    private var dialogAuth: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()



        if (savedInstanceState != null) {
            val result = savedInstanceState.getString(NEW_CONFIRM_PASSWORD)
            newPasswordTemp = result.orEmpty()

            isSuccessDialogShowingOrientation =
                savedInstanceState.getBoolean(KEY_SUCCESS_DIALOG_SHOWING)
            if (isSuccessDialogShowingOrientation) {
                showAlertDialog(STATUS_SUCCESS)
            }
        }

        getAdmin()

        isValid(true)

        binding.btnCancel.setOnClickListener { finish() }

        binding.btnOk.setOnClickListener { savePassword() }

        binding.viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdmin() }


    }

    private fun initView() {
        binding = ActivityPasswordEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun getAdmin() {
        showLoadingMain(true)
        isVisibleAllView(false)
        failedToConnect(false)
        authViewModel.getUser().observe(this) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                adminViewModel.getAdminPassword()
            }
        }


        fun getAdminData() {
            adminViewModel.adminData.observe(this) {eventData ->
                eventData.getContentIfNotHandled()?.let {

                    hashPassword = it.password.orEmpty()
                    showLoadingMain(false)
                    isVisibleAllView(true)
                    failedToConnect(false)

                    if (it.logout) {
                        authViewModel.saveUser(null, null, null)
                    }

                    if (it.isUpdated && verifyPassword(newPasswordTemp)) {
                        isSuccessDialogShowingOrientation = true
                        showAlertDialog(STATUS_SUCCESS)
                        showLoadingOnButton(false)
                    }
                }

            }


            adminViewModel.errors.observe(this) { eventError ->
                eventError.getContentIfNotHandled()?.let { data ->
                    if (data.errors != null) {
                        val listMessage = data.errors.message.orEmpty()
                        showAlertDialogAuth(listMessage[0])
                        showLoadingMain(false)
                        isVisibleAllView(true)
                        failedToConnect(false)
                        showLoadingOnButton(false)
                    }
                }
            }

            adminViewModel.errorsSnackbarText.observe(this) { eventString ->
                eventString.getContentIfNotHandled()?.let { snackBarText ->
                    hideKeyboard()
                    Snackbar.make(
                        binding.root as ViewGroup,
                        snackBarText,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    showLoadingMain(false)
                    showLoadingOnButton(false)
                    isVisibleAllView(false)
                    failedToConnect(true)
                }
            }
        }

        getAdminData()

    }

    private fun isValid(started: Boolean): Boolean {

        fun checkOldAndNewPassword(
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
                min,
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
            return String.format(getString(R.string.min_character_field), string, min)
        }

        oldEdtPassword = binding.edtOldPassword
        oldPassword = oldEdtPassword.text.dataString()
        oldLayoutPassword = binding.inputLayoutOldPassword
        oldMessage = stringFormat(getString(R.string.text_label_old_password))

        newEdtPassword = binding.edtNewPassword
        newPassword = newEdtPassword.text.dataString()
        newLayoutPassword = binding.inputLayoutNewPassword
        newMessage = stringFormat(getString(R.string.text_label_new_password))

        newConfirmEdtPassword = binding.edtConfirmNewPassword
        newConfirmPassword = newConfirmEdtPassword.text.dataString()
        newConfirmLayoutPassword = binding.inputLayoutConfirmNewPassword

        var checkNewPassword = false
        var checkNewConfirmPassword = false

        val checkOldPassword: Boolean = checkOldAndNewPassword(
            oldEdtPassword, oldPassword, oldLayoutPassword, oldMessage, started, null
        )
        if (checkOldPassword) {

            checkNewPassword = checkOldAndNewPassword(
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
        }


        return checkOldPassword && checkNewPassword && checkNewConfirmPassword

    }

    private fun savePassword() {
        if (!hasBeenClicked) {
            hasBeenClicked = true
            if (isValid(false)) {
                hideKeyboard()
                if (verifyPassword(oldPassword)) {
                    showLoadingOnButton(true)
                    newPasswordTemp = binding.edtConfirmNewPassword.text.toString()
                    adminViewModel.updateAdminCurrentPassword(newPasswordTemp)
                } else {
                    showAlertDialog(STATUS_ERROR)
                }
            }
            hasBeenClicked = false
        }
    }

    private fun verifyPassword(plainPassword: String): Boolean {
        return BCrypt.verifyer()
            .verify(plainPassword.toCharArray(), hashPassword.toCharArray()).verified
    }

    private fun showAlertDialogAuth(error: String) {
        val errorMessage = if (error == getString(R.string.text_const_unauthorized)) getString(R.string.text_please_login_again) else error

        if (dialogAuth == null) {
            dialogAuth = MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setIcon(ContextCompat.getDrawable(this, R.drawable.z_ic_warning))
                .setTitle("Login Ulang")
                .setMessage(errorMessage)
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    dialogHasBeenShow = false
                    authViewModel.saveUser(null, null, null)
                }
                .create()
        }

        if (!dialogHasBeenShow) {
            dialogHasBeenShow = true
            dialogAuth?.show()
        }
    }

    private fun showAlertDialog(status: String) {

        var title = ""
        var message = ""
        var icon: Drawable? = null
        when (status) {
            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(this, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = getString(R.string.text_alert_change_password, title)
            }

            STATUS_ERROR -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                title = getString(R.string.text_error, "")
                message = getString(
                    R.string.what_you_entered_is_not_correct,
                    getString(R.string.text_label_old_password)
                )
            }
        }

        if (dialogAlert == null) {
            dialogAlert = MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setIcon(icon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    dialogHasBeenShow = false
                    dialogAlert = null
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
                }
                .create()
        }

        if (!dialogHasBeenShow) {
            dialogHasBeenShow = true
            dialogAlert?.show()
        }


    }

    private fun showLoadingOnButton(boolean: Boolean) {
        showLoading(binding.progressBar, boolean)
        if (boolean) {
            binding.btnCancel.visibility = View.GONE
            binding.btnOk.visibility = View.GONE
        } else {
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnOk.visibility = View.VISIBLE
        }
    }

    private fun showLoadingMain(boolean: Boolean) {
        showLoading(binding.mainProgressBar, boolean)
    }

    private fun failedToConnect(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    private fun isVisibleAllView(boolean: Boolean) {
        if (boolean) {
            binding.edtOldPassword.visibility = View.VISIBLE
            binding.edtNewPassword.visibility = View.VISIBLE
            binding.edtConfirmNewPassword.visibility = View.VISIBLE
            binding.inputLayoutOldPassword.visibility = View.VISIBLE
            binding.inputLayoutNewPassword.visibility = View.VISIBLE
            binding.inputLayoutConfirmNewPassword.visibility = View.VISIBLE
            binding.btnOk.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.VISIBLE
        } else {
            binding.edtOldPassword.visibility = View.GONE
            binding.edtNewPassword.visibility = View.GONE
            binding.edtConfirmNewPassword.visibility = View.GONE
            binding.inputLayoutOldPassword.visibility = View.GONE
            binding.inputLayoutNewPassword.visibility = View.GONE
            binding.inputLayoutConfirmNewPassword.visibility = View.GONE
            binding.btnOk.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(NEW_CONFIRM_PASSWORD, newPasswordTemp)
        outState.putBoolean(KEY_SUCCESS_DIALOG_SHOWING, isSuccessDialogShowingOrientation)
        super.onSaveInstanceState(outState)
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    override fun onStop() {
        super.onStop()
        if (dialogAuth != null) {
            dialogHasBeenShow = false
            dialogAuth?.dismiss()
            dialogAuth = null
        }
        if (dialogAlert != null) {
            dialogHasBeenShow = false
            dialogAlert?.dismiss()
            dialogAlert = null
        }
    }

    companion object {
        const val NEW_CONFIRM_PASSWORD = "new_confirm_password"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"
    }
}