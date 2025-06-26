package com.yogadimas.simastekom.simastekom_mahasiswa.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.yogadimas.simastekom.R
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
import com.yogadimas.simastekom.core.data.source.remote.request.base.BaseRequest
import com.yogadimas.simastekom.core.data.source.remote.response.base.BaseResponse
import com.yogadimas.simastekom.core.ui.UiState
import com.yogadimas.simastekom.core.utils.ErrorClient
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showSnackBarErrorServer
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnAuthorizedDialog
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnknownDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.core.parameter.parametersOf

abstract class BaseManipulationActivity<
        VB : ViewBinding,
        DataResponse : Any,
        DataRequest : BaseRequest,
        > :
    AppCompatActivity() {

    protected lateinit var binding: VB
    protected abstract fun inflateBinding(layoutInflater: LayoutInflater): VB
    protected abstract val activityContext: AppCompatActivity

    protected abstract val viewmodel: BaseViewModel

    protected val dialogAlert: DialogAlert by lazy {
        getKoin().get<DialogAlert> {
            parametersOf(activityContext, activityContext)
        }
    }

    protected abstract val main: ScrollView
    protected abstract val toolbar: MaterialToolbar
    protected abstract val loading: CircularProgressIndicator
    protected abstract val emptyDataView: MaterialCardView
    protected abstract val errorLoadDataView: ConstraintLayout
    protected abstract val errorBtnReload: Button

    protected abstract val labelDialogSuccess: String

    private val isCreatedMode: Boolean get() = dataBundle.id == 0

    protected abstract var dataBundle: DataRequest

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleIntentResult(result.data, result.resultCode)
    }

    private fun handleIntentResult(intent: Intent?, code: Int) {
        intent?.let { setupIntentResult(code, it) }
    }

    protected open fun setupIntentResult(code: Int, intent: Intent) {}


    protected fun setupListener() {
        setupBtnReload()
    }


    protected fun setupCollector() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) { load() }
    }

    private fun CoroutineScope.load() = launch {
        collectorMain()
        showInitialLoadingView()
        setupMode(::showCreatedView, ::getById)
    }

    private fun CoroutineScope.collectorMain() = launch {
        state().collectLatest { uiState ->
            when (uiState) {
                is UiState.ErrorClient -> showErrorClientView(uiState.errorClient)
                is UiState.ErrorServer -> showErrorServerView(uiState.message)
                is UiState.Loading -> showLoadingView(true)
                is UiState.Success -> showSuccessView(uiState.data)
            }
        }
    }

    protected abstract fun state(): SharedFlow<UiState<BaseResponse<DataResponse>>>


    protected fun setupMode(
        createdAction: () -> Unit, detailAction: suspend () -> Unit
    ) = lifecycleScope.launch { if (isCreatedMode) createdAction() else detailAction() }

    protected abstract fun getById()

    protected abstract fun setCreate(data: DataRequest)

    protected abstract fun setUpdate(data: DataRequest)

    protected abstract fun setDelete()


    private suspend fun showInitialLoadingView() {
        showLoadingView(true)
        delay(600L)
    }

    private fun showLoadingView(isLoading: Boolean) {
        if (isLoading) {
            showDefaultView(false)
            showErrorLoadDataView(false)
        }
        loading.showLoadingFade(isLoading)
    }

    private fun showSuccessView(data: BaseResponse<DataResponse>) {
        showLoadingView(false)
        if (setupSuccessManipulation(data.messageCode.orEmpty())) return
        setupMode(
            createdAction = ::showCreatedView, detailAction = { showSuccessGetByIdView(data.data) })

    }

    private fun setupSuccessManipulation(messageCode: String): Boolean {
        val message = messageCode.dialogMessageSuccess(
            activityContext, labelDialogSuccess
        )
        message?.let {
            navigateToListActivity(it)
            return true
        }
        return false
    }

    private fun showSuccessGetByIdView(data: DataResponse?) {
        showDefaultViewDelayed { setupMainData(data) }
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


    protected abstract fun setupMainData(data: DataResponse?)

    protected abstract fun setupDataSource(data: DataResponse?)


    protected fun <T> TextInputEditText.setFormLauncher(key: String, activityClass: Class<T>) {
        setInputLauncher(activityContext, key, activityClass, resultLauncher)
    }

    protected fun TextInputEditText.setTextWatchers(onTextChanged: (String) -> Unit) {
        setupTextWatchers(::setupBtnSaveState, onTextChanged)
    }


    protected abstract fun setupBtnSaveState()

    protected abstract fun isFormValid(): Boolean

    private fun showCreatedView() {
        showLoadingView(false)
        showDefaultViewDelayed { setupMainData(null) }
    }

    private fun showDefaultViewDelayed(action: () -> Unit = {}) = lifecycleScope.launch {
        delay(1L)
        showDefaultView(true)
        action()
    }


    protected abstract fun initStubBindingIfNull()
    protected abstract fun setupDataViewOnStub()
    protected abstract fun setupListenerOnStub()


    protected fun saveOnStub() = apply {
        hideKeyboard()
        setupMode(
            createdAction = { setCreate(dataBundle) },
            detailAction = { setUpdate(dataBundle) }
        )
    }

    protected fun deleteOnStub() = apply {
        hideKeyboard()
        showAlertConfirmDeletion()
    }


    private fun showErrorLoadDataView(isShow: Boolean) {
        errorLoadDataView.isVisible = isShow
    }

    protected open fun showDefaultView(isShow: Boolean) {
        initStubBindingIfNull()

    }


    private fun setupBtnReload() = errorBtnReload.apply {
        setOnClickListener { reload() }
    }

    private fun reload() = lifecycleScope.launch { load() }


    private fun hideKeyboard() {
        activityContext.hideKeyBoard()
    }


    protected fun setupIntent() {
        dataBundle.id = intent.getIntData(KEY_EXTRA_ID)
    }

    protected fun setupBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        dataBundle =
            savedInstanceState.getParcelableData(KEY_BUNDLE, dataBundle::class.java) ?: dataBundle

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_BUNDLE, dataBundle)
        super.onSaveInstanceState(outState)
    }


    protected open fun setupView(onView: () -> Unit) {
        binding = inflateBinding(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        onView()

        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
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
        toolbar.setManipulation(activityContext, isCreatedMode)
    }


    private fun navigateToListActivity(message: String) {
        val resultIntent = Intent()
        resultIntent.putExtra(KEY_RESULT_EXTRA, message)
        setResult(KEY_RESULT_CODE, resultIntent)
        finish()
    }

    companion object {
        const val KEY_EXTRA_ID = "key_extra_id"
        const val KEY_RESULT_EXTRA = "key_result_extra"
        const val KEY_RESULT_CODE = 200
        const val KEY_BUNDLE = "key_bundle"
    }
}
