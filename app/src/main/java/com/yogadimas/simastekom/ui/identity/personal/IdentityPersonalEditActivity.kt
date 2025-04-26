package com.yogadimas.simastekom.ui.identity.personal

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.formatDataMaterialTextview
import com.yogadimas.simastekom.common.helper.getParcelableCompat
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnOptionDialogListenerInterface
import com.yogadimas.simastekom.databinding.ActivityIdentityPersonalEditBinding
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.ui.dialog.GenderDialogFragment
import com.yogadimas.simastekom.ui.dialog.ReligionDialogFragment
import com.yogadimas.simastekom.ui.identity.address.AddressHomeEditActivity
import com.yogadimas.simastekom.ui.identity.personal.birth.PlaceDateBirthActivity
import com.yogadimas.simastekom.ui.identity.personal.email.EmailActivity
import com.yogadimas.simastekom.ui.identity.phone.PhoneActivity
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.mainpage.profile.ProfileFragment
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class IdentityPersonalEditActivity : AppCompatActivity(), OnOptionDialogListenerInterface {


    private lateinit var binding: ActivityIdentityPersonalEditBinding

    private val fragmentManager = supportFragmentManager

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var isLoading = false
    private var isAlertDialogShow = false

    private var dialog: AlertDialog? = null

    private var isSuccessDialogShowingOrientation = false

    private var identityPersonalData: IdentityPersonalData? = IdentityPersonalData()

    private var keyRole = KEY_ADMIN_STUDENT


    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode = result.resultCode
        val resultData = result.data
        val doesItProduceValue: (Int) -> Boolean =
            { key -> resultCode == key && resultData != null }

        fun showAlertDialogCallback(key: String, isDeleted: Boolean = false) {
            alertSuccessCallback(key = key, resultData = resultData, isDeleted = isDeleted)
        }

        when {
            doesItProduceValue(PhoneActivity.KEY_RESULT_CODE) -> {
                showAlertDialogCallback(PhoneActivity.KEY_EXTRA_SUCCESS)
            }

            doesItProduceValue(EmailActivity.KEY_RESULT_CODE) -> {
                showAlertDialogCallback(EmailActivity.KEY_EXTRA_SUCCESS)
            }

            doesItProduceValue(AddressHomeEditActivity.KEY_RESULT_CODE_DELETED) -> {
                showAlertDialogCallback(AddressHomeEditActivity.KEY_EXTRA_SUCCESS, true)
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIdentityPersonalEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        identityPersonalData = getParcelableExtra(intent, KEY_ADMIN_STUDENT)
            ?: getParcelableExtra(intent, KEY_ADMIN_LECTURER)
                    ?: getParcelableExtra(intent, KEY_ADMIN_ADMIN)


        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }

        showDefaultView(false)

        observeData()

        binding.apply {

            toolbar.setNavigationOnClickListener {
                finish()
            }
            toolbar.menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
                val intent = Intent(this@IdentityPersonalEditActivity, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra(MainActivity.KEY_PAGE, ProfileFragment.NAME_FRAGMENT)
                startActivity(intent)
                true
            }

            edtIdCardNumber.transformationMethod = null
            edtIdCardNumber.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        hideKeyboard()
                        true
                    }

                    else -> false
                }

            }


            checkingIsGenderGone {
                edtGender.setOnClickListener {
                    hideKeyboard()
                    GenderDialogFragment().show(
                        fragmentManager,
                        GenderDialogFragment::class.java.simpleName
                    )
                }
            }



            edtReligion.setOnClickListener {
                hideKeyboard()
                ReligionDialogFragment().show(
                    fragmentManager,
                    ReligionDialogFragment::class.java.simpleName
                )
            }



            tvPhone.setOnClickListener {
                resultLauncher.launch(
                    Intent(this@IdentityPersonalEditActivity, PhoneActivity::class.java).apply {
                        putExtra(KEY_ADMIN_STUDENT, identityPersonalData)
                    }
                )
            }
            tvEmail.setOnClickListener {
                resultLauncher.launch(
                    Intent(this@IdentityPersonalEditActivity, EmailActivity::class.java).apply {
                        putExtra(KEY_ADMIN_STUDENT, identityPersonalData)
                    }
                )
            }
            tvPlaceDateBirth.setOnClickListener {
                startActivity(
                    Intent(
                        this@IdentityPersonalEditActivity,
                        PlaceDateBirthActivity::class.java
                    ).apply {
                        putExtra(KEY_ADMIN_STUDENT, identityPersonalData)
                    }
                )
            }
            tvAddressHome.setOnClickListener {
                resultLauncher.launch(
                    Intent(
                        this@IdentityPersonalEditActivity,
                        AddressHomeEditActivity::class.java
                    ).apply {
                        putExtra(
                            KEY_ADMIN_STUDENT,
                            identityPersonalData
                        )
                    }
                )
            }

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { observeData() }
        }

    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        identityPersonalData = savedInstanceState.getParcelableCompat(
            KEY_BUNDLE_IDENTITY_PERSONAL
        )
        isSuccessDialogShowingOrientation =
            savedInstanceState.getBoolean(KEY_SUCCESS_DIALOG_SHOWING)
        if (isSuccessDialogShowingOrientation) {
            showAlertDialog(status = STATUS_SUCCESS)
        }
    }


    private fun observeData() {
        authViewModel.getUser().observe(this) {
            var (token, userId, userType) = it
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                if (identityPersonalData?.isFromAdmin == true) {
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
            showLoadingView(it)
        }

        adminViewModel.identityPersonal.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    showDefaultView(false)
                } else {
                    showDefaultView(true)
                }
                showFailedConnectView(false)


                binding.apply {
                    edtIdCardNumber.setText(it.idCardNumber)
                    if (identityPersonalData?.isFromAdmin == true) {
                        identityPersonalData?.gender = it.gender
                    } else {
                        setGender(it.gender)
                    }
                    edtReligion.setText(it.religion)
                    tvPhone.text =
                        formatDataMaterialTextview(
                            getString(R.string.text_label_phone),
                            it.phone.orEmpty(),
                            this@IdentityPersonalEditActivity
                        )
                    tvEmail.text =
                        formatDataMaterialTextview(
                            getString(R.string.text_label_email),
                            it.email.orEmpty(),
                            this@IdentityPersonalEditActivity
                        )
                }





                if (it.isUpdated) {
                    isSuccessDialogShowingOrientation = true
                    showAlertDialog(status = STATUS_SUCCESS)
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

                        errors.gender != null -> {
                            listMessage = errors.gender
                        }
                    }
                    showDefaultView(true)
                    showFailedConnectView(false)
                    showAlertDialog(listMessage[0], STATUS_ERROR)
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                hideKeyboard()
                showDefaultView(false)
                showFailedConnectView(true)
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
        val idCardNumber = binding.edtIdCardNumber.text.toString().trim()
        val gender =
            if (identityPersonalData?.isFromAdmin == true)
                identityPersonalData?.gender
            else
                binding.edtGender.text.toString().lowercase()
        val religion = binding.edtReligion.text.toString().trim()

        adminViewModel.updateIdentityPersonal(
            userType,
            userId,
            IdentityPersonalData(
                idCardNumber = idCardNumber,
                gender = gender,
                religion = religion
            )
        )
    }


    private fun alertSuccessCallback(key: String, resultData: Intent?, isDeleted: Boolean) {
        val message = resultData?.getStringExtra(key).orEmpty()
        showAlertDialog(msg = message, status = STATUS_SUCCESS, isCallback = true, isDeleted)
    }


    private fun showAlertDialog(
        msg: String = "",
        status: String,
        isCallback: Boolean = false,
        isDeleted: Boolean = false,
    ) {
        val unauthorized = msg == getString(R.string.text_const_unauthorized)
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
                message = when {
                    msg.isNotEmpty() && isCallback -> when {
                        isDeleted -> msg
                        else -> getString(R.string.text_alert_update_format, title, msg)
                    }

                    else -> getString(R.string.text_alert_update_data_format, title)
                }

            }

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
            if (status == STATUS_SUCCESS) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500)
                    isAlertDialogShow = false
                    isSuccessDialogShowingOrientation = false
                    dialog?.dismiss()
                    dialog = null
                }
            }

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
                inputLayoutGender.editText?.clearFocus()
                inputLayoutIdCardNumber.editText?.clearFocus()
                inputLayoutReligion.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
    }


    private fun showLoadingView(boolean: Boolean) {
        showLoading(binding.mainProgressBar, boolean)
        if (boolean) {
            showDefaultView(false)
            showFailedConnectView(false)
        }
    }

    private fun showDefaultView(boolean: Boolean) {
        binding.apply {
            appBarLayout.isVisible = boolean
            if (boolean) {
                toolbar.visibility = View.VISIBLE
                inputLayoutIdCardNumber.visibility = View.VISIBLE
                checkingIsGenderGone { inputLayoutGender.visibility = View.VISIBLE }
                inputLayoutReligion.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE
                div1.visibility = View.VISIBLE
                layoutNavigation.visibility = View.VISIBLE
            } else {
                toolbar.visibility = View.INVISIBLE
                inputLayoutIdCardNumber.visibility = View.GONE
                inputLayoutGender.visibility = View.GONE
                inputLayoutReligion.visibility = View.GONE
                btnSave.visibility = View.GONE
                div1.visibility = View.GONE
                layoutNavigation.visibility = View.GONE
            }
        }
    }

    private fun showFailedConnectView(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }


    private fun setGender(gender: String?) {
        binding.apply {
            if (gender != null) {
                when (gender.lowercase()) {
                    getString(R.string.text_man).lowercase() -> {
                        inputLayoutGender.startIconDrawable =
                            ContextCompat.getDrawable(
                                this@IdentityPersonalEditActivity,
                                R.drawable.z_ic_man
                            )
                        edtGender.setText(getString(R.string.text_man))
                    }

                    getString(R.string.text_woman).lowercase() -> {
                        inputLayoutGender.startIconDrawable =
                            ContextCompat.getDrawable(
                                this@IdentityPersonalEditActivity,
                                R.drawable.z_ic_woman
                            )
                        edtGender.setText(getString(R.string.text_woman))
                    }
                }
            }
        }
    }

    private fun checkingIsGenderGone(block: () -> Unit) {
        if (identityPersonalData?.isFromAdmin != true) {
            block()
        } else {
            setGenderGone()
        }
    }

    private fun setGenderGone() = binding.apply {
        inputLayoutGender.isVisible = false
        inputLayoutGender.editText?.isVisible = false
        inputLayoutGender.startIconDrawable = null
        inputLayoutGender.endIconMode = TextInputLayout.END_ICON_NONE
        inputLayoutGender.requestLayout()
    }

    override fun onOptionChosen(text: String, category: String) {
        when (category) {
            GenderDialogFragment.KEY_OPTION_GENDER -> {
                setGender(text)
                binding.edtGender.setText(text)
            }

            ReligionDialogFragment.KEY_OPTION_RELIGION -> {
                binding.edtReligion.setText(text)
            }
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_BUNDLE_IDENTITY_PERSONAL, identityPersonalData)
        outState.putBoolean(KEY_SUCCESS_DIALOG_SHOWING, isSuccessDialogShowingOrientation)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"

        const val KEY_ADMIN_STUDENT = "key_admin_student"
        const val KEY_ADMIN_LECTURER = "key_admin_lecturer"
        const val KEY_ADMIN_ADMIN = "key_admin_admin"

        private const val KEY_BUNDLE_IDENTITY_PERSONAL = "key_bundle_identity_personal"
    }


}