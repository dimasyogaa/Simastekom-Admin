package com.yogadimas.simastekom.simastekom_mahasiswa.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.core.common.customs.DialogAlert
import com.yogadimas.simastekom.core.common.customs.HeaderAdapter
import com.yogadimas.simastekom.core.common.enums.ErrorType
import com.yogadimas.simastekom.core.common.extensions.getStringData
import com.yogadimas.simastekom.core.common.extensions.showSuccessDialog
import com.yogadimas.simastekom.core.ui.adapter.paging.LoadingStateAdapter
import com.yogadimas.simastekom.core.utils.CustomPagingSourceException
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showSnackBarErrorServer
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showToast
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showToastErrorServer
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnAuthorizedDialog
import com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions.showUnknownDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.core.parameter.parametersOf
import com.yogadimas.simastekom.core.R as CoreR

abstract class BaseListActivity<
        VB : ViewBinding,
        Data : Any,
        VH : RecyclerView.ViewHolder,
        A : PagingDataAdapter<Data, VH>
        > :
    AppCompatActivity() {

    protected lateinit var binding: VB
    protected abstract fun inflateBinding(layoutInflater: LayoutInflater): VB
    protected abstract val activityContext: AppCompatActivity
    private val lifecycleOwner: LifecycleOwner by lazy { activityContext }


    protected abstract val viewmodel: BaseViewModel

    protected abstract var adapter: A
    protected var loadStateListener: ((CombinedLoadStates) -> Unit)? = null
    protected abstract var loadingStateAdapter: LoadingStateAdapter

    protected val dialogAlert: DialogAlert by lazy {
        getKoin().get<DialogAlert> {
            parametersOf(activityContext, activityContext)
        }
    }

    protected var isInitialLoad = true
    protected var isSearching = false
    protected var isSearchKeywordEmpty = true

    protected abstract val main: CoordinatorLayout
    protected abstract val toolbar: MaterialToolbar
    protected abstract val toolbarSearchFilter: MaterialToolbar
    protected abstract val searchBar: SearchBar
    protected abstract val searchView: SearchView
    protected abstract val recyclerView: RecyclerView
    protected abstract val fabAdd: FloatingActionButton
    protected abstract val loading: LinearProgressIndicator
    protected abstract val emptyDataView: LinearLayout
    protected abstract val notFoundDataView: LinearLayout
    protected abstract val errorLoadDataView: LinearLayout
    protected abstract val errorBtnReload: Button
    protected abstract val notFoundBtnReload: Button
    protected abstract val toManipulation: Class<*>


    private val keyResultCode: Int = BaseManipulationActivity.KEY_RESULT_CODE
    private val keyResultExtra: String = BaseManipulationActivity.KEY_RESULT_EXTRA
    private val keyManipulationId: String = BaseManipulationActivity.KEY_EXTRA_ID


    private fun resetStateLoadAndSearch() {
        isInitialLoad = true
        isSearching = false
        isSearchKeywordEmpty = true
    }

    private fun clearSearch() {
        searchBar.setText(null)
        searchView.setText(null)
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
            keyResultCode -> {
                setupDialogSuccessResult(intent)
                reload()
            }
        }
    }

    private fun setupDialogSuccessResult(intent: Intent) {
        val key = keyResultExtra
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


    protected fun setupListener() = binding.apply {
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

    protected abstract fun setupAdapter()

    protected fun setupCollector() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            showDefaultView(false)
            collectorMain()
            collectorPaging()
        }
    }


    private fun CoroutineScope.collectorMain() = launch {
        get().collectLatest { pagingData ->
            submitListOfData(pagingData)
        }
    }

    private fun CoroutineScope.collectorMain(searchKeyword: String?) = launch {
        get(searchKeyword).collectLatest { pagingData ->
            isSearching = true
            submitListOfData(pagingData)
        }
    }

    private fun CoroutineScope.collectorPaging() = launch {
        loadStateListener?.let { adapter.removeLoadStateListener(it) }
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

        loadStateListener?.let { adapter.addLoadStateListener(it) }
    }


    private fun submitListOfData(listData: PagingData<Data>) {
        adapter.submitData(lifecycle, listData)
    }

    private fun get(searchKeyword: String? = null): SharedFlow<PagingData<Data>> {
        setupAdapterPaging()
        return trigger(searchKeyword)
    }

    protected abstract fun trigger(searchKeyword: String? = null): SharedFlow<PagingData<Data>>

    private fun setupAdapterPaging() {
        loadingStateAdapter = LoadingStateAdapter { adapter.retry() }
        val withLoadStateFooter = adapter.withLoadStateFooter(
            footer = loadingStateAdapter
        )
        val concatAdapter = ConcatAdapter(
            HeaderAdapter(),
            withLoadStateFooter
        )
        recyclerView.apply {
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
                && adapter.itemCount > 0

    private fun isDataEmpty(loadState: CombinedLoadStates) =
        loadState.source.refresh is LoadState.NotLoading
                && loadState.append.endOfPaginationReached && adapter.itemCount == 0

    private fun getLoadStateError(loadState: CombinedLoadStates): LoadState.Error? = listOf(
        loadState.source.refresh,
        loadState.source.append,
        loadState.source.prepend
    ).firstOrNull { it is LoadState.Error } as? LoadState.Error


    private fun showLoadingView(isLoading: Boolean) {
        loading.isVisible = isLoading
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
        emptyDataView.isVisible = isShow
    }

    private fun showNotFoundDataView(isShow: Boolean) {
        notFoundDataView.isVisible = isShow
    }


    private fun showErrorLoadDataView(isShow: Boolean) {
        errorLoadDataView.isVisible = isShow
    }

    private fun showDefaultView(isShow: Boolean) = binding.apply {
        showReloadMenu(isShow)
        showSearchBarView(isShow)
        showAddButton(isShow)
        setupAnimationRv(isShow)
    }


    private fun showReloadMenu(isShow: Boolean) {
        toolbar.menu.findItem(CoreR.id.menu_reload).isVisible = isShow
    }


    private fun showAlertUnAuthorized() {
        dialogAlert.showUnAuthorizedDialog(activityContext) { viewmodel.logoutLocal() }
    }

    private fun showAlertErrorClient() {
        dialogAlert.showUnknownDialog(activityContext)
    }

    private fun showSnackBarErrorServer(message: String? = null) {
        activityContext.showSnackBarErrorServer(binding, message, fabAdd)
    }


    private fun setupBtnReload() = binding.apply {
        toolbar.menu.findItem(CoreR.id.menu_reload).setOnMenuItemClickListener {
            reload()
            true
        }
        errorBtnReload.setOnClickListener { reload() }
        notFoundBtnReload.setOnClickListener { reload() }
    }

    private fun showAddButton(isShow: Boolean) = binding.apply {
        fabAdd.isInvisible = !isShow
    }

    private fun showSearchBarView(isShow: Boolean) = binding.apply {
        toolbarSearchFilter.isVisible = isShow
    }

    private fun setupAnimationRv(isShow: Boolean) = binding.apply {
        if (isShow) {
            recyclerView.animate().alpha(1.0f).setDuration(600)
        } else {
            recyclerView.alpha = 0f
        }
    }


    private fun reload() = lifecycleScope.launch {
        resetStateLoadAndSearch()
        clearSearch()
        collectorMain()
        recyclerView.smoothScrollToPosition(0)
    }

    protected open fun setupView() {
        binding = inflateBinding(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setStatusBarColor(window)
        setupToolbar()
        setupSearch()
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

    private fun setupToolbar() = toolbar.apply {
        setNavigationOnClickListener { finish() }
    }


    private fun setupSearch() = searchView.apply {
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
        val text = searchView.text

        searchBar.setText(text)
        lifecycleScope.launch {
            searchView.hide()
            val keyword = text.toString().trim()
            isSearchKeywordEmpty = keyword.isEmpty()
            collectorMain(keyword)
        }
    }


    protected fun navigateToManipulationActivity(id: Int? = null) {
        val intent = Intent(activityContext, toManipulation)
        intent.putExtra(keyManipulationId, id)
        resultLauncher.launch(intent)
    }


    protected fun setupOnBackPressedDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.apply { if (searchView.isShowing) searchView.hide() else finish() }
            }
        }
        onBackPressedDispatcher.addCallback(lifecycleOwner, callback)
    }


}