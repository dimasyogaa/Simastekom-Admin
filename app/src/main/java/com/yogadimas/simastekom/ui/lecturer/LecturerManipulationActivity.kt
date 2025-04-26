package com.yogadimas.simastekom.ui.lecturer

import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorMessage
import com.yogadimas.simastekom.common.enums.ErrorPassword
import com.yogadimas.simastekom.common.enums.HttpResponseType
import com.yogadimas.simastekom.common.enums.Str
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.animateViewStub
import com.yogadimas.simastekom.common.helper.getParcelableCompat
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.movePageWithParcelable
import com.yogadimas.simastekom.common.helper.showLoadingFade
import com.yogadimas.simastekom.common.interfaces.OnOptionDialogListenerInterface
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityLecturerManipulationBinding
import com.yogadimas.simastekom.databinding.LayoutLecturerManipulationTextInputs1Binding
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.LecturerData
import com.yogadimas.simastekom.model.responses.ProfilePictureData
import com.yogadimas.simastekom.ui.dialog.GenderDialogFragment
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity
import com.yogadimas.simastekom.ui.identity.profilepicture.ProfilePictureEditActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminLecturerViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

typealias VStub1Binding = LayoutLecturerManipulationTextInputs1Binding

class LecturerManipulationActivity : AppCompatActivity(), OnOptionDialogListenerInterface {

    private lateinit var binding: ActivityLecturerManipulationBinding
    private val activityContext = this@LecturerManipulationActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }
    private val viewmodel: AdminLecturerViewModel by viewModel()

    private lateinit var vStub1: ViewStub
    private var vStub1Binding: VStub1Binding? = null

    private var isAlertDialogShow = false
    private var dialog: AlertDialog? = null

    private val emptyString = Str.EMPTY.value

    private val isEditDeleteMode: Boolean
        get() = lecturerData?.userId?.isNotEmpty() == true &&
                lecturerData?.userType?.isNotEmpty() == true

    private var lecturerData: LecturerData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturerManipulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vStub1 = binding.vs1

        auth()

        setLectureDataBundle(savedInstanceState)

        mainContent()

    }

    private fun auth() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (getToken() == AuthPreferences.DEFAULT_VALUE) goToLogin(activityContext)
        }
    }


    private fun setLectureDataBundle(savedInstanceState: Bundle?) {
        lecturerData = savedInstanceState?.getParcelableCompat(KEY_BUNDLE_LECTURER)
            ?: LecturerData()
    }


    private fun mainContent() = lifecycleScope.launch {
        showToolbar(false)
        val data = lecturerData ?: LecturerData()
        data.apply {
            userId = intent.getStringExtra(KEY_EXTRA_ID).orEmpty()
            userType = intent.getStringExtra(KEY_EXTRA_USER_TYPE).orEmpty()
        }
        binding.viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getDataByMode(data) }
        initialGetData(data)
    }


    private fun showToolbarAddMode() {
        setupToolbar(title = getString(R.string.text_add))
    }

    private fun showToolbarEditDeleteMode() {
        setupToolbar(getString(R.string.text_change_or_delete))
        setupToolbarMenu(
            R.menu.top_appbar_delete_menu,
            R.id.deleteMenu,
            R.color.md_theme_error
        ) { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteMenu -> {
                    lecturerData?.let {
                        alertDelete(
                            it.userId.orEmpty(),
                            it.lecturerIdNumber.orEmpty(),
                            it.fullName.orEmpty()
                        )
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun setupToolbar(
        title: String? = null,
    ) = binding.toolbar.apply {
        showToolbar(false)
        setNavigationOnClickListener { finish() }
        title?.let { this.title = it }
    }

    private fun setupToolbarMenu(
        menuRes: Int,
        iconItemId: Int,
        iconColorRes: Int,
        onMenuItemClick: (MenuItem) -> Boolean,
    ) {
        binding.toolbar.apply {
            menu.clear()
            menuInflater.inflate(menuRes, menu)
            val icon = menu.findItem(iconItemId)?.icon?.mutate()
            val color = ContextCompat.getColor(context, iconColorRes)
            icon?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
                } else {
                    @Suppress("DEPRECATION")
                    it.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                }
            }
            setOnMenuItemClickListener(onMenuItemClick)
        }
    }


    private fun initialGetData(data: LecturerData) {
        getDataByMode(data)
    }

    private fun getDataByMode(data: LecturerData) {
        if (isEditDeleteMode) editDeleteMode(data) else addMode(data)
    }


    private suspend fun getToken(): String = authViewModel.getUser().asFlow().first().first


    private fun addMode(data: LecturerData) = execute { token ->
        withContext(Dispatchers.Main) {
            showSmoothLoadingView()
            showDataLecturerManipulationView(token, data)
        }
    }

    private fun editDeleteMode(data: LecturerData) = execute { token ->
        withContext(Dispatchers.Main) {
            showSmoothLoadingView()
            viewmodel.getLecturerById(
                token,
                data.userId.orEmpty()
            )
        }
    }

    private suspend fun showSmoothLoadingView() {
        showLoadingView(true)
        delay(DELAY_TIME)
    }


    private fun execute(action: suspend (String) -> Unit) =
        lifecycleScope.launch { action(getToken()); collectLecturerState(getToken()) }


    private suspend fun collectLecturerState(token: String) {
        viewmodel.lecturerState.collect { state ->
            when (state) {
                is State.Loading -> showLoadingView(true)
                is State.Success -> showDataLecturerManipulationView(token, state.data)
                is State.ErrorClient -> showErrorClient(state.error)
                is State.ErrorServer -> showErrorServer(state.error)
            }
        }
    }

    private fun showLoadingView(isVisible: Boolean) {

        if (isVisible) {
            showToolbar(false)
            showFailedConnectView(false)
        }

        showLoadingFade(binding.mainProgressBar, isVisible)
    }


    private fun showDataLecturerManipulationView(token: String, data: LecturerData) {
        showLoadingView(false)

        if (data.isAdded || data.isUpdated || data.isDeleted) {
            val success = getString(R.string.text_success)
            val label = getString(R.string.text_lecturer)
            val resultIntent = Intent()

            val msg = when {
                data.isAdded -> R.string.text_alert_add_format
                data.isUpdated -> R.string.text_alert_update_format
                else -> R.string.text_alert_delete_format
            }

            resultIntent.putExtra(
                KEY_EXTRA_SUCCESS,
                getString(msg, success, label)
            )

            setResult(KEY_RESULT_CODE, resultIntent)
            finish()
        } else {
            lifecycleScope.launch {
                delay(1)
                setupViewStubWithData(token, data)
            }
        }
        if (isEditDeleteMode) showToolbarEditDeleteMode() else showToolbarAddMode()

    }

    private fun showErrorClient(error: Errors) {
        showLoadingView(false)

        val message = error.errors?.message?.first() ?: emptyString

        val formattedMessage = when {
            message.containsIgnoreCase(ErrorMessage.REGISTERED.value) ->
                getString(
                    R.string.text_error_msg_registered_format,
                    getString(R.string.text_label_lecturer_id_number),
                    getString(R.string.text_other_lecturer)
                )

            message.containsIgnoreCase(ErrorMessage.UNREGISTERED.value) ->
                getString(
                    R.string.text_error_msg_unregistered_format,
                    getString(R.string.text_lecturer)
                )

            message.containsIgnoreCase(ErrorMessage.USED.value) ->
                getString(
                    R.string.text_error_msg_used_format,
                    getString(R.string.text_label_lecturer_id_number),
                    getString(R.string.text_other_lecturer)
                )

            else -> message
        }



        if (formattedMessage.containsIgnoreCase(ErrorMessage.UNAUTHORIZED.value)) {
            showFailedConnectView(true)
            alertError(formattedMessage, isUnAuthorized = true)
            return
        }
        alertError(formattedMessage)
        showAllView(true)

    }

    private fun String.containsIgnoreCase(value: String): Boolean =
        this.contains(value, ignoreCase = true)

    private fun showErrorServer(error: String) {
        showLoadingView(false)
        showFailedConnectView(true)
        showSnackBar(error)
    }


    private fun setupViewStubWithData(token: String, data: LecturerData) {
        if (vStub1.parent != null) {
            initializeViewStubBindingIfNull()
            setupFirstInputLayout(vStub1Binding!!, token, data)
        } else {
            setupFirstInputLayout(vStub1Binding!!, token, data)
        }

    }

    private fun setupFirstInputLayout(vsb1: VStub1Binding, token: String, data: LecturerData) {


        showToolbar(true)
        vsb1.apply {
            if (isEditDeleteMode) {
                setupEditMode(data)
            } else {
                setupAddMode(data)
            }
            setupTextWatchersForInputs()
            setupGenderDialog()
            checkSaveButtonIsEnabled()
            btnProfilePicture.setOnClickListener {
                movePageWithParcelable(
                    key = ProfilePictureEditActivity.KEY_ADMIN,
                    destination = ProfilePictureEditActivity::class.java,
                    data = ProfilePictureData(
                        userId = data.userId,
                        userType = data.userType
                    )
                )
            }
            btnSave.setOnClickListener { save(token) }
            if (!root.isVisible) showViewStub(true)
        }


    }

    private fun VStub1Binding.setupEditMode(data: LecturerData) {
        inputLayoutPassword.isVisible = false
        edtPassword.isVisible = false
        inputLayoutConfirmPassword.isVisible = false
        edtConfirmPassword.isVisible = false
        animateViewStub(this)

        setupCommonInputs(data)

        btnDelete.isVisible = true
        btnDelete.isEnabled = true

        layoutNavigation.isVisible = true
        btnIdentityPersonal.isEnabled = true

        btnIdentityPersonal.setOnClickListener {
            movePageWithParcelable(
                IdentityPersonalEditActivity.KEY_ADMIN_LECTURER,
                IdentityPersonalEditActivity::class.java,
                IdentityPersonalData(
                    userId = lecturerData?.userId,
                    userType = lecturerData?.userType,
                    isFromAdmin = true
                )
            )
        }

        btnDelete.setOnClickListener {
            lecturerData?.let {
                alertDelete(
                    it.userId.orEmpty(),
                    it.lecturerIdNumber.orEmpty(),
                    it.fullName.orEmpty()
                )
            }
        }

    }

    private fun VStub1Binding.setupAddMode(data: LecturerData) {
        animateViewStub(this)

        setupCommonInputs(data)

        layoutNavigation.isVisible = false
        btnIdentityPersonal.isEnabled = false

        btnDelete.isVisible = false
        btnDelete.isEnabled = false
    }

    private fun VStub1Binding.setupCommonInputs(data: LecturerData) {
        lecturerData?.apply {
            fullName = data.fullName
            password = data.password
            confirmPassword = data.confirmPassword
            lecturerIdNumber = data.lecturerIdNumber
            degree = data.degree
            gender = data.gender
        }?.let {
            edtFullName.setText(it.fullName)
            edtLecturerIdNumber.setText(it.lecturerIdNumber)
            edtDegree.setText(it.degree)
            setGender(this, it.gender)
            if (!isEditDeleteMode) {
                edtPassword.setText(it.password)
                edtConfirmPassword.setText(it.confirmPassword)
            }
        }

    }

    private fun VStub1Binding.setupTextWatchersForInputs() {
        setupTextWatchers(edtFullName) { lecturerData?.fullName = it }
        setupTextWatchers(edtPassword) { lecturerData?.password = it }
        setupTextWatchers(edtConfirmPassword) { lecturerData?.confirmPassword = it }
        setupTextWatchers(edtLecturerIdNumber) { lecturerData?.lecturerIdNumber = it }
        setupTextWatchers(edtDegree) { lecturerData?.degree = it }
        setupTextWatchers(edtGender) { lecturerData?.gender = it.lowercase() }
    }

    private fun setupTextWatchers(editText: TextInputEditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                onTextChanged(p0.toString())
                checkSaveButtonIsEnabled()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun LayoutLecturerManipulationTextInputs1Binding.setupGenderDialog() {
        edtGender.setOnClickListener {
            hideKeyboard()
            GenderDialogFragment().show(
                supportFragmentManager,
                GenderDialogFragment::class.java.simpleName
            )
        }
    }

    private fun setGender(
        viewBinding1: LayoutLecturerManipulationTextInputs1Binding,
        gender: String?,
    ) {
        viewBinding1.apply {
            edtGender.setText(
                when (gender?.lowercase()) {
                    getString(R.string.text_man).lowercase() -> getString(R.string.text_man)
                    getString(R.string.text_woman).lowercase() -> getString(R.string.text_woman)
                    else -> emptyString
                }
            )
        }
    }


    private fun save(
        token: String,
    ) {
        hideKeyboard()
        if (isEditDeleteMode) {
            showAllView(false)
            lecturerData?.let { data ->
                viewmodel.updateLecturer(token, data.userId.orEmpty(), data)
            }
        } else {
            showAllView(false)
            lecturerData?.let { data ->
                if (isValidPassword(data)) {
                    viewmodel.addLecturer(token, data)
                } else {
                    showAllView(true)
                }
            }
//            dummyAddOperation()

        }

    }

    private fun dummyAddOperation() {
        showAllView(false)
        lecturerData?.isAdded = true
        lecturerData?.let { data ->
            if (isValidPassword(data)) {
                viewmodel.addLecturerDummy(HttpResponseType.SUCCESS, data)
            }
        }
    }

    private fun isValidPassword(data: LecturerData): Boolean {
        if ((data.password?.length ?: 0) < 6) {
            showErrorPassword(ErrorPassword.MIN_CHAR)
        } else if (!data.password.equals(data.confirmPassword)) {
            showErrorPassword(ErrorPassword.NOT_SAME)
        } else {
            return true
        }
        return false
    }

    private fun showErrorPassword(errorType: ErrorPassword) {
        binding.root.scrollY = 0
        val message = when (errorType) {
            ErrorPassword.MIN_CHAR -> getString(
                R.string.text_min_character_field_format,
                getString(R.string.text_label_password), MIN_PASSWORD_LENGTH
            )

            ErrorPassword.NOT_SAME -> getString(
                R.string.text_make_sure_confirm_matches_entered_format,
                getString(R.string.text_label_password)
            )
        }
        alertError(message)
        vStub1Binding?.apply {
            edtPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            edtConfirmPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
    }

    private fun showAllView(value: Boolean) {
        initializeViewStubBindingIfNull()
        showToolbar(value)
        showViewStub(value)
    }


    private fun showToolbar(value: Boolean) = binding.toolbar.apply {
        isInvisible = !value
    }

    private fun showViewStub(value: Boolean) = vStub1Binding?.root?.apply {
        isVisible = value
    }

    private fun showFailedConnectView(value: Boolean) =
        binding.viewHandle.viewFailedConnect.root.apply {
            isVisible = value
        }


    private fun showSnackBar(message: String) {
        SnackBarHelper.display(binding.root as ViewGroup, message, activityContext)
    }

    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            initializeViewStubBindingIfNull()
            vStub1Binding?.apply {
                inputLayoutFullName.editText?.clearFocus()
                if (!isEditDeleteMode) {
                    inputLayoutPassword.editText?.clearFocus()
                    inputLayoutConfirmPassword.editText?.clearFocus()
                }
                inputLayoutLecturerIdNumber.editText?.clearFocus()
                inputLayoutDegree.editText?.clearFocus()
                inputLayoutGender.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(activityContext))
        clearFocus()
    }


    private fun checkSaveButtonIsEnabled() {
        val viewBinding1isNotEmpty = vStub1Binding?.run {
            listOf(
                edtFullName,
                edtLecturerIdNumber,
                edtDegree,
                edtGender,
            ).all { it.text.toString().isNotEmpty() }
        } ?: false


        val isPasswordNotEmpty = vStub1Binding?.run {
            edtPassword.text.toString().isNotEmpty() && edtConfirmPassword.text.toString()
                .isNotEmpty()
        } == true

        vStub1Binding?.btnSave?.isEnabled = if (isEditDeleteMode) {
            viewBinding1isNotEmpty
        } else {
            viewBinding1isNotEmpty && isPasswordNotEmpty
        }

    }


    private fun alertError(message: String, isUnAuthorized: Boolean = false) {
        showAlertDialog(isUnAuthorized = isUnAuthorized, msg = message, status = STATUS_ERROR)
    }

    private fun alertDelete(userId: String, lecturerIdNumber: String, fullName: String) {
        showAlertDialog(
            msg = getString(R.string.text_string_strip_string_format, lecturerIdNumber, fullName),
            status = STATUS_CONFIRM_DELETE,
            userId = userId
        )
    }

    private fun showAlertDialog(
        isUnAuthorized: Boolean = false,
        msg: String = emptyString,
        status: String,
        userId: String = emptyString,
    ) {
        val (icon, title, message) = buildDialogContent(status, isUnAuthorized, msg)

        if (dialog == null) {
            dialog = MaterialAlertDialogBuilder(activityContext).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)

                when (status) {
                    STATUS_ERROR -> setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        logout(isUnAuthorized)
                        dismissAlertDialog()
                    }

                    STATUS_CONFIRM_DELETE -> {
                        setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                            deleteLecturer(userId)
                            dismissAlertDialog()
                        }
                        setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                            dismissAlertDialog()
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

    private fun logout(isUnAuthorized: Boolean) {
        if (isUnAuthorized) {
            authViewModel.saveUser(null, null, null)
            goToLogin(activityContext)
        }
    }

    private fun deleteLecturer(userId: String) = lifecycleScope.launch {
        showAllView(false)
        viewmodel.deleteLecturer(getToken(), userId)
    }


    private fun buildDialogContent(
        status: String,
        isUnAuthorized: Boolean,
        msg: String,
    ): Triple<Drawable?, String, String> {
        return when (status) {
            STATUS_ERROR -> {
                val icon = ContextCompat.getDrawable(activityContext, R.drawable.z_ic_warning)
                val title =
                    if (isUnAuthorized) getString(R.string.text_login_again) else getString(R.string.text_error)
                val message =
                    if (isUnAuthorized) getString(R.string.text_please_login_again) else msg
                Triple(icon, title, message)
            }

            STATUS_CONFIRM_DELETE -> {
                val baseIcon = ContextCompat.getDrawable(activityContext, R.drawable.z_ic_delete)
                val icon = DrawableCompat.wrap(baseIcon!!).mutate().apply {
                    val color = ContextCompat.getColor(activityContext, R.color.md_theme_error)
                    DrawableCompat.setTint(this, color)
                }
                val title = getString(R.string.text_delete)
                val message = getString(R.string.text_question_do_you_want_to_delete_format, msg)
                Triple(icon, title, message)
            }

            else -> Triple(null, "", "")
        }
    }


    override fun onOptionChosen(text: String, category: String) {
        when (category) {
            GenderDialogFragment.KEY_OPTION_GENDER -> {
                vStub1Binding?.apply {
                    setGender(this@apply, text)
                    edtGender.setText(text)
                }
            }
        }
    }

    private fun initializeViewStubBindingIfNull() {
        if (vStub1Binding == null) {
            vStub1Binding = LayoutLecturerManipulationTextInputs1Binding.bind(vStub1.inflate())
        }
    }

    private fun dismissAlertDialog() {
        isAlertDialogShow = false
        dialog?.dismiss()
        dialog = null
    }

    override fun onPause() {
        super.onPause()
        dismissAlertDialog()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_BUNDLE_LECTURER, lecturerData)
        super.onSaveInstanceState(outState)
    }


    companion object {
        private const val STATUS_CONFIRM_DELETE = "status_confirm_deleted"
        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_ID = "key_extra_id"
        const val KEY_EXTRA_USER_TYPE = "key_extra_user_type"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 200

        const val KEY_BUNDLE_LECTURER = "key_bundle_lecturer"

        private const val MIN_PASSWORD_LENGTH = 6
        private const val DELAY_TIME = 600L


    }


}

