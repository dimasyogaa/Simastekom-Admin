package com.yogadimas.simastekom.ui.identity.personal.phone

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityPhoneBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.PhoneNumberValidationHelper
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.setBold
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity.Companion.KEY_ADMIN_STUDENT
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.profile.ProfileFragment
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhoneActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPhoneBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()


    private var isLoading = false
    private var isAlertDialogShow = false


    private var dialog: AlertDialog? = null

    private var identityPersonalData: IdentityPersonalData? = IdentityPersonalData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)



        identityPersonalData = getParcelableExtra(intent, KEY_ADMIN_STUDENT)


        binding.apply {

            toolbar.setNavigationOnClickListener {
                finish()
            }
            toolbar.menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
                val intent = Intent(this@PhoneActivity, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra(MainActivity.KEY_PAGE, ProfileFragment.NAME_FRAGMENT)
                startActivity(intent)
                true
            }

            tvInstructionPhone2.text = setBold(
                getString(R.string.text_instruction_phone_2, getString(R.string.text_country_code)),
                listOf(getString(R.string.sign_plus), getString(R.string.text_country_code))
            )
            tvInstructionPhone3.text = setBold(
                getString(
                    R.string.text_instruction_phone_3,
                    getString(R.string.text_dummy_number_phone_ind_08),
                    getString(R.string.text_dummy_number_phone_ind_62)
                ),
                listOf(
                    getString(R.string.text_dummy_number_phone_ind_08),
                    getString(R.string.text_dummy_number_phone_ind_62),
                    getString(R.string.text_dummy_number_phone_international)
                )
            )


            btnSave.isEnabled = edtPhone.text.toString().isNotEmpty()

            edtPhone.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val isValid = PhoneNumberValidationHelper(
                        this@PhoneActivity,
                        binding.inputLayoutPhone,
                        binding.edtPhone,
                    ).isValid()
                    btnSave.isEnabled = edtPhone.text.toString().isNotEmpty() && isValid
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            binding.viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getUser() }
        }

        getUser()


    }



    private fun getUser() {


        authViewModel.getUser().observe(this) {
            var (token, userId, userType) = it
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                if (identityPersonalData?.isFromAdminStudent == true) {
                    identityPersonalData?.let { data ->
                        userType = data.userType.orEmpty()
                        userId = data.userId.orEmpty()
                        adminViewModel.getIdentityPersonal(userType, userId)
                    }
                } else {
                    adminViewModel.getIdentityPersonal(userType, userId)
                }
                binding.btnSave.setOnClickListener { updateSave(userType, userId) }
            }
        }

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.identityPersonal.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    isVisibleAllView(false)
                } else {
                    isVisibleAllView(true)
                }
                failedToConnect(false)

                if (it.isUpdated) {
                    val resultIntent = Intent()
                    resultIntent.putExtra(
                        KEY_EXTRA_SUCCESS,
                        getString(R.string.text_label_phone)
                    )
                    setResult(KEY_RESULT_CODE, resultIntent)
                    finish()
                }
            }

        }

        adminViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val errors = data.errors
                    var listMessage: List<String> = listOf()
                    when {
                        errors.message != null -> {
                            listMessage = errors.message
                        }
                    }
                    isVisibleAllView(true)
                    failedToConnect(false)
                    showAlertDialog(listMessage[0], STATUS_ERROR)
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                hideKeyboard()
                isVisibleAllView(false)
                failedToConnect(true)
                Snackbar.make(
                    binding.root as ViewGroup,
                    snackBarText,
                    Snackbar.LENGTH_SHORT
                ).show()

            }
        }
    }

    private fun updateSave(userType: String, userId: String) {
        hideKeyboard()

        if (PhoneNumberValidationHelper(
                this@PhoneActivity,
                binding.inputLayoutPhone,
                binding.edtPhone
            ).isValid()) {
            adminViewModel.updateIdentityPersonal(
                userType,
                userId,
                IdentityPersonalData(phone = binding.edtPhone.text.toString())
            )
        }
    }

    private fun showAlertDialog(msg: String = "", status: String) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null

        when (status) {
            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.title_dialog_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error, "")
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
                inputLayoutPhone.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
    }

    private fun showLoadingMain(boolean: Boolean) {
        showLoading(binding.mainProgressBar, boolean)
        if (boolean) {
            isVisibleAllView(false)
            failedToConnect(false)
        }
    }

    private fun isVisibleAllView(boolean: Boolean) {
        binding.apply {

            if (boolean) {
                toolbar.visibility = View.VISIBLE
                inputLayoutPhone.visibility = View.VISIBLE
                edtPhone.visibility = View.VISIBLE
                cvInstruction.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                inputLayoutPhone.visibility = View.GONE
                edtPhone.visibility = View.GONE
                cvInstruction.visibility = View.GONE
                btnSave.visibility = View.GONE

            }
        }
    }

    private fun failedToConnect(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
    }

    companion object {
        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 210
    }


}