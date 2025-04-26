package com.yogadimas.simastekom.ui.identity.phone

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
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.Str
import com.yogadimas.simastekom.common.helper.PhoneNumberValidationHelper
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.goToProfileFragment
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.setBold
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityPhoneBinding
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.StudentIdentityParentData
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity.Companion.KEY_ADMIN_STUDENT
import com.yogadimas.simastekom.ui.student.identity.parent.StudentIdentityParentEditActivity.Companion.KEY_ADMIN_STUDENT_PARENT
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhoneActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPhoneBinding

    private val context = this@PhoneActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()


    private var isLoading = false
    private var isAlertDialogShow = false


    private var dialog: AlertDialog? = null

    private var identityPersonalData: IdentityPersonalData? = IdentityPersonalData()
    private var studentIdentityParentData: StudentIdentityParentData? = StudentIdentityParentData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showDefaultView(false)

        setDataFromIntent()
        toolbar()

        setupPhoneInstructions()
        setupPhoneValidation()
        setupRefreshButton()

        loadData()
    }


    private fun toolbar() = binding.toolbar.apply {
        setNavigationOnClickListener { finish() }
        menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
            goToProfileFragment(context)
            true
        }
    }

    private fun setDataFromIntent() {
        studentIdentityParentData = getParcelableExtra(intent, KEY_ADMIN_STUDENT_PARENT)
        if (studentIdentityParentData == null) {
            identityPersonalData = getParcelableExtra(intent, KEY_ADMIN_STUDENT)
        }
    }

    private fun setupPhoneInstructions() {
        binding.apply {
            tvInstructionPhone2.text = setBold(
                getString(
                    R.string.text_instruction_phone_2_format,
                    getString(R.string.text_country_code)
                ),
                listOf(getString(R.string.sign_plus), getString(R.string.text_country_code))
            )
            tvInstructionPhone3.text = setBold(
                getString(
                    R.string.text_instruction_phone_3_format,
                    getString(R.string.text_dummy_number_phone_ind_08),
                    getString(R.string.text_dummy_number_phone_ind_62)
                ),
                listOf(
                    getString(R.string.text_dummy_number_phone_ind_08),
                    getString(R.string.text_dummy_number_phone_ind_62),
                    getString(R.string.text_dummy_number_phone_international)
                )
            )
        }
    }

    private fun setupPhoneValidation() {
        binding.apply {
            btnSave.isEnabled = edtPhone.text.toString().isNotEmpty()

            edtPhone.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val isValid = PhoneNumberValidationHelper(
                        context,
                        inputLayoutPhone,
                        edtPhone
                    ).isValid()
                    btnSave.isEnabled = edtPhone.text.toString().isNotEmpty() && isValid
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }
    }

    private fun setupRefreshButton() = binding.viewHandle.viewFailedConnect.btnRefresh.apply {
        setOnClickListener { loadData() }
    }

    private fun loadData() {
        if (studentIdentityParentData?.userId != null) {
            observeStudentIdentityParent()
        } else {
            observeIdentityPersonal()
        }

    }


    private fun observeIdentityPersonal() {

        authViewModel.getUser().observe(context) {
            var (token, userId, userType) = it
            if (token == AuthPreferences.DEFAULT_VALUE) {
                goToLogin(context)
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
                binding.btnSave.setOnClickListener {
                    updateSave(
                        userType = userType,
                        userId = userId
                    )
                }
            }
        }

        adminViewModel.isLoading.observe(context) {
            isLoading = it
            showLoadingView(it)
        }

        adminViewModel.identityPersonal.observe(context) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    showDefaultView(false)
                } else {
                    showDefaultView(true)
                }
                showFailedConnectView(false)

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

        adminViewModel.errors.observe(context) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val errors = data.errors
                    var listMessage: List<String> = listOf()
                    when {
                        errors.message != null -> {
                            listMessage = errors.message
                        }
                    }
                    showDefaultView(true)
                    showFailedConnectView(false)
                    alertError(listMessage[0])
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(context) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                hideKeyboard()
                showDefaultView(false)
                showFailedConnectView(true)
                showSnackBar(snackBarText)

            }
        }
    }

    private fun observeStudentIdentityParent() {
        authViewModel.getUser().observe(context) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                goToLogin(context)
            } else {
                val studentId = studentIdentityParentData?.userId.orEmpty()
                adminViewModel.getStudentIdentityParentByIdLiveData(token, studentId)
                    .observe(context) { state ->
                        setStateStudentIdentityParent(state, token)
                    }
            }
        }

    }


    private fun updateSave(
        userType: String = Str.EMPTY.value,
        userId: String,
        token: String = Str.EMPTY.value,
    ) {
        hideKeyboard()

        if (PhoneNumberValidationHelper(
                context,
                binding.inputLayoutPhone,
                binding.edtPhone
            ).isValid()
        ) {
            val phoneData = binding.edtPhone.text.toString()
            if (studentIdentityParentData?.userId != null) {
                adminViewModel.updateStudentIdentityParentLiveData(
                    token,
                    studentIdentityParentData?.userId.orEmpty(),
                    StudentIdentityParentData(userId = userId, phone = phoneData)
                ).observe(context) { state ->
                    setStateStudentIdentityParent(state, token)
                }
            } else {
                adminViewModel.updateIdentityPersonal(
                    userType,
                    userId,
                    IdentityPersonalData(phone = phoneData)
                )
            }

        }
    }


    private fun setStateStudentIdentityParent(
        state: State<StudentIdentityParentData>?,
        token: String,
    ) {
        when (state) {
            is State.Loading -> showLoadingView(true)

            is State.Success -> {
                showLoadingView(false)
                showDefaultView(true)

                val identityData = state.data

                val message = when {
                    identityData.isAdded -> getString(
                        R.string.text_alert_add_format,
                        getString(R.string.text_success),
                        getString(R.string.text_label_phone)
                    )

                    identityData.isUpdated -> getString(
                        R.string.text_alert_update_format,
                        getString(R.string.text_success),
                        getString(R.string.text_label_phone)
                    )

                    else -> ""
                }

                if (message.isNotEmpty()) {
                    val resultIntent = Intent().apply {
                        putExtra(KEY_EXTRA_SUCCESS, message)
                    }
                    setResult(KEY_RESULT_CODE, resultIntent)
                    finish()
                }

                setupButtonListenersStudentIdentityParent(
                    identityData,
                    token,
                )


            }

            is State.ErrorClient -> {
                showLoadingView(false)
                showDefaultView(true)
                alertError(state.error.errors?.message?.firstOrNull() ?: Str.EMPTY.value)
            }

            is State.ErrorServer -> {
                showLoadingView(false)
                showFailedConnectView(true)
                showSnackBar(state.error)
            }

            null -> {}
        }
    }

    private fun setupButtonListenersStudentIdentityParent(
        identityData: StudentIdentityParentData,
        token: String,
    ) {

        val userIdCreated = identityData.userId ?: studentIdentityParentData?.userId.orEmpty()


        binding.apply {
            btnSave.setOnClickListener {
                updateSave(
                    userId = userIdCreated,
                    token = token
                )
            }
        }
    }


    private fun alertError(message: String) {
        STATUS_ERROR.showAlertDialog(msg = message)
    }

    private fun String.showAlertDialog(msg: String = "") {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null

        when (this@showAlertDialog) {
            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(context, R.drawable.z_ic_warning)
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(context, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error_format, "")
                    message = msg
                }
            }
        }

        if (dialog == null) {
            dialog = MaterialAlertDialogBuilder(context).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (this@showAlertDialog == STATUS_ERROR) {
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
        hideKeyboard(currentFocus ?: View(context))
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

    private fun showFailedConnectView(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        SnackBarHelper.display(binding.root as ViewGroup, message, context)
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