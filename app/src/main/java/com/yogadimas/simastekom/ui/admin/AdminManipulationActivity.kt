package com.yogadimas.simastekom.ui.admin

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
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import com.yogadimas.simastekom.databinding.ActivityAdminManipulationBinding
import com.yogadimas.simastekom.databinding.LayoutAdminManipulationTextInputs1Binding
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.ProfilePictureData
import com.yogadimas.simastekom.ui.dialog.GenderDialogFragment
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity
import com.yogadimas.simastekom.ui.identity.profilepicture.ProfilePictureEditActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminAdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

typealias VStub1BindingAdmin = LayoutAdminManipulationTextInputs1Binding

class AdminManipulationActivity : AppCompatActivity(), OnOptionDialogListenerInterface {

    private lateinit var binding: ActivityAdminManipulationBinding
    private val activityContext = this@AdminManipulationActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }
    private val viewmodel: AdminAdminViewModel by viewModel()

    private lateinit var vStub1: ViewStub
    private var vStub1Binding: VStub1BindingAdmin? = null

    private var isAlertDialogShow = false
    private var dialog: AlertDialog? = null

    private val emptyString = Str.EMPTY.value

    private val isEditDeleteMode: Boolean
        get() = adminData?.userId?.isNotEmpty() == true &&
                adminData?.userType?.isNotEmpty() == true

    private var adminData: AdminData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupAuth()
        setupAdminDataBundle(savedInstanceState)
        setupMainContent()
    }


    private fun setupView() {
        binding = ActivityAdminManipulationBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        vStub1 = binding.vs1

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setStatusBarColor(window)
    }

    private fun setStatusBarColor(window: Window) {
        val typedValue = TypedValue()
        val theme = window.context.theme
        theme.resolveAttribute(R.color.md_theme_surface, typedValue, true)
        val color = typedValue.data
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = color
            val isLight = resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_NO

            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = isLight
                isAppearanceLightNavigationBars = isLight
            }
        }
    }


    private fun setupAuth() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (getToken() == AuthPreferences.DEFAULT_VALUE) goToLogin(activityContext)
        }
    }

    private suspend fun getToken(): String = authViewModel.getUser().asFlow().first().first


    private fun setupAdminDataBundle(savedInstanceState: Bundle?) {
        adminData = savedInstanceState?.getParcelableCompat(KEY_BUNDLE_ADMIN)
            ?: AdminData()
    }

    private fun showToolbarAddMode() { setupToolbar(title = getString(R.string.text_add)) }

    private fun showToolbarEditDeleteMode() {
        setupToolbar(getString(R.string.text_change_or_delete))
        setupToolbarMenu(
            R.menu.top_appbar_delete_menu,
            R.id.deleteMenu,
            R.color.md_theme_error
        ) { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteMenu -> {
                    adminData?.let {
                        alertDelete(
                            it.userId.orEmpty(),
                            it.username.orEmpty(),
                            it.name.orEmpty()
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

    private fun setupMainContent() = lifecycleScope.launch {
        showToolbar(false)
        val data = adminData ?: AdminData()
        data.apply {
            userId = intent.getStringExtra(KEY_EXTRA_ID).orEmpty()
            userType = intent.getStringExtra(KEY_EXTRA_USER_TYPE).orEmpty()
        }
        binding.viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getDataByMode(data) }
        initialGetData(data)
    }

    private fun initialGetData(data: AdminData) {
        getDataByMode(data)
    }

    private fun getDataByMode(data: AdminData) {
        if (isEditDeleteMode) editDeleteMode(data) else addMode(data)
    }


    private fun addMode(data: AdminData) = execute { token ->
        withContext(Dispatchers.Main) {
            showSmoothLoadingView()
            showDataAdminManipulationView(token, data)
        }
    }

    private fun editDeleteMode(data: AdminData) = execute { token ->
        withContext(Dispatchers.Main) {
            showSmoothLoadingView()
            viewmodel.getAdminById(
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
        lifecycleScope.launch { action(getToken()); collectAdminState(getToken()) }


    private suspend fun collectAdminState(token: String) {
        viewmodel.adminState.collect { state ->
            when (state) {
                is State.Loading -> showLoadingView(true)
                is State.Success -> showDataAdminManipulationView(token, state.data)
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

    private fun showDataAdminManipulationView(token: String, data: AdminData) {
        showLoadingView(false)

        if (data.isAdded || data.isUpdated || data.isDeleted) {
            val success = getString(R.string.text_success)
            val label = getString(R.string.text_admin)
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
                    getString(R.string.text_label_id_username),
                    getString(R.string.text_other_admin)
                )

            message.containsIgnoreCase(ErrorMessage.UNREGISTERED.value) ->
                getString(
                    R.string.text_error_msg_unregistered_format,
                    getString(R.string.text_admin)
                )

            message.containsIgnoreCase(ErrorMessage.USED.value) ->
                getString(
                    R.string.text_error_msg_used_format,
                    getString(R.string.text_label_id_username),
                    getString(R.string.text_other_admin)
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


    private fun setupViewStubWithData(token: String, data: AdminData) {
        if (vStub1.parent != null) {
            initializeViewStubBindingIfNull()
            setupFirstInputLayout(vStub1Binding!!, token, data)
        } else {
            setupFirstInputLayout(vStub1Binding!!, token, data)
        }

    }

    private fun setupFirstInputLayout(vsb1: VStub1BindingAdmin, token: String, data: AdminData) {
        showToolbar(true)
        vsb1.apply {
            if (isEditDeleteMode) setupEditMode(data) else setupAddMode(data)

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

    private fun VStub1BindingAdmin.setupEditMode(data: AdminData) {
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
                IdentityPersonalEditActivity.KEY_ADMIN_ADMIN,
                IdentityPersonalEditActivity::class.java,
                IdentityPersonalData(
                    userId = adminData?.userId,
                    userType = adminData?.userType,
                    isFromAdmin = true
                )
            )
        }

        btnDelete.setOnClickListener {
            adminData?.let {
                alertDelete(
                    it.userId.orEmpty(),
                    it.username.orEmpty(),
                    it.name.orEmpty()
                )
            }
        }

    }

    private fun VStub1BindingAdmin.setupAddMode(data: AdminData) {
        animateViewStub(this)

        setupCommonInputs(data)

        layoutNavigation.isVisible = false
        btnIdentityPersonal.isEnabled = false

        btnDelete.isVisible = false
        btnDelete.isEnabled = false
    }

    private fun VStub1BindingAdmin.setupCommonInputs(data: AdminData) {
        adminData?.apply {
            name = data.name
            password = data.password
            confirmPassword = data.confirmPassword
            username = data.username
            gender = data.gender
        }?.let {
            edtFullName.setText(it.name)
            edtAdminIdUsername.setText(it.username)
            setGender(this, it.gender)
            if (!isEditDeleteMode) {
                edtPassword.setText(it.password)
                edtConfirmPassword.setText(it.confirmPassword)
            }
        }

    }

    private fun VStub1BindingAdmin.setupTextWatchersForInputs() {
        setupTextWatchers(edtFullName) { adminData?.name = it }
        setupTextWatchers(edtPassword) { adminData?.password = it }
        setupTextWatchers(edtConfirmPassword) { adminData?.confirmPassword = it }
        setupTextWatchers(edtAdminIdUsername) { adminData?.username = it }
        setupTextWatchers(edtGender) { adminData?.gender = it.lowercase() }
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

    private fun LayoutAdminManipulationTextInputs1Binding.setupGenderDialog() {
        edtGender.setOnClickListener {
            hideKeyboard()
            GenderDialogFragment().show(
                supportFragmentManager,
                GenderDialogFragment::class.java.simpleName
            )
        }
    }

    private fun setGender(
        viewBinding1: LayoutAdminManipulationTextInputs1Binding,
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
            adminData?.let { data ->
                viewmodel.updateAdmin(token, data.userId.orEmpty(), data)
            }
        } else {
            showAllView(false)
            adminData?.let { data ->
                if (isValidPassword(data)) {
                    viewmodel.addAdmin(token, data)
                } else {
                    showAllView(true)
                }
            }
        }

    }


    private fun isValidPassword(data: AdminData): Boolean {
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
                getString(R.string.text_label_password),
                MIN_PASSWORD_LENGTH
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
                inputLayoutAdminIdUsername.editText?.clearFocus()
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
                edtAdminIdUsername,
                edtGender,
            ).all { it.text.toString().isNotEmpty() }
        } == true

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

    private fun alertDelete(userId: String, username: String, fullName: String) {
        showAlertDialog(
            msg = getString(R.string.text_string_strip_string_format, username, fullName),
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
                            deleteAdmin(userId)
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

    private fun deleteAdmin(userId: String) = lifecycleScope.launch {
        showAllView(false)
        viewmodel.deleteAdmin(getToken(), userId)
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
            vStub1Binding = LayoutAdminManipulationTextInputs1Binding.bind(vStub1.inflate())
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
        outState.putParcelable(KEY_BUNDLE_ADMIN, adminData)
        super.onSaveInstanceState(outState)
    }


    companion object {
        private const val STATUS_CONFIRM_DELETE = "status_confirm_delete"
        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_ID = "key_extra_id"
        const val KEY_EXTRA_USER_TYPE = "key_extra_user_type"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 200

        const val KEY_BUNDLE_ADMIN = "key_bundle_admin"

        private const val MIN_PASSWORD_LENGTH = 6
        private const val DELAY_TIME = 600L
    }

}