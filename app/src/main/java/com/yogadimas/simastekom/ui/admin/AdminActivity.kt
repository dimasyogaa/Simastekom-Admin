package com.yogadimas.simastekom.ui.admin

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.core.R as CoreR
import com.yogadimas.simastekom.adapter.admin.AdminManipulationAdapter
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.enums.ErrorMessage
import com.yogadimas.simastekom.common.enums.SortBy
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.enums.Str
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.ToastHelper
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.common.paging.LoadingStateAdapter

import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityAdminBinding
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.viewmodel.admin.AdminAdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    private val context = this@AdminActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val viewmodel: AdminAdminViewModel by viewModel()

    private lateinit var adminManipulationAdapter: AdminManipulationAdapter
    private var loadStateListener: ((CombinedLoadStates) -> Unit)? = null

    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false

    private var isInitialLoad = true
    private var isSearching = false
    private var isEmptyKeywordSearch = true

    private var isCallback = false
    private var isAdded = false
    private var isDeleted = false

    private val emptyString = Str.EMPTY.value

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AdminManipulationActivity.KEY_RESULT_CODE && result.data != null) {
            isCallback = true
            val successText =
                result.data?.getStringExtra(AdminManipulationActivity.KEY_EXTRA_SUCCESS)
                    .orEmpty()
            if (successText.contains(getString(R.string.text_to_add).lowercase())) {
                isAdded = true
            } else if (successText.contains(getString(R.string.text_to_delete).lowercase())) {
                isDeleted = true
            }
            alertSuccess(successText)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupAuth()
        collectAdminStateForDelete()
        setupMainContent()
    }

    private fun setupView() {
        binding = ActivityAdminBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
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
            showLoadingView(true)
            authViewModel.getUser().asFlow().collect { user ->
                if (user.first == AuthPreferences.DEFAULT_VALUE) goToLogin(context)
            }
        }
    }


    private fun setupMainContent() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) { launch { setupListAdmin(getToken()) } }
    }


    private suspend fun getToken(): String = authViewModel.getUser().asFlow().first().first


    private fun setupListAdmin(token: String) {
        binding.apply {
            setupToolbar(token)
            setupSearch(token)
            setupSort(token)

            rvAdmin.layoutManager = LinearLayoutManager(context)
            fabAdd.setOnClickListener {
                resultLauncher.launch(
                    Intent(
                        context,
                        AdminManipulationActivity::class.java
                    )
                )
            }
            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { setCollectData(token) }
        }
        setupAdapter()
        onBackPressedDispatcher()
        setCollectData(token)
    }

    private fun setupAdapter() {
        adminManipulationAdapter =
            AdminManipulationAdapter(object : OnItemClickManipulationCallback<AdminData> {
                override fun onItemClicked(data: AdminData) {
                    val intent = Intent(
                        context,
                        AdminManipulationActivity::class.java
                    ).apply {
                        putExtra(AdminManipulationActivity.KEY_EXTRA_ID, data.userId)
                        putExtra(AdminManipulationActivity.KEY_EXTRA_USER_TYPE, data.userType)
                    }
                    resultLauncher.launch(intent)
                }

                override fun onDeleteClicked(data: AdminData) {
                    val label =
                        "${data.username} | ${data.name}"
                    alertDelete(label, data.userId.toString())
                }
            })
    }


    private fun ActivityAdminBinding.setupToolbar(token: String) = toolbar.apply {
        setNavigationOnClickListener { finish() }
        menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener {
            setCollectData(token)
            true
        }
    }

    private fun ActivityAdminBinding.setupSearch(token: String) = searchView.apply {
        setupWithSearchBar(searchBar)
        editText.setOnEditorActionListener { _, _, _ ->
            searchBar.setText(text)
            lifecycleScope.launch {
                hide()
                val keyword = text.toString().trim()
                isEmptyKeywordSearch = keyword.isEmpty()

                viewmodel.getAdmins(
                    token,
                    keyword,
                    null,
                    SortDir.ASC.value
                ).collectLatest { pagingData ->
                    isSearching = true
                    adminManipulationAdapter.submitData(pagingData)
                }
            }
            false
        }
    }

    private fun ActivityAdminBinding.setupSort(token: String) {
        chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        listOf(chipSortBy, chipAdminIdUsername, chipName).forEach { chipId ->
            chipId.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    when (chipId) {
                        chipSortBy -> {
                            showMenu(
                                chip,
                                R.menu.top_appbar_sort_by_menu,
                                token,
                                SortBy.CREATED_AT.value
                            )
                        }

                        chipAdminIdUsername -> {
                            showMenu(
                                chip,
                                R.menu.top_appbar_sort_smallest_largest_menu,
                                token,
                                SortBy.USER_NAME.value
                            )
                        }

                        else -> {
                            showMenu(
                                chip,
                                R.menu.top_appbar_sort_asc_desc_menu,
                                token,
                                when (chipId) {
                                    chipName -> SortBy.FULL_NAME.value
                                    else -> emptyString
                                }
                            )
                        }
                    }

                }
            }
        }
    }


    private fun setCollectData(token: String, adminData: AdminData? = null) {
        defaultStateLoadAndSearch()
        clearSearch()

        adminData?.let {
            if (it.isDeleted) {
                val message = getString(
                    R.string.text_string_pipe_string_format,
                    it.username,
                    it.name
                )
                alertSuccess(
                    getString(
                        R.string.text_alert_delete_format,
                        getString(R.string.text_success),
                        message
                    )
                )
            }
        }


        val footerAdapter = LoadingStateAdapter {
            adminManipulationAdapter.retry()
        }

        binding.rvAdmin.adapter = adminManipulationAdapter.withLoadStateFooter(
            footer = footerAdapter
        )


        collectPaging()


        lifecycleScope.launch {
            launch {
                val studentFlow = if (isAdded) {
                    viewmodel.getAdmins(token, sortDir = SortDir.DESC.value)
                } else {
                    viewmodel.getAdmins(token)
                }
                studentFlow.collectLatest { pagingData ->
                    adminManipulationAdapter.submitData(pagingData)
                }
            }

            launch {
                viewmodel.errorStateFlow.collect { errorMessage ->
                    errorMessage?.let {
                        if (errorMessage.contains(ErrorCode.UNAUTHORIZED.value)) {
                            showDefaultView(true)
                            showFailedConnectView(false)
                            alertError(getString(R.string.text_const_unauthorized))
                        }
                    }
                }
            }
        }
    }

    private fun onBackPressedDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.apply { if (searchView.isShowing) searchView.hide() else finish() }
            }
        }
        onBackPressedDispatcher.addCallback(context, callback)
    }

    private fun showMenu(v: View, menuRes: Int, token: String, sortBy: String) {
        val popup = PopupMenu(context, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        val keyword = binding.searchView.text.toString().trim()
        isEmptyKeywordSearch = keyword.isEmpty()

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.ascMenu -> {
                    lifecycleScope.launch {
                        viewmodel.getAdmins(
                            token,
                            keyword,
                            sortBy,
                            SortDir.ASC.value
                        ).collectLatest { pagingData ->
                            isSearching = true
                            adminManipulationAdapter.submitData(pagingData)
                        }
                    }

                    true
                }

                R.id.descMenu -> {
                    lifecycleScope.launch {
                        viewmodel.getAdmins(
                            token,
                            keyword,
                            sortBy,
                            SortDir.DESC.value
                        ).collectLatest { pagingData ->
                            isSearching = true
                            adminManipulationAdapter.submitData(pagingData)
                        }
                    }
                    true
                }

                else -> false
            }
        }
        popup.setOnDismissListener {
            binding.apply {
                chipSortBy.isChecked = false
                chipAdminIdUsername.isChecked = false
                chipName.isChecked = false
            }
        }
        popup.show()
    }


    private fun collectPaging() {
        loadStateListener?.let { adminManipulationAdapter.removeLoadStateListener(it) }
        loadStateListener = { loadState ->

            val refreshState = loadState.source.refresh
            val isLoading = refreshState is LoadState.Loading
            val isNotLoading = refreshState is LoadState.NotLoading
            val isError = loadState.hasError
            val hasItems = adminManipulationAdapter.itemCount > 0
            val isEmptyData = isNotLoading && loadState.append.endOfPaginationReached && !hasItems

            when {
                isLoading -> showLoadingView(true)
                isNotLoading && hasItems -> handleDataLoadedState()
                isError -> handleErrorState(loadState)
                isEmptyData -> handleEmptyDataState()
            }
        }

        loadStateListener?.let { adminManipulationAdapter.addLoadStateListener(it) }

    }

    private fun handleDataLoadedState() {
        showDefaultView(true)
        showLoadingView(false)
        isSearching = false
    }

    private fun handleErrorState(loadState: CombinedLoadStates) {
        showLoadingView(false)
        val errorMessage = loadState.extractErrorMessage()
        errorMessage?.let { message ->
            if (message.contains(ErrorCode.UNAUTHORIZED.value)) {
                showDefaultView(true)
                showFailedConnectView(false)
                alertError(getString(R.string.text_const_unauthorized))
            } else {
                val hasItems = adminManipulationAdapter.itemCount > 0
                showDefaultView(hasItems)
                showFailedConnectView(!hasItems)
                defaultStateLoadAndSearch()
                showSnackBarError(message)
            }
        }
    }

    private fun handleEmptyDataState() {
        showLoadingView(false)
        val isInitialLoadAndNotSearching = isInitialLoad && !isSearching
        val isSearchingAndEmptyKeyword = isSearching && isEmptyKeywordSearch

        val noDataText = if (isInitialLoadAndNotSearching || isSearchingAndEmptyKeyword) {
            isInitialLoad = false
            showDefaultView(false)
            showEmptyDataView(true)
            getString(R.string.text_no_data_yet)
        } else {
            isSearching = false
            showDefaultView(true)
            showEmptyDataView(false)
            getString(R.string.text_not_found)
        }

        ToastHelper.showCustomToastActivity(context, noDataText)
    }


    private fun CombinedLoadStates.extractErrorMessage(): String? {
        return (source.append as? LoadState.Error)?.error?.localizedMessage
            ?: (source.prepend as? LoadState.Error)?.error?.localizedMessage
            ?: (source.refresh as? LoadState.Error)?.error?.localizedMessage
    }


    private fun collectAdminStateForDelete() = lifecycleScope.launch {
        viewmodel.adminState.collectLatest { state ->
            if (isDeleted) {
                when (state) {
                    is State.Loading -> showLoadingView(true)
                    is State.Success -> setCollectData(getToken(), state.data)
                    is State.ErrorClient -> showErrorClient(state.error)
                    is State.ErrorServer -> showErrorServer(state.error)
                }
            }
        }
    }


    private fun showErrorClient(error: Errors) {
        showLoadingView(false)
        showFailedConnectView(false)

        var message = error.errors?.message?.first().orEmpty()

        if (message.contains(ErrorMessage.UNAUTHORIZED.value)) {
            showDefaultView(true)
            message = getString(R.string.text_const_unauthorized)
        }

        alertError(message)
    }

    private fun showErrorServer(errorMessage: String) {
        showLoadingView(false)
        showFailedConnectView(true)
        showDefaultView(false)
        showSnackBarError(errorMessage)
    }


    private fun defaultStateLoadAndSearch() {
        isInitialLoad = true
        isSearching = false
        isEmptyKeywordSearch = true
    }

    private fun clearSearch() {
        binding.searchBar.setText(null)
        binding.searchView.setText(null)
    }

    private fun alertSuccess(message: String) {
        showAlertDialog(msg = message, status = STATUS_SUCCESS)
    }

    private fun alertError(message: String) {
        showAlertDialog(msg = message, status = STATUS_ERROR)
    }

    private fun alertDelete(message: String, userId: String) {
        showAlertDialog(msg = message, status = STATUS_CONFIRM_DELETE, userId = userId)
    }

    private fun showAlertDialog(
        msg: String = emptyString,
        status: String,
        userId: String = emptyString,
    ) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = emptyString
        var message = emptyString
        var icon: Drawable? = null
        when (status) {
            STATUS_CONFIRM_DELETE -> {
                icon = ContextCompat.getDrawable(context, R.drawable.z_ic_delete)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(context, R.color.md_theme_error)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_delete)
                message = getString(R.string.text_question_do_you_want_to_delete_format, msg)
            }

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
                    title = getString(R.string.text_error_format, "")
                    message = msg
                }

            }
        }

        if (dialog == null) {
            if (status == STATUS_SUCCESS) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2100)
                    isAdded = false
                    isDeleted = false
                    isAlertDialogShow = false
                    dialog?.dismiss()
                    dialog = null
                }
            }

            dialog = MaterialAlertDialogBuilder(context).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (status == STATUS_CONFIRM_DELETE || status == STATUS_ERROR) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        isAlertDialogShow = false
                        dialog = null
                        when (status) {
                            STATUS_CONFIRM_DELETE -> lifecycleScope.launch {
                                isDeleted = true
                                viewmodel.deleteAdmin(getToken(), userId)
                            }

                            STATUS_ERROR -> {
                                if (unauthorized) {
                                    authViewModel.saveUser(
                                        null,
                                        null,
                                        null
                                    )
                                } else {
                                    lifecycleScope.launch {
                                        setCollectData(getToken())
                                    }
                                    return@setPositiveButton
                                }
                            }
                        }
                    }

                    if (status == STATUS_CONFIRM_DELETE) {
                        setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                            isAlertDialogShow = false
                            dialog = null
                            return@setNegativeButton
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

    private fun showLoadingView(isVisible: Boolean) {
        val shouldShowLoading = isVisible && !isAlertDialogShow
        showLoading(binding.mainProgressBar, shouldShowLoading)

        if (isVisible) {
            showDefaultView(false)
            showFailedConnectView(false)
            showEmptyDataView(false)
        }
    }

    private fun showDefaultView(isVisible: Boolean) {
        binding.apply {
            if (isVisible) {
                toolbar.visibility = View.VISIBLE
                toolbarSearchFilter.visibility = View.VISIBLE
                fabAdd.visibility = View.VISIBLE
                rvAdmin.animate().alpha(1.0f).setDuration(600)
            } else {
                toolbar.visibility = View.INVISIBLE
                toolbarSearchFilter.visibility = View.GONE
                fabAdd.visibility = View.GONE
                rvAdmin.alpha = 0f
            }
        }

    }

    private fun showFailedConnectView(isVisible: Boolean) {
        binding.viewHandle.viewFailedConnect.root.isVisible = isVisible
        setToolbarVisible()
    }

    private fun showEmptyDataView(isVisible: Boolean) {
        binding.viewHandle.viewEmptyData.root.isVisible = isVisible
        binding.fabAdd.isVisible = isVisible
        setToolbarVisible()
    }

    private fun setToolbarVisible() = binding.toolbar.apply { isVisible = true }


    private fun showSnackBarError(message: String) {
        SnackBarHelper.display(
            viewGroup = binding.root as ViewGroup,
            message = message,
            lifecycleOwner = context,
        )
    }


    override fun onPause() {
        super.onPause()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
        ToastHelper.dismissToast()
    }

    companion object {
        private const val STATUS_CONFIRM_DELETE = "status_confirm_delete"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
    }


}