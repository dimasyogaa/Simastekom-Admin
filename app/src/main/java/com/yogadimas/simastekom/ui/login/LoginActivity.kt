package com.yogadimas.simastekom.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.Helper.onTextChange
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityLoginBinding
import com.yogadimas.simastekom.viewmodel.AdminLoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val adminLoginViewModel: AdminLoginViewModel by viewModels()

    private var isDoneProgress = false
    private var isLoading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adminLoginViewModel.isLoading.observe(this) {

            isLoading = it
            if (isLoading) binding.progressBar.visibility = View.VISIBLE

            if (isDoneProgress) {
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        delay(1000)
                        binding.progressBar.visibility = View.GONE
                        binding.progressBar.progress = 0
                        isDoneProgress = false
                    }
                }
            }

        }


        adminLoginViewModel.data.observe(this) {
            Log.e("TAG", "onCreate data: $it")
        }

        adminLoginViewModel.errors.observe(this) {
            if (it != null) {
                if (it.errors != null) {
                    val listMessage = it.errors.message.orEmpty()
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            delay(1000)
                            showAlertDialog(listMessage[0])

                        }

                    }
                }
            }


        }


        adminLoginViewModel.snackbarText.observe(this) { eventString ->
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

            if (checkValidated(id, password) && !isDoneProgress && !isLoading) {
                adminLoginViewModel.postLogin(
                    id,
                    password,
                ) {
                    Log.e("TAG", "progressListener: $it")
                    val progress = it
                    binding.progressBar.setProgressCompat(progress, true)
                    isDoneProgress = it == 100
                }
            }


            // startActivity(Intent(this, MainActivity::class.java))
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
        startActivity(Intent(this, MainActivity::class.java))
        // MaterialAlertDialogBuilder(this)
        //     .setCancelable(true)
        //     // .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_delete))
        //     .setTitle("Login Gagal")
        //     .setMessage(error.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        //     .setPositiveButton("OK") { _, _ -> return@setPositiveButton }
        //     .show()
    }


    private fun stringFormat(string: String): String {
        return String.format(getString(R.string.empty_field), string)
    }
}