package com.yogadimas.simastekom.ui.student.identity.parent

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.Str
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.dataString
import com.yogadimas.simastekom.common.helper.formatDataMaterialTextview
import com.yogadimas.simastekom.common.helper.getParcelableCompat
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.goToProfileFragment
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoadingFade
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityParentEditBinding
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.StudentIdentityParentData
import com.yogadimas.simastekom.ui.identity.address.AddressHomeEditActivity
import com.yogadimas.simastekom.ui.identity.phone.PhoneActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentIdentityParentEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentIdentityParentEditBinding

    private val context = this@StudentIdentityParentEditActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }
    private val adminViewModel: AdminViewModel by viewModel()

    private var isAlertDialogShow = false
    private var dialog: AlertDialog? = null
    private val strEmpty = Str.EMPTY.value

    private var studentIdentityParentData: StudentIdentityParentData? = StudentIdentityParentData()

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode = result.resultCode
        val resultData = result.data
        val doesItProduceValue: (Int) -> Boolean =
            { key -> resultCode == key && resultData != null }

        val showAlertDialogCallback: (String) -> Unit = { key ->
            val successText = resultData?.getStringExtra(key).orEmpty()
            alertSuccess(successText)
        }
        when {
            doesItProduceValue(PhoneActivity.KEY_RESULT_CODE) -> {
                showAlertDialogCallback(PhoneActivity.KEY_EXTRA_SUCCESS)
                observeData()
            }
            doesItProduceValue(AddressHomeEditActivity.KEY_RESULT_CODE) -> {
                observeData()
            }
            doesItProduceValue(AddressHomeEditActivity.KEY_RESULT_CODE_DELETED) -> {
                showAlertDialogCallback(AddressHomeEditActivity.KEY_EXTRA_SUCCESS)
                observeData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentIdentityParentEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStudentIdentityParentDataBundle(savedInstanceState)
        toolbar()
        mainContent()
    }

    private fun setStudentIdentityParentDataBundle(savedInstanceState: Bundle?) {
        studentIdentityParentData = getParcelableExtra(intent, KEY_ADMIN_STUDENT_PARENT)
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        studentIdentityParentData = savedInstanceState.getParcelableCompat(
            KEY_BUNDLE_IDENTITY_PARENT
        )
    }


    private fun toolbar() = binding.toolbar.apply {
        setNavigationOnClickListener { finish() }
        menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
            goToProfileFragment(context)
            true
        }
    }

    private fun mainContent() = binding.apply {
        edtFatherIdCardNumber.apply {
            transformationMethod = null
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        hideKeyboard()
                        true
                    }

                    else -> false
                }

            }
        }
        edtMotherIdCardNumber.apply {
            transformationMethod = null
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        hideKeyboard()
                        true
                    }

                    else -> false
                }

            }
        }
        observeData()
    }


    private fun observeData() = executeMode { validToken, userId ->
        adminViewModel.getStudentIdentityParentById(validToken, userId)
        collectStudentState(validToken)
    }

    private fun executeMode(action: suspend (String, String) -> Unit) = lifecycleScope.launch {
        val token = getToken() ?: return@launch
        val userId = if (studentIdentityParentData?.isFromAdminStudent == true) {
            getSelectedUserId()
        } else {
            getCurrentUserLoginId()
        } ?: return@launch

        action(token, userId)
    }

    private suspend fun collectStudentState(token: String) {
        adminViewModel.studentIdentityParentState.collect { state ->
            when (state) {
                is State.Loading -> showLoadingView(true)
                is State.Success -> showDataView(token, state.data)
                is State.ErrorClient -> showErrorClientView(state.error)
                is State.ErrorServer -> showErrorServerView(state.error)
            }
        }
    }


    private suspend fun getToken(): String? {
        val user = authViewModel.getUser().asFlow().first()
        val token = user.first
        return if (token == AuthPreferences.DEFAULT_VALUE) {
            goToLogin(context)
            null
        } else token
    }

    private suspend fun getCurrentUserLoginId(): String? {
        val userId = authViewModel.getUser().asFlow().first().second
        return userId.ifEmpty {
            goToLogin(context)
            null
        }
    }

    private fun getSelectedUserId(): String? {
        return studentIdentityParentData?.let { data ->
            data.userId.orEmpty()
        }
    }


    private fun showErrorClientView(error: Errors) {
        showLoadingView(false)
        showDefaultView(true)
        val message = error.errors?.message?.firstOrNull() ?: Str.EMPTY.value
        alertError(message)
    }

    private fun showErrorServerView(error: String) {
        showLoadingView(false)
        showFailedConnectView(true)
        showSnackBar(error)
    }

    private fun showDataView(token: String, data: StudentIdentityParentData) =
        lifecycleScope.launch {
            val userIdResponse = data.userId
            val isModeCreate = userIdResponse == null
            delay(600)
            showLoadingView(false)
            showDefaultView(true)
            checkButtonDeleteIsEnabled(userIdResponse)
            if (data.isAdded || data.isUpdated || data.isDeleted) {
                val success = getString(R.string.text_success)
                val label = getString(R.string.text_identity_parent)
                val msg = when {
                    data.isAdded -> R.string.text_alert_add_format
                    data.isUpdated -> R.string.text_alert_update_format
                    else -> R.string.text_alert_delete_format
                }


                val message = getString(msg, success, label)
                alertSuccess(message)

            }
            binding.apply {
                studentIdentityParentData?.let { parentData ->
                    parentData.apply {
                        idCardNumberFather = data.idCardNumberFather
                        nameFather = data.nameFather
                        idCardNumberMother = data.idCardNumberMother
                        nameMother = data.nameMother
                        studentIdNumber = data.studentIdNumber
                        studentName = data.studentName
                        occupation = data.occupation
                        phone = data.phone
                        address = data.address

                        edtFatherIdCardNumber.setText(idCardNumberFather)
                        edtFatherName.setText(nameFather)
                        edtMotherIdCardNumber.setText(idCardNumberMother)
                        edtMotherName.setText(nameMother)
                        edtParentOccupation.setText(occupation)
                        tvPhone.text = formatDataMaterialTextview(
                            getString(R.string.text_label_phone),
                            phone.orEmpty(),
                            context
                        )
                        tvAddressHome.text = if (address?.userId != null) {
                            getString(
                                R.string.text_change_format,
                                getString(R.string.text_address_home)
                            )
                        } else {
                            getString(
                                R.string.text_add_format,
                                getString(R.string.text_address_home)
                            )
                        }

                        setupTextWatchers(edtFatherIdCardNumber) { idCardNumberFather = it }
                        setupTextWatchers(edtFatherName) { nameFather = it }
                        setupTextWatchers(edtMotherIdCardNumber) { idCardNumberMother = it }
                        setupTextWatchers(edtMotherName) { nameMother = it }
                        setupTextWatchers(edtParentOccupation) { occupation = it }
                        (userIdResponse ?: userId)?.let { userId ->
                            btnSave.setOnClickListener {
                                addOrUpdate(
                                    token,
                                    userId,
                                    isModeCreate
                                )
                            }
                            btnDelete.setOnClickListener { alertDelete(userId) }
                            tvPhone.setOnClickListener {
                                studentIdentityParentData?.userId = userId
                                resultLauncher.launch(
                                    Intent(
                                        context,
                                        PhoneActivity::class.java
                                    ).apply {
                                        putExtra(
                                            KEY_ADMIN_STUDENT_PARENT,
                                            studentIdentityParentData
                                        )
                                    }
                                )
                            }
                            tvAddressHome.setOnClickListener {
                                studentIdentityParentData?.userId = userId
                                resultLauncher.launch(
                                    Intent(
                                        context,
                                        AddressHomeEditActivity::class.java
                                    ).apply {
                                        putExtra(
                                            KEY_ADMIN_STUDENT_PARENT,
                                            studentIdentityParentData
                                        )
                                    }
                                )
                            }
                        }

                    }
                }
            }



            checkButtonSaveIsEnabled()


        }


    private fun addOrUpdate(token: String, userId: String, doesItAdd: Boolean) = binding.apply {
        hideKeyboard()
        val idCardNumberFather = edtFatherIdCardNumber.text.dataString()
        val idCardNumberMother = edtMotherIdCardNumber.text.dataString()
        val nameFather = edtFatherName.text.dataString()
        val nameMother = edtMotherName.text.dataString()
        val occupationParent = edtParentOccupation.text.dataString()

        val data = StudentIdentityParentData(
            userId = userId,
            idCardNumberFather = idCardNumberFather,
            nameFather = nameFather,
            idCardNumberMother = idCardNumberMother,
            nameMother = nameMother,
            occupation = occupationParent,
        )
        if (doesItAdd) {
            adminViewModel.addStudentIdentityParent(token, data)
        } else {
            adminViewModel.updateStudentIdentityParent(token, userId, data)
        }


    }


    private fun showLoadingView(isVisible: Boolean) {

        showLoadingFade(binding.mainProgressBar, isVisible)

        if (isVisible) {
            hideKeyboard()
            showDefaultView(false)
            showFailedConnectView(false)
        }
    }

    private fun showDefaultView(isVisible: Boolean) {
        binding.apply {
            if (isVisible) {
                toolbar.visibility = View.VISIBLE
                inputLayoutFatherIdCardNumber.visibility = View.VISIBLE
                inputLayoutMotherIdCardNumber.visibility = View.VISIBLE
                inputLayoutFatherName.visibility = View.VISIBLE
                inputLayoutMotherName.visibility = View.VISIBLE
                inputLayoutParentOccupation.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE
                div1.visibility = View.VISIBLE
                div2.visibility = View.VISIBLE
                layoutNavigation.visibility = View.VISIBLE
            } else {
                toolbar.visibility = View.INVISIBLE
                inputLayoutFatherIdCardNumber.visibility = View.GONE
                inputLayoutMotherIdCardNumber.visibility = View.GONE
                inputLayoutFatherName.visibility = View.GONE
                inputLayoutMotherName.visibility = View.GONE
                inputLayoutParentOccupation.visibility = View.GONE
                btnSave.visibility = View.GONE
                btnDelete.visibility = View.GONE
                div1.visibility = View.GONE
                div2.visibility = View.GONE
                layoutNavigation.visibility = View.GONE
            }
        }
    }

    private fun showFailedConnectView(isVisible: Boolean) {
        binding.viewHandle.viewFailedConnect.root.isVisible = isVisible
    }

    private fun setupTextWatchers(editText: TextInputEditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                onTextChanged(p0.toString())
                checkButtonSaveIsEnabled()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun checkButtonSaveIsEnabled() = binding.apply {
        btnSave.isEnabled = run {
            listOf(
                edtFatherIdCardNumber,
                edtFatherName,
                edtMotherIdCardNumber,
                edtMotherName,
                edtParentOccupation
            ).any { it.text.toString().isNotEmpty() }
        }

    }

    private fun checkButtonDeleteIsEnabled(userId: String?) = binding.btnDelete.apply {
        val colorOnSurface = com.google.android.material.R.attr.colorOnSurface
        val colorError = com.google.android.material.R.attr.colorError

        val setColor: (Int) -> Int = { resid ->
            MaterialColors.getColor(this, resid)
        }
        isEnabled = if (userId == null) {
            setTextColor(
                ColorUtils.setAlphaComponent(
                    setColor(colorOnSurface),
                    (0.30f * 255).toInt()
                )
            )
            false
        } else {
            setTextColor(setColor(colorError))
            true
        }
        isVisible = isEnabled


    }


    private fun alertSuccess(message: String) {
        showAlertDialog(msg = message, status = STATUS_SUCCESS)
    }

    private fun alertError(message: String) {
        showAlertDialog(msg = message, status = STATUS_ERROR)
    }

    private fun alertDelete(userId: String) {
        showAlertDialog(msg = getString(R.string.text_identity_parent_this), status = STATUS_CONFIRM_DELETE, userId = userId)
    }

    private fun showAlertDialog(
        msg: String = strEmpty,
        status: String,
        userId: String = strEmpty,
    ) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null
        when (status) {
            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(context, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(context, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = msg
            }

            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(context, R.drawable.z_ic_warning)
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(context, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error, "")
                    message = msg
                }

            }

            STATUS_CONFIRM_DELETE -> {
                icon = ContextCompat.getDrawable(context, R.drawable.z_ic_delete)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(context, R.color.md_theme_error)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_delete)
                message = getString(R.string.text_question_do_you_want_to_delete_format, msg)

            }

        }

        if (dialog == null) {
            if (status == STATUS_SUCCESS) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500)
                    isAlertDialogShow = false
                    dialog?.dismiss()
                    dialog = null
                }
            }

            fun defaultStateDialog() {
                isAlertDialogShow = false
                dialog = null
            }

            dialog = MaterialAlertDialogBuilder(context).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (status == STATUS_ERROR) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        defaultStateDialog()
                        if (unauthorized) authViewModel.saveUser(
                            null,
                            null,
                            null
                        ) else return@setPositiveButton
                    }
                } else if (status == STATUS_CONFIRM_DELETE) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        defaultStateDialog()
                        lifecycleScope.launch {
                            getToken()?.let {
                                adminViewModel.deleteStudentIdentityParent(it, userId)
                            }
                        }
                    }
                    setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                        defaultStateDialog()
                        return@setNegativeButton
                    }
                }
            }.create()
        }

        if (!isAlertDialogShow) {
            isAlertDialogShow = true
            dialog?.show()
        }

    }


    private fun showSnackBar(message: String) {
        SnackBarHelper.display(binding.root as ViewGroup, message, context)
    }

    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            binding.apply {
                inputLayoutFatherIdCardNumber.editText?.clearFocus()
                inputLayoutMotherIdCardNumber.editText?.clearFocus()
                inputLayoutFatherName.editText?.clearFocus()
                inputLayoutMotherName.editText?.clearFocus()
                inputLayoutParentOccupation.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(context))
        clearFocus()
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
        outState.putParcelable(KEY_BUNDLE_IDENTITY_PARENT, studentIdentityParentData)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val STATUS_CONFIRM_DELETE = "status_confirm_delete"

        const val KEY_ADMIN_STUDENT_PARENT = "key_admin_student_parent"

        private const val KEY_BUNDLE_IDENTITY_PARENT = "key_bundle_identity_parent"
    }

}