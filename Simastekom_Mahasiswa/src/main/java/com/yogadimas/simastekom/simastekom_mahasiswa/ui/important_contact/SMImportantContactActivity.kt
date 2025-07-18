package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_contact

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Window
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.search.SearchView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.core.common.customs.DialogAlert
import com.yogadimas.simastekom.core.common.customs.HeaderAdapter
import com.yogadimas.simastekom.core.common.enums.ErrorType
import com.yogadimas.simastekom.core.common.extensions.getStringData
import com.yogadimas.simastekom.core.common.extensions.showSuccessDialog
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.core.ui.adapter.paging.LoadingStateAdapter
import com.yogadimas.simastekom.core.utils.CustomPagingSourceException
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showSnackBarErrorServer
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showToast
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showToastErrorServer
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnAuthorizedDialog
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnknownDialog
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ActivitySmimportantContactBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.yogadimas.simastekom.core.R as CoreR

class SMImportantContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmimportantContactBinding
    private val activityContext = this@SMImportantContactActivity

    private val viewmodel: SMImportantContactViewModel by viewModel()

    private lateinit var importantContactAdapter: SMImportantContactAdapter
    private var loadStateListener: ((CombinedLoadStates) -> Unit)? = null
    private lateinit var loadingStateAdapter: LoadingStateAdapter

    private val dialogAlert: DialogAlert by inject {
        parametersOf(
            activityContext,
            activityContext
        )
    }

    private var isInitialLoad = true
    private var isSearching = false
    private var isSearchKeywordEmpty = true

    private fun resetStateLoadAndSearch() {
        isInitialLoad = true
        isSearching = false
        isSearchKeywordEmpty = true
    }

    private fun clearSearch() {
        binding.searchBar.setText(null)
        binding.searchView.setText(null)
    }


    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleIntentResult(result.data, result.resultCode)
    }

    private fun handleIntentResult(intent: Intent?, code: Int) {
        intent?.let {
            showDefaultView(false)
            setupIntentResult(code, it)
        }
    }

    private fun setupIntentResult(code: Int, intent: Intent) {
        when (code) {
            SMImportantContactManipulationActivity.KEY_RESULT_CODE_SUCCESS -> {
                setupDialogSuccessResult(intent)
                reload()
            }
        }
    }

    private fun setupDialogSuccessResult(intent: Intent) {
        val key = SMImportantContactManipulationActivity.KEY_RESULT_EXTRA_SUCCESS
        val data = intent.getStringData(key)
        dialogAlert.showSuccessDialog(activityContext, lifecycleScope, data)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupListener()
        setupAdapter()
        setupCollector()
        setupOnBackPressedDispatcher()
    }


    private fun setupListener() = binding.apply {
        setupBtnReload()
        fabAdd.setOnClickListener {
            navigateToManipulationActivity()
        }
        searchView.apply {
            addTransitionListener { _, _, newState ->
                handleSearchTransition(newState)
            }
            editText.setOnEditorActionListener { _, actionId, event ->
                handleSearchEditorAction(actionId, event)
            }
        }
    }

    private fun setupAdapter() {
        importantContactAdapter = SMImportantContactAdapter { data ->
            navigateToManipulationActivity(data.id)
        }
    }

    private fun setupCollector() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            showDefaultView(false)
            collectorImportantContacts()
            collectorPaging()
        }
    }


    private fun CoroutineScope.collectorImportantContacts() = launch {
        getImportantContacts().collectLatest { pagingData ->
            submitListOfData(pagingData)
        }
    }

    private fun CoroutineScope.collectorImportantContacts(searchKeyword: String?) = launch {
        getImportantContacts(searchKeyword).collectLatest { pagingData ->
            isSearching = true
            submitListOfData(pagingData)
        }
    }

    private fun CoroutineScope.collectorPaging() = launch {
        loadStateListener?.let { importantContactAdapter.removeLoadStateListener(it) }
        loadStateListener = { loadState ->

            val isLoading = isLoading(loadState)
            val isInitialError = isInitialError(loadState)
            val isAppendError = isAppendError(loadState)
            val isDataLoaded = isDataLoaded(loadState)
            val isEmptyData = isDataEmpty(loadState)
            val errors = getLoadStateError(loadState)

            Log.e(
                "LoadStateCheck", """
                1. isLoading: $isLoading
                2. isInitialError: $isInitialError
                2. isAppendError: $isAppendError
                3. isDataLoaded: $isDataLoaded
                4. isEmptyData: $isEmptyData
                5. errors: $errors
            """.trimIndent()
            )

            when {
                isLoading(loadState) -> showLoadingView(true)

                isInitialError(loadState) -> {
                    val error = getLoadStateError(loadState)?.error as CustomPagingSourceException
                    showInitialErrorView(error)
                }

                isAppendError(loadState) -> {
                    val error = getLoadStateError(loadState)?.error as CustomPagingSourceException
                    showAppendErrorView(error)
                }


                isDataEmpty(loadState) -> handleDataEmptyOrNotFoundView()

                isDataLoaded(loadState) -> showDataLoadedView()

            }


        }

        loadStateListener?.let { importantContactAdapter.addLoadStateListener(it) }
    }


    private fun submitListOfData(listData: PagingData<ImportantContactData>) {
        importantContactAdapter.submitData(lifecycle, listData)
    }

    private fun getImportantContacts(searchKeyword: String? = null): SharedFlow<PagingData<ImportantContactData>> {
        setupAdapterPaging()
        return viewmodel.getImportantContacts(searchKeyword)
    }

    private fun setupAdapterPaging() {
        loadingStateAdapter = LoadingStateAdapter { importantContactAdapter.retry() }
        val withLoadStateFooter = importantContactAdapter.withLoadStateFooter(
            footer = loadingStateAdapter
        )
        val concatAdapter = ConcatAdapter(
            HeaderAdapter(),
            withLoadStateFooter
        )
        binding.rvImportantContact.apply {
            layoutManager = LinearLayoutManager(activityContext)
            adapter = concatAdapter
        }
    }


    private fun isLoading(loadState: CombinedLoadStates): Boolean =
        (loadState.source.refresh is LoadState.Loading)

    private fun isInitialError(loadState: CombinedLoadStates): Boolean =
        loadState.source.refresh is LoadState.Error

    private fun isAppendError(loadState: CombinedLoadStates): Boolean =
        loadState.source.append is LoadState.Error

    private fun isDataLoaded(loadState: CombinedLoadStates): Boolean =
        loadState.source.refresh is LoadState.NotLoading
                && importantContactAdapter.itemCount > 0

    private fun isDataEmpty(loadState: CombinedLoadStates) =
        loadState.source.refresh is LoadState.NotLoading
                && loadState.append.endOfPaginationReached && importantContactAdapter.itemCount == 0

    private fun getLoadStateError(loadState: CombinedLoadStates): LoadState.Error? = listOf(
        loadState.source.refresh,
        loadState.source.append,
        loadState.source.prepend
    ).firstOrNull { it is LoadState.Error } as? LoadState.Error


    private fun showLoadingView(isLoading: Boolean) {
        binding.loadingImportantContact.isVisible = isLoading
        if (isLoading) {
            showDefaultView(false)
            showErrorLoadDataView(false)
            showEmptyDataView(false)
            showNotFoundDataView(false)
        }

    }

    private fun showDataLoadedView() {
        showLoadingView(false)
        showDefaultView(true)
        isSearching = false
    }


    private fun showDataEmptyView(): String {
        isInitialLoad = false
        showReloadMenu(true)
        showEmptyDataView(true)
        return getString(R.string.text_no_data_yet)
    }

    private fun showDataNotFoundView(): String {
        isSearching = false
        showSearchBarView(true)
        showNotFoundDataView(true)
        return getString(R.string.text_not_found)
    }

    private fun handleDataEmptyOrNotFoundView() {
        showLoadingView(false)
        showAddButton(true)

        val isInitialLoadAndNotSearching = isInitialLoad && !isSearching
        val isSearchingAndEmptyKeyword = isSearching && isSearchKeywordEmpty

        val shouldShowEmptyView = isInitialLoadAndNotSearching || isSearchingAndEmptyKeyword

        val noDataText = if (shouldShowEmptyView) showDataEmptyView() else showDataNotFoundView()

        showToast(noDataText)
    }


    private fun showInitialErrorView(error: CustomPagingSourceException) {
        showLoadingView(false)
        submitListOfData(PagingData.empty())
        showErrorLoadDataView(true)
        when (error.code) {
            ErrorType.CLIENT.value -> showAlertErrorClient()
            ErrorType.UNAUTHORIZED.value -> showAlertUnAuthorized()
            else -> showSnackBarErrorServer()
        }
    }

    private fun showAppendErrorView(error: CustomPagingSourceException) {
        showLoadingView(false)
        when (error.code) {
            ErrorType.UNAUTHORIZED.value -> showAlertUnAuthorized()
            ErrorType.SERVER.value -> showToastErrorServer()
        }
    }


    private fun showEmptyDataView(isShow: Boolean) {
        binding.viewHandle.viewEmptyData.root.isVisible = isShow
    }

    private fun showNotFoundDataView(isShow: Boolean) {
        binding.viewHandle.viewNotFoundData.root.isVisible = isShow
    }


    private fun showErrorLoadDataView(isShow: Boolean) {
        binding.viewHandle.viewErrorLoadData.root.isVisible = isShow
    }

    private fun showDefaultView(isShow: Boolean) = binding.apply {
        showReloadMenu(isShow)
        showSearchBarView(isShow)
        showAddButton(isShow)
        setupAnimationRv(isShow)
    }


    private fun showReloadMenu(isShow: Boolean) {
        binding.toolbar.menu.findItem(CoreR.id.menu_reload).isVisible = isShow
    }


    private fun showAlertUnAuthorized() {
        dialogAlert.showUnAuthorizedDialog(activityContext) { viewmodel.logoutLocal() }
    }

    private fun showAlertErrorClient() {
        dialogAlert.showUnknownDialog(activityContext)
    }

    private fun showSnackBarErrorServer(message: String? = null) {
        activityContext.showSnackBarErrorServer(binding, message, binding.fabAdd)
    }


    private fun setupBtnReload() = binding.apply {
        toolbar.menu.findItem(CoreR.id.menu_reload).setOnMenuItemClickListener {
            reload()
            true
        }
        viewHandle.apply {
            viewErrorLoadData.btnReload.setOnClickListener { reload() }
            viewHandle.viewNotFoundData.btnReload.setOnClickListener { reload() }
        }
    }

    private fun showAddButton(isShow: Boolean) = binding.apply {
        fabAdd.isInvisible = !isShow
    }

    private fun showSearchBarView(isShow: Boolean) = binding.apply {
        toolbarSearchFilter.isVisible = isShow
    }

    private fun setupAnimationRv(isShow: Boolean) = binding.apply {
        if (isShow) {
            rvImportantContact.animate().alpha(1.0f).setDuration(600)
        } else {
            rvImportantContact.alpha = 0f
        }
    }


    private fun reload() = lifecycleScope.launch {
        resetStateLoadAndSearch()
        clearSearch()
        collectorImportantContacts()
        binding.rvImportantContact.smoothScrollToPosition(0)
    }


    private fun setupView() {
        binding = ActivitySmimportantContactBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        binding.apply {
            setContentView(root)
            ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }


            setStatusBarColor(window)
            setupToolbar()
            setupSearch()

        }
    }

    private fun setStatusBarColor(window: Window) {
        val typedValue = TypedValue()
        val theme = window.context.theme
        theme.resolveAttribute(CoreR.color.md_theme_surface, typedValue, true)
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

    private fun setupToolbar() = binding.toolbar.apply {
        setNavigationOnClickListener { finish() }
    }


    private fun setupSearch() = binding.searchView.apply {
        val searchBar = binding.searchBar
        setupWithSearchBar(searchBar)
    }

    private fun handleSearchEditorAction(actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
            (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
        ) {
            performSearch()
            return true
        }
        return false
    }

    private fun handleSearchTransition(newState: SearchView.TransitionState) = binding.apply {
        if (newState == SearchView.TransitionState.SHOWN) {
            fabAdd.hide()
        } else if (newState == SearchView.TransitionState.HIDDEN) {
            fabAdd.show()
        }
    }

    private fun performSearch() {
        val searchBar = binding.searchBar
        val searchView = binding.searchView
        val text = searchView.text

        searchBar.setText(text)
        lifecycleScope.launch {
            searchView.hide()
            val keyword = text.toString().trim()
            isSearchKeywordEmpty = keyword.isEmpty()
            collectorImportantContacts(keyword)
        }
    }


    private fun navigateToManipulationActivity(id: Int? = null) {
        val intent = Intent(activityContext, SMImportantContactManipulationActivity::class.java)
        intent.putExtra(SMImportantContactManipulationActivity.KEY_EXTRA_ID, id)
        resultLauncher.launch(intent)
    }


    private fun setupOnBackPressedDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.apply { if (searchView.isShowing) searchView.hide() else finish() }
            }
        }
        onBackPressedDispatcher.addCallback(activityContext, callback)
    }


}