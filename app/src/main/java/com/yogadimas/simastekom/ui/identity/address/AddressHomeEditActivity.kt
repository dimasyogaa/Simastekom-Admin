package com.yogadimas.simastekom.ui.identity.address

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.Str
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.dataString
import com.yogadimas.simastekom.common.helper.getParcelableCompat
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.goToProfileFragment
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityAddressHomeEditBinding
import com.yogadimas.simastekom.model.responses.AddressData
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.StudentIdentityParentData
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity.Companion.KEY_ADMIN_STUDENT
import com.yogadimas.simastekom.ui.student.identity.parent.StudentIdentityParentEditActivity.Companion.KEY_ADMIN_STUDENT_PARENT
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddressHomeEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddressHomeEditBinding

    private val contextActivity = this@AddressHomeEditActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var isAlertDialogShow = false
    private var dialog: AlertDialog? = null

    private val strEmpty = Str.EMPTY.value

    private var addressData: AddressData? = AddressData()

    private var identityPersonalData: IdentityPersonalData? = IdentityPersonalData()
    private var studentIdentityParentData: StudentIdentityParentData? = StudentIdentityParentData()

    private var hasIdentityParent = false
    private var hasAddress = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressHomeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setDataFromIntentAndBundle(savedInstanceState)
        setupFields()
        setupBackPressCallback()
        loadData()
    }

    private val tiLayouts: Array<TextInputLayout> by lazy {
        arrayOf(
            binding.inputLayoutProvince,
            binding.inputLayoutCityRegency,
            binding.inputLayoutDistrict,
            binding.inputLayoutVillage,
            binding.inputLayoutRw,
            binding.inputLayoutRt,
            binding.inputLayoutStreet,
            binding.inputLayoutAddressOtherDetail
        )
    }

    private val tiEditTexts: Array<TextInputEditText> by lazy {
        arrayOf(
            binding.edtProvince, binding.edtCityRegency, binding.edtDistrict, binding.edtVillage,
            binding.edtRw, binding.edtRt, binding.edtStreet, binding.edtAddressOtherDetail
        )
    }

    private fun setupToolbar() = binding.toolbar.apply {
        setNavigationOnClickListener {
            checkHasChangedBeforeFinish()
        }
        menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
            hideKeyboard()
            if (hasChanges()) showAlertDialog(status = STATUS_PROFILE_FRAGMENT)
            else goToProfileFragment(context)
            true
        }
    }

    private fun setDataFromIntentAndBundle(savedInstanceState: Bundle?) {
        studentIdentityParentData = getParcelableExtra(intent, KEY_ADMIN_STUDENT_PARENT)
        if (studentIdentityParentData != null) {
            addressData?.isStudentIdentityParent = true
        } else {
            identityPersonalData = getParcelableExtra(intent, KEY_ADMIN_STUDENT)
        }
        savedInstanceState?.let(::restoreInstanceState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        studentIdentityParentData =
            savedInstanceState.getParcelableCompat(KEY_BUNDLE_IDENTITY_PARENT)
        addressData = savedInstanceState.getParcelableCompat(KEY_BUNDLE_ADDRESS)
        hasIdentityParent = savedInstanceState.getBoolean(KEY_BUNDLE_HAS_IDENTITY_PARENT)
        hasAddress = savedInstanceState.getBoolean(KEY_BUNDLE_HAS_ADDRESS)
    }

    private fun setupFields() {
        tiEditTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener(createTextWatcher(index))
        }
        checkButtonSaveIsEnabled(tiLayouts, tiEditTexts)
        binding.viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { loadData() }
    }

    private fun createTextWatcher(index: Int) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val inputText = s.toString()
            tiLayouts[index].apply {
                isErrorEnabled = inputText.startsWith(" ")
                error =
                    if (isErrorEnabled) getString(R.string.text_cannot_contain_spaces_early_format) else null
            }
            checkButtonSaveIsEnabled(tiLayouts, tiEditTexts)
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun setupBackPressCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkHasChangedBeforeFinish()
            }


        }
        onBackPressedDispatcher.addCallback(contextActivity, callback)
    }

    private fun checkHasChangedBeforeFinish() {
        if (hasChanges()) showAlertDialog(status = STATUS_HAS_CHANGED) else {
            checkHasOperationAddressStudentIdentityParent()
            finish()
        }
    }

    private fun checkHasOperationAddressStudentIdentityParent() {
        if (addressData?.isAdded == true || addressData?.isUpdated == true || addressData?.isDeleted == true) {
            val resultIntent = Intent()
            setResult(KEY_RESULT_CODE, resultIntent)
        }
    }

    private fun loadData() {
        setOperationIdentityPersonalAndParent(
            personal = { observeIdentityPersonal() },
            parent = { observeStudentIdentityParent() }
        )
    }


    private fun observeIdentityPersonal() {
        authViewModel.getUser().observe(contextActivity) { authData ->
            val (token, userId, userType) = authData

            if (token == AuthPreferences.DEFAULT_VALUE) {
                goToLogin(contextActivity)
                return@observe
            }

            adminViewModel.token = token

            val actualUserType = identityPersonalData?.userType ?: userType
            val actualUserId = identityPersonalData?.userId ?: userId
            adminViewModel.getIdentityPersonal(actualUserType, actualUserId)
        }


        adminViewModel.apply {
            fun handleIdentityData(identityData: IdentityPersonalData) {
                val isDataLoaded = !(adminViewModel.isLoading.value ?: true)
                showDefaultView(isDataLoaded)
                showFailedConnectView(!isDataLoaded)

                identityData.address?.run {
                    when {
                        isAdded -> alertSuccess(
                            getString(
                                R.string.text_alert_add_format,
                                getString(R.string.text_success),
                                getString(R.string.text_address_home)
                            )
                        )

                        isUpdated -> alertSuccess(
                            getString(
                                R.string.text_alert_update_format,
                                getString(R.string.text_success),
                                getString(R.string.text_address_home)
                            )
                        )

                        isDeleted -> {
                            sendCallbackIntentResultDeleted()
                        }
                    }
                }

                binding.apply {
                    identityData.address?.let { address ->
                        addressData?.apply {
                            province = address.province
                            cityRegency = address.cityRegency
                            district = address.district
                            village = address.village
                            rw = address.rw
                            rt = address.rt
                            street = address.street
                            otherDetailAddress = address.otherDetailAddress

                            updateEditTextsWithData(address)
                        }
                    }
                    checkButtonDeleteIsEnabled(identityData.address?.userId)
                    checkButtonSaveIsEnabled(tiLayouts, tiEditTexts)
                    setupButtonListenersIdentityPersonal(identityData)
                }

            }
            isLoading.observe(contextActivity) { showLoadingView(it) }
            identityPersonal.observe(contextActivity) { eventData ->
                eventData.getContentIfNotHandled()?.let { identityData ->
                    handleIdentityData(identityData)
                }
            }
            errors.observe(contextActivity) { eventError ->
                eventError.getContentIfNotHandled()?.errors?.message?.firstOrNull()
                    ?.let { errorMsg ->
                        showDefaultView(true)
                        showFailedConnectView(false)
                        alertError(errorMsg)
                    }
            }
            errorsSnackbarText.observe(contextActivity) { eventString ->
                eventString.getContentIfNotHandled()?.let { snackBarText ->
                    hideKeyboard()
                    showDefaultView(false)
                    showFailedConnectView(true)
                    showSnackBar(snackBarText)
                }
            }
        }
    }

    private fun observeStudentIdentityParent() {
        authViewModel.getUser().observe(contextActivity) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                goToLogin(contextActivity)
            } else {
                val studentId = studentIdentityParentData?.userId.orEmpty()
                addressData?.userId = studentId
                adminViewModel.getStudentIdentityParentByIdLiveData(token, studentId)
                    .observe(contextActivity) { state ->
                        setStudentIdentityParentState(state, token)
                    }
            }
        }

    }

    private fun updateSave(
        userType: String = strEmpty,
        userId: String,
        token: String = strEmpty,
    ) {
        hideKeyboard()
        binding.apply {
            val data = AddressData(
                province = edtProvince.text.dataString(),
                cityRegency = edtCityRegency.text.dataString(),
                district = edtDistrict.text.dataString(),
                village = edtVillage.text.dataString(),
                rw = edtRw.text.dataString(),
                rt = edtRt.text.dataString(),
                street = edtStreet.text.dataString(),
                otherDetailAddress = edtAddressOtherDetail.text.dataString(),
            )

            setOperationIdentityPersonalAndParent(
                personal = { updateSaveIdentityPersonalAddressLiveData(userType, userId, data) },
                parent = {
                    updateSaveIdentityParentAddressLiveData(
                        token,
                        userId,
                        data
                    )
                }
            )
        }

    }

    private fun updateSaveIdentityPersonalAddressLiveData(
        userType: String,
        userId: String,
        data: AddressData,
    ) {
        adminViewModel.updateIdentityPersonal(
            userType,
            userId,
            IdentityPersonalData(address = data)
        )
    }

    private fun updateSaveIdentityParentAddressLiveData(
        token: String,
        userId: String,
        data: AddressData,
    ) {
        adminViewModel.updateStudentIdentityParentLiveData(
            token,
            studentIdentityParentData?.userId.orEmpty(),
            StudentIdentityParentData(userId = userId, address = data)
        ).observe(contextActivity) { state ->
            setStudentIdentityParentState(state, token)
        }
    }

    private fun deleteIdentityPersonalAddressLiveData(
        userType: String,
        userId: String,
    ) {
        adminViewModel.deleteIdentityPersonalAddress(userType, userId)
    }


    private fun deleteIdentityParentAddressLiveData(
        token: String,
        userId: String,
    ) {
        adminViewModel.deleteStudentIdentityParentAddressLiveData(
            token,
            userId
        ).observe(contextActivity) { state ->
            setStudentIdentityParentState(
                state,
                token
            )
        }
    }

    private fun setStudentIdentityParentState(
        state: State<StudentIdentityParentData>?,
        token: String,
    ) {
        when (state) {
            is State.Loading -> showLoadingView(true)

            is State.Success -> showDataView(state.data, token)

            is State.ErrorClient -> lifecycleScope.launch {
                delay(600)
                if (hasAddress) {
                    showDataView(studentIdentityParentData!!, token)
                } else {
                    showDataView(StudentIdentityParentData(), token)
                }
                delay(600)
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

    private fun showDataView(
        responseData: StudentIdentityParentData,
        token: String,
    ) = lifecycleScope.launch {
        delay(600)
        showLoadingView(false)
        showDefaultView(true)


        checkButtonDeleteIsEnabled(responseData.address?.userId)
        manipulationTypeResponse(responseData)

        studentIdentityParentData?.apply {
            hasIdentityParent = responseData.userId != null
            responseData.userId?.let { userId = it }

            idCardNumberFather = responseData.idCardNumberFather
            nameFather = responseData.nameFather
            idCardNumberMother = responseData.idCardNumberMother
            nameMother = responseData.nameMother
            studentIdNumber = responseData.studentIdNumber
            studentName = responseData.studentName
            occupation = responseData.occupation
            phone = responseData.phone

            hasAddress = responseData.address?.userId != null
            responseData.address?.let { address ->
                address.userId?.let { this.address?.userId = it }
                addressData?.apply {
                    province = address.province
                    cityRegency = address.cityRegency
                    district = address.district
                    village = address.village
                    rw = address.rw
                    rt = address.rt
                    street = address.street
                    otherDetailAddress = address.otherDetailAddress
                    updateEditTextsWithData(address)
                }
            }
        }

        checkButtonSaveIsEnabled(tiLayouts, tiEditTexts)
        setupButtonListenersStudentIdentityParent(responseData, token)
    }


    private fun manipulationTypeResponse(identityData: StudentIdentityParentData) {
        identityData.address?.run {
            when {
                isAdded -> alertSuccess(
                    getString(
                        R.string.text_alert_add_format,
                        getString(R.string.text_success),
                        getString(R.string.text_address_home)
                    )
                )

                isUpdated -> alertSuccess(
                    getString(
                        R.string.text_alert_update_format,
                        getString(R.string.text_success),
                        getString(R.string.text_address_home)
                    )
                )

                isDeleted -> {
                    sendCallbackIntentResultDeleted()
                }
            }
        }
    }

    private fun setupButtonListenersIdentityPersonal(identityData: IdentityPersonalData) {
        val userType = identityData.userType.orEmpty()
        val userId = identityData.userId.orEmpty()

        binding.apply {
            btnDelete.setOnClickListener {
                alertDelete(userId = userId, userType = userType)
            }
            btnSave.setOnClickListener {
                updateSave(
                    userType = userType,
                    userId = userId
                )
            }
        }
    }

    private fun setupButtonListenersStudentIdentityParent(
        identityData: StudentIdentityParentData,
        token: String,
    ) {
        val studentIdParent = identityData.userId
        val studentId = studentIdentityParentData?.userId.orEmpty()
        val userId = studentIdParent ?: studentId

        binding.apply {
            btnDelete.setOnClickListener {
                alertDelete(token = token, userId = userId)
            }
            btnSave.setOnClickListener {
                updateSave(
                    userId = userId,
                    token = token,
                )
            }
        }
    }


    private fun updateEditTextsWithData(address: AddressData) = with(binding) {
        setEditTextValue(address.province, edtProvince)
        setEditTextValue(address.cityRegency, edtCityRegency)
        setEditTextValue(address.district, edtDistrict)
        setEditTextValue(address.village, edtVillage)
        setEditTextValue(address.rw, edtRw)
        setEditTextValue(address.rt, edtRt)
        setEditTextValue(address.street, edtStreet)
        setEditTextValue(address.otherDetailAddress, edtAddressOtherDetail)
    }

    private fun setOperationIdentityPersonalAndParent(personal: () -> Unit, parent: () -> Unit) {
        if (addressData?.isStudentIdentityParent == true) {
            parent()
        } else {
            personal()
        }
    }

    private fun sendCallbackIntentResultDeleted() {
        val message = getString(
            R.string.text_alert_delete_format,
            getString(R.string.text_success),
            getString(R.string.text_address_home)
        )
        val resultIntent = Intent().apply {
            putExtra(KEY_EXTRA_SUCCESS, message)
        }
        setResult(KEY_RESULT_CODE_DELETED, resultIntent)
        finish()
    }

    private fun alertSuccess(message: String) {
        showAlertDialog(msg = message, status = STATUS_SUCCESS)
    }

    private fun alertError(message: String) {
        showAlertDialog(msg = message, status = STATUS_ERROR)
    }

    private fun alertDelete(
        token: String = strEmpty, userId: String, userType: String = strEmpty,
    ) {
        showAlertDialog(
            msg = getString(R.string.text_address_home_this),
            status = STATUS_CONFIRM_DELETE,
            userId = userId,
            userType = userType,
            token = token
        )
    }

    private fun showAlertDialog(
        msg: String = strEmpty,
        status: String,
        userId: String = strEmpty,
        userType: String = strEmpty,
        token: String = strEmpty,
    ) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = strEmpty
        var message = strEmpty
        var icon: Drawable? = null
        when (status) {
            STATUS_HAS_CHANGED -> {
                icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_warning)
                title = getString(R.string.text_changes_not_saved)
                message = getString(R.string.text_exit_not_saved)
            }

            STATUS_PROFILE_FRAGMENT -> {
                icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_person)
                title = getString(R.string.text_profile)
                message = getString(R.string.text_question_do_you_want_to_back_profile_page)
            }

            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(contextActivity, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = msg
            }

            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_warning)
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error, "")
                    message = msg
                }

            }

            STATUS_CONFIRM_DELETE -> {
                icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_delete)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(contextActivity, R.color.md_theme_error)
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

            dialog = MaterialAlertDialogBuilder(contextActivity).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (status != STATUS_SUCCESS) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        defaultStateDialog()
                        when (status) {
                            STATUS_HAS_CHANGED -> finish()

                            STATUS_PROFILE_FRAGMENT -> goToProfileFragment(context)

                            STATUS_ERROR -> {
                                if (unauthorized) authViewModel.saveUser(
                                    null,
                                    null,
                                    null
                                ) else return@setPositiveButton
                            }

                            STATUS_CONFIRM_DELETE -> {
                                defaultStateDialog()
                                setOperationIdentityPersonalAndParent(
                                    personal = {
                                        deleteIdentityPersonalAddressLiveData(
                                            userType,
                                            userId
                                        )
                                    },
                                    parent = {
                                        deleteIdentityParentAddressLiveData(
                                            token,
                                            userId,
                                        )
                                    }
                                )

                            }
                        }
                    }
                    when (status) {
                        STATUS_HAS_CHANGED, STATUS_PROFILE_FRAGMENT, STATUS_CONFIRM_DELETE -> {
                            setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                                defaultStateDialog()
                                return@setNegativeButton
                            }
                        }
                    }
                }

            }.create()
        }



        if (!isAlertDialogShow) {
            isAlertDialogShow = true
            dialog?.show()
        }


    }

    private fun setEditTextValue(value: String? = null, editText: TextInputEditText) {
        if (value.isNullOrEmpty()) {
            editText.text = null
        } else {
            editText.setText(value)
        }
    }

    private fun hasChanges(): Boolean {
        binding.apply {
            return addressData?.province.orEmpty() != edtProvince.text.toString() ||
                    addressData?.cityRegency.orEmpty() != edtCityRegency.text.toString() ||
                    addressData?.district.orEmpty() != edtDistrict.text.toString() ||
                    addressData?.village.orEmpty() != edtVillage.text.toString() ||
                    addressData?.rw.orEmpty() != edtRw.text.toString() ||
                    addressData?.rt.orEmpty() != edtRt.text.toString() ||
                    addressData?.street.orEmpty() != edtStreet.text.toString() ||
                    addressData?.otherDetailAddress.orEmpty() != edtAddressOtherDetail.text.toString()
        }
    }


    private fun checkButtonSaveIsEnabled(
        layouts: Array<TextInputLayout>,
        editTexts: Array<TextInputEditText>,
    ) {
        binding.btnSave.isEnabled =
            editTexts.any { it.text.toString().isNotEmpty() } && layouts.all { !it.isErrorEnabled }

    }


    private fun checkButtonDeleteIsEnabled(userId: String?) = binding.btnDelete.apply {
        isEnabled = userId != null
        isVisible = isEnabled
    }

    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            binding.apply {
                inputLayoutProvince.editText?.clearFocus()
                inputLayoutCityRegency.editText?.clearFocus()
                inputLayoutDistrict.editText?.clearFocus()
                inputLayoutVillage.editText?.clearFocus()
                inputLayoutRw.editText?.clearFocus()
                inputLayoutRt.editText?.clearFocus()
                inputLayoutStreet.editText?.clearFocus()
                inputLayoutAddressOtherDetail.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(contextActivity))
        clearFocus()
    }

    private fun showLoadingView(boolean: Boolean) = lifecycleScope.launch {
        showLoading(binding.mainProgressBar, boolean)
        if (boolean) {
            showDefaultView(false)
            showFailedConnectView(false)
        }
    }

    private fun showDefaultView(boolean: Boolean) {
        binding.apply {

            if (boolean) {
                toolbar.visibility = View.VISIBLE
                inputLayoutProvince.visibility = View.VISIBLE
                edtProvince.visibility = View.VISIBLE
                inputLayoutCityRegency.visibility = View.VISIBLE
                edtCityRegency.visibility = View.VISIBLE
                inputLayoutDistrict.visibility = View.VISIBLE
                edtDistrict.visibility = View.VISIBLE
                inputLayoutVillage.visibility = View.VISIBLE
                edtVillage.visibility = View.VISIBLE
                inputLayoutRw.visibility = View.VISIBLE
                edtRw.visibility = View.VISIBLE
                inputLayoutRt.visibility = View.VISIBLE
                edtRt.visibility = View.VISIBLE
                inputLayoutStreet.visibility = View.VISIBLE
                edtStreet.visibility = View.VISIBLE
                inputLayoutAddressOtherDetail.visibility = View.VISIBLE
                edtAddressOtherDetail.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                inputLayoutProvince.visibility = View.GONE
                edtProvince.visibility = View.GONE
                inputLayoutCityRegency.visibility = View.GONE
                edtCityRegency.visibility = View.GONE
                inputLayoutDistrict.visibility = View.GONE
                edtDistrict.visibility = View.GONE
                inputLayoutVillage.visibility = View.GONE
                edtVillage.visibility = View.GONE
                inputLayoutRw.visibility = View.GONE
                edtRw.visibility = View.GONE
                inputLayoutRt.visibility = View.GONE
                edtRt.visibility = View.GONE
                inputLayoutStreet.visibility = View.GONE
                edtStreet.visibility = View.GONE
                inputLayoutAddressOtherDetail.visibility = View.GONE
                edtAddressOtherDetail.visibility = View.GONE
                btnDelete.visibility = View.GONE
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
        SnackBarHelper.display(binding.root as ViewGroup, message, contextActivity)
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
        outState.putParcelable(KEY_BUNDLE_ADDRESS, addressData)
        outState.putParcelable(KEY_BUNDLE_IDENTITY_PARENT, studentIdentityParentData)
        outState.putBoolean(KEY_BUNDLE_HAS_ADDRESS, hasAddress)
        outState.putBoolean(KEY_BUNDLE_HAS_IDENTITY_PARENT, hasIdentityParent)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATUS_HAS_CHANGED = "status_has_changed"
        private const val STATUS_PROFILE_FRAGMENT = "status_profile_fragment"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val STATUS_CONFIRM_DELETE = "status_confirm_delete"

        private const val KEY_BUNDLE_ADDRESS = "key_bunlde_address"
        private const val KEY_BUNDLE_IDENTITY_PARENT = "key_bundle_identity_parent"
        private const val KEY_BUNDLE_HAS_ADDRESS = "key_bundle_has_address"
        private const val KEY_BUNDLE_HAS_IDENTITY_PARENT = "key_bundle_has_identity_parent"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 2120
        const val KEY_RESULT_CODE_DELETED = 2121
    }
}