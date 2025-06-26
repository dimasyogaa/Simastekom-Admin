package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_contact

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewStub
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.core.common.customs.DialogAlert
import com.yogadimas.simastekom.core.common.enums.ErrorType
import com.yogadimas.simastekom.core.common.extensions.dialogMessageSuccess
import com.yogadimas.simastekom.core.common.extensions.getIntData
import com.yogadimas.simastekom.core.common.extensions.getParcelableData
import com.yogadimas.simastekom.core.common.extensions.hideKeyBoard
import com.yogadimas.simastekom.core.common.extensions.setInputLauncher
import com.yogadimas.simastekom.core.common.extensions.setManipulation
import com.yogadimas.simastekom.core.common.extensions.setupTextWatchers
import com.yogadimas.simastekom.core.common.extensions.showConfirmDeleteDialog
import com.yogadimas.simastekom.core.common.extensions.showLoadingFade
import com.yogadimas.simastekom.core.common.model.IdNameResult
import com.yogadimas.simastekom.core.data.source.remote.request.ImportantContactRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.ui.UiState
import com.yogadimas.simastekom.core.utils.ErrorClient
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showSnackBarErrorServer
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnAuthorizedDialog
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnknownDialog
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ActivitySmimportantContactManipulationBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.LayoutFormImportantContactBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_category.SMImportantContactCategoryActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private typealias VStubImportantContact = LayoutFormImportantContactBinding

class SMImportantContactManipulationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmimportantContactManipulationBinding
    private val activityContext = this@SMImportantContactManipulationActivity

    private val viewmodel: SMImportantContactViewModel by viewModel()

    private val dialogAlert: DialogAlert by inject {
        parametersOf(
            activityContext, activityContext
        )
    }

    private lateinit var vStub: ViewStub
    private var vStubBinding: VStubImportantContact? = null


    private val isCreatedMode: Boolean get() = importantContact.id == 0

    private var importantContact: ImportantContactRequest = ImportantContactRequest()

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleIntentResult(result.data, result.resultCode)
    }

    private fun handleIntentResult(intent: Intent?, code: Int) {
        intent?.let { setupIntentResult(code, it) }
    }

    private fun setupIntentResult(code: Int, intent: Intent) {
        when (code) {
            SMImportantContactCategoryActivity.KEY_IMPORTANT_CONTACT_RESULT_CODE -> {
                setupCategoryResult(intent)
            }
        }
    }

    private fun setupCategoryResult(intent: Intent) {

        val data = intent.getParcelableData<IdNameResult>(
            SMImportantContactCategoryActivity.KEY_IMPORTANT_CONTACT_RESULT_EXTRA
        )

        importantContact.categoryId = data?.id
        importantContact.categoryName = data?.name

        setupCategoryData()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupIntent()
        setupBundle(savedInstanceState)
        setupView()
        setupListener()
        setupCollector()
    }


    private fun setupListener() {
        setupBtnReload()
    }


    private fun setupCollector() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) { load() }
    }

    private fun CoroutineScope.load() = launch {
        collectorImportantContact()
        showInitialLoadingView()
        setupMode(::showCreatedView, ::getById)
    }

    private fun CoroutineScope.collectorImportantContact() = launch {
        viewmodel.importantContactState.collectLatest { uiState ->
            when (uiState) {
                is UiState.ErrorClient -> showErrorClientView(uiState.errorClient)
                is UiState.ErrorServer -> showErrorServerView(uiState.message)
                is UiState.Loading -> showLoadingView(true)
                is UiState.Success -> showSuccessView(uiState.data)
            }
        }
    }


    private fun setupMode(
        createdAction: () -> Unit, detailAction: suspend () -> Unit
    ) = lifecycleScope.launch { if (isCreatedMode) createdAction() else detailAction() }

    private fun getById() {
        viewmodel.getImportantContactById(importantContact.id)
    }

    private fun setCreate(data: ImportantContactRequest) {
        viewmodel.createImportantContact(data)
    }

    private fun setUpdate(data: ImportantContactRequest) {
        viewmodel.updateImportantContact(importantContact.id, data)
    }

    private fun setDelete() {
        viewmodel.deleteImportantContact(importantContact.id)
    }


    private suspend fun showInitialLoadingView() {
        showLoadingView(true)
        delay(DELAY_TIME)
    }

    private fun showLoadingView(isLoading: Boolean) {
        if (isLoading) {
            showDefaultView(false)
            showErrorLoadDataView(false)
        }
        binding.loadingImportantContact.showLoadingFade(isLoading)
    }

    private fun showSuccessView(data: BaseResponse<ImportantContactData>) {
        showLoadingView(false)
        if (setupSuccessManipulation(data.messageCode.orEmpty())) return
        setupMode(
            createdAction = ::showCreatedView, detailAction = { showSuccessGetByIdView(data.data) })

    }

    private fun setupSuccessManipulation(messageCode: String): Boolean {
        val message = messageCode.dialogMessageSuccess(
            activityContext, getString(R.string.text_important_contact)
        )
        message?.let {
            navigateToImportantContactActivity(it)
            return true
        }
        return false
    }

    private fun showSuccessGetByIdView(data: ImportantContactData?) {
        showDefaultViewDelayed { setupImportantContactData(data) }
    }

    private fun showErrorClientView(error: ErrorClient) {
        showLoadingView(false)
        showErrorLoadDataView(true)
        when (error.code) {
            ErrorType.CLIENT.value -> showAlertErrorClient()
            ErrorType.UNAUTHORIZED.value -> showAlertUnAuthorized()
            else -> showSnackBarErrorServer()
        }
    }

    private fun showErrorServerView(message: String) {
        showLoadingView(false)
        showErrorLoadDataView(true)
        showSnackBarErrorServer()
    }

    private fun showAlertConfirmDeletion() {
        dialogAlert.showConfirmDeleteDialog(activityContext) { setDelete() }
    }

    private fun showAlertUnAuthorized() {
        dialogAlert.showUnAuthorizedDialog(activityContext) { viewmodel.logoutLocal() }
    }

    private fun showAlertErrorClient() {
        dialogAlert.showUnknownDialog(activityContext)
    }

    private fun showSnackBarErrorServer(message: String? = null) {
        activityContext.showSnackBarErrorServer(binding, message)
    }


    private fun setupImportantContactData(data: ImportantContactData?) = binding.apply {
        vStubBinding?.run {
            setupDataSource(data)
            setupDataView()
            setupListener()
        }
    }

    private fun setupDataSource(data: ImportantContactData?) {
        val dataRemote = data ?: ImportantContactData()
        val dataBundle = importantContact
        val name = dataBundle.name ?: dataRemote.name
        val phone = dataBundle.phone ?: dataRemote.phone
        val categoryId = dataBundle.categoryId ?: dataRemote.category?.id
        val categoryName = dataBundle.categoryName ?: dataRemote.category?.name
        val information = dataBundle.information ?: dataRemote.information

        importantContact.name = name
        importantContact.phone = phone
        importantContact.categoryId = categoryId
        importantContact.categoryName = categoryName
        importantContact.information = information
    }

    private fun setupCategoryData() {
        vStubBinding?.run {
            edtCategory.setText(importantContact.categoryName)
        }
    }


    private fun <T> TextInputEditText.setFormLauncher(key: String, activityClass: Class<T>) {
        setInputLauncher(activityContext, key, activityClass, resultLauncher)
    }

    private fun TextInputEditText.setTextWatchers(onTextChanged: (String) -> Unit) {
        setupTextWatchers(::setupBtnSaveState, onTextChanged)
    }

    private fun setupBtnSaveState() = vStubBinding?.btnSave?.run {
        isEnabled = isFormValid()
    }

    private fun isFormValid(): Boolean = vStubBinding?.run {
        listOf(
            edtName,
            edtPhone,
            edtCategory,
        ).all { it.text.toString().isNotEmpty() }
    } == true


    private fun showCreatedView() {
        showLoadingView(false)
        showDefaultViewDelayed { setupImportantContactData(null) }
    }

    private fun showDefaultViewDelayed(action: () -> Unit = {}) = lifecycleScope.launch {
        delay(1)
        showDefaultView(true)
        action()
    }

    private fun showViewStub(value: Boolean) = vStubBinding?.root?.run { isVisible = value }

    private fun initStubBindingIfNull() {
        if (vStubBinding == null) {
            vStubBinding = LayoutFormImportantContactBinding.bind(vStub.inflate())
        }
    }


    private fun VStubImportantContact.setupDataView() {
        edtName.setText(importantContact.name)
        edtPhone.setText(importantContact.phone)
        setupCategoryData()
        edtInformation.setText(importantContact.information)
    }

    private fun VStubImportantContact.setupListener() {
        edtName.setTextWatchers { importantContact.name = it }
        edtPhone.setTextWatchers { importantContact.phone = it }
        edtCategory.setTextWatchers { importantContact.categoryName = it }
        edtInformation.setTextWatchers { importantContact.information = it }


        edtCategory.setFormLauncher(
            SMImportantContactCategoryActivity.KEY_IMPORTANT_CONTACT,
            SMImportantContactCategoryActivity::class.java
        )

        setupBtnSaveState()
        btnSave.setOnClickListener { save() }
        btnDelete.setOnClickListener { delete() }
    }

    private fun VStubImportantContact.save() = apply {
        hideKeyboard()
        setupMode(
            createdAction = { setCreate(importantContact) },
            detailAction = { setUpdate(importantContact) }
        )
    }

    private fun VStubImportantContact.delete() = apply {
        hideKeyboard()
        showAlertConfirmDeletion()
    }


    private fun showErrorLoadDataView(isShow: Boolean) {
        binding.viewHandle.viewErrorLoadData.root.isVisible = isShow
    }

    private fun showDefaultView(isShow: Boolean) {
        initStubBindingIfNull()
        showViewStub(isShow)
        vStubBinding?.run {
            setupMode(
                createdAction = { btnDelete.isVisible = !isShow },
                detailAction = { btnDelete.isVisible = isShow })
        }
    }


    private fun setupBtnReload() = binding.viewHandle.viewErrorLoadData.btnReload.apply {
        setOnClickListener { reload() }
    }

    private fun reload() = lifecycleScope.launch { load() }


    private fun hideKeyboard() {
        activityContext.hideKeyBoard()
    }


    private fun setupIntent() {
        importantContact.id = intent.getIntData(KEY_EXTRA_ID)
    }

    private fun setupBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        importantContact = savedInstanceState.getParcelableData<ImportantContactRequest>(
            KEY_BUNDLE_IMPORTANT_CONTACT
        ) ?: ImportantContactRequest()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_BUNDLE_IMPORTANT_CONTACT, importantContact)
        super.onSaveInstanceState(outState)
    }


    private fun setupView() {
        binding = ActivitySmimportantContactManipulationBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        vStub = binding.vsImportantContact

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setStatusBarColor(window)

        setupToolbar()
    }

    private fun setStatusBarColor(window: Window) {
        val typedValue = TypedValue()
        val theme = window.context.theme
        theme.resolveAttribute(R.color.md_theme_surface, typedValue, true)
        val color = typedValue.data
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = color
            val isLight =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO

            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = isLight
                isAppearanceLightNavigationBars = isLight
            }
        }
    }

    private fun setupToolbar() {
        binding.viewAppBar.toolbar.setManipulation(activityContext, isCreatedMode)
    }


    private fun navigateToImportantContactActivity(message: String) {
        val resultIntent = Intent()
        resultIntent.putExtra(KEY_RESULT_EXTRA_SUCCESS, message)
        setResult(KEY_RESULT_CODE_SUCCESS, resultIntent)
        finish()
    }

    companion object {
        const val KEY_EXTRA_ID = "key_extra_id"

        const val KEY_RESULT_EXTRA_SUCCESS = "key_result_extra_success"
        const val KEY_RESULT_CODE_SUCCESS = 200

        const val KEY_BUNDLE_IMPORTANT_CONTACT = "key_bundle_important_contact"

        private const val DELAY_TIME = 600L
    }
}


