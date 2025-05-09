package com.yogadimas.simastekom.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityLoginBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.onTextChange
import com.yogadimas.simastekom.common.helper.sendMessage
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.ui.password.ForgotPasswordActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var isLoading = false

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoading(binding.progressBar, it)
        }

        adminViewModel.adminData.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                authViewModel.saveUser(it.token, it.userId, it.userType)
                val intent = Intent(this, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }


        adminViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val listMessage = data.errors.message.orEmpty()
                    showAlertDialog(listMessage[0])
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                Snackbar.make(
                    binding.root as ViewGroup,
                    snackBarText,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        val bindingEdtId = binding.edtId
        val bindingEdtPassword = binding.edtPassword
        val bindingLayoutEdtId = binding.inputLayoutId
        val bindingLayoutEdtPassword = binding.inputLayoutPassword

        onTextChange(bindingEdtId, bindingLayoutEdtId)
        onTextChange(bindingEdtPassword, bindingLayoutEdtPassword)

        binding.btnLogin.setOnClickListener {

            val id = bindingEdtId.text.toString().trim()
            val password = bindingEdtPassword.text.toString().trim()

            if (checkValidated(id, password) && !isLoading) {
                adminViewModel.login(
                    id,
                    password,
                )
            }
        }

        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
        }

        binding.btnSendWa.setOnClickListener {
            sendMessage(
                context = this@LoginActivity
            )
        }

    }


    private fun checkValidated(id: String, password: String): Boolean {

        var allInputAreFilled = false

        binding.apply {
            if (id.isEmpty()) {
                checkInputIsEmpty(inputLayoutId, getString(R.string.text_label_id_username))
                if (password.isNotEmpty()) checkInputIsEmpty(inputLayoutPassword)
            } else {
                checkInputIsEmpty(inputLayoutId)
                if (password.isEmpty()) {
                    checkInputIsEmpty(inputLayoutPassword, getString(R.string.text_label_password))
                } else {
                    checkInputIsEmpty(inputLayoutPassword)
                    allInputAreFilled = true
                }
            }
        }


        return allInputAreFilled
    }

    private fun checkInputIsEmpty(
        inputView: TextInputLayout,
        message: String? = null,
    ) {
        if (message != null) {
            inputView.isErrorEnabled = true
            inputView.error = stringFormat(message)
        } else {
            inputView.error = null
            inputView.isErrorEnabled = false
        }

    }

    private fun showAlertDialog(error: String) {
        if (dialog == null) {
            dialog = MaterialAlertDialogBuilder(this)
                .setCancelable(true)
                .setIcon(ContextCompat.getDrawable(this, R.drawable.z_ic_warning))
                .setTitle(getString(R.string.text_login_failed))
                .setMessage(error)
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->     dialog = null;return@setPositiveButton }
                .create()
        }
        dialog?.show()

    }

    private fun stringFormat(string: String): String {
        return String.format(getString(R.string.text_empty_field_format), string)
    }

    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            dialog?.dismiss()
            dialog = null
        }
    }
}