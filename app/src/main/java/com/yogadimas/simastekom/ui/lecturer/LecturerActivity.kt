package com.yogadimas.simastekom.ui.lecturer

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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
import com.yogadimas.simastekom.adapter.lecturer.LecturerManipulationAdapter
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
import com.yogadimas.simastekom.databinding.ActivityLecturerBinding
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.LecturerData
import com.yogadimas.simastekom.viewmodel.admin.AdminLecturerViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LecturerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerBinding

    private val context = this@LecturerActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val viewmodel: AdminLecturerViewModel by viewModel()

    private lateinit var lecturerManipulationAdapter: LecturerManipulationAdapter
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
        if (result.resultCode == LecturerManipulationActivity.KEY_RESULT_CODE && result.data != null) {
            isCallback = true
            val successText =
                result.data?.getStringExtra(LecturerManipulationActivity.KEY_EXTRA_SUCCESS)
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
        binding = ActivityLecturerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth()

        collectLecturerStateForDelete()
        mainContent()
    }

    private fun collectLecturerStateForDelete() = lifecycleScope.launch {
        viewmodel.lecturerState.collectLatest { state ->
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

    private fun auth() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            showLoadingView(true)
            authViewModel.getUser().asFlow().collect { user ->
                if (user.first == AuthPreferences.DEFAULT_VALUE)  goToLogin(context)
            }
        }
    }

    private fun mainContent() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            launch {
                setMainContent(getToken())
            }
        }
    }

    private suspend fun getToken(): String = authViewModel.getUser().asFlow().first().first

    private fun setMainContent(token: String) {
        binding.apply {
            setToolbar(token)
            setSearch(token)
            setSort(token)

            rvLecturer.layoutManager = LinearLayoutManager(context)
            fabAdd.setOnClickListener {
                resultLauncher.launch(
                    Intent(
                        context,
                        LecturerManipulationActivity::class.java
                    )
                )
            }
            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { setCollectData(
                token,
            ) }
        }

        lecturerManipulationAdapter =
            LecturerManipulationAdapter(object : OnItemClickManipulationCallback<LecturerData> {
                override fun onItemClicked(data: LecturerData) {
                    val intent = Intent(
                        context,
                        LecturerManipulationActivity::class.java
                    ).apply {
                        putExtra(LecturerManipulationActivity.KEY_EXTRA_ID, data.userId)
                        putExtra(LecturerManipulationActivity.KEY_EXTRA_USER_TYPE, data.userType)
                    }
                    resultLauncher.launch(intent)
                }

                override fun onDeleteClicked(data: LecturerData) {
                    val label =
                        "${data.lecturerIdNumber} | ${data.fullName}"
                    alertDelete(label, data.userId ?: "0")
                }
            })

        onBackPressedDispatcher()
        setCollectData(token)

    }


    private fun ActivityLecturerBinding.setToolbar(token: String) = toolbar.apply {
        setNavigationOnClickListener { finish() }
        menu.findItem(R.id.refreshMenu).setOnMenuItemClickListener {
            setCollectData(token)
            true
        }
    }

    private fun ActivityLecturerBinding.setSearch(token: String) = searchView.apply {
        setupWithSearchBar(searchBar)
        editText.setOnEditorActionListener { _, _, _ ->
            searchBar.setText(text)
            lifecycleScope.launch {
                hide()
                val keyword = text.toString().trim()
                isEmptyKeywordSearch = keyword.isEmpty()

                viewmodel.getLecturers(
                    token,
                    keyword,
                    null,
                    SortDir.ASC.value
                ).collectLatest { pagingData ->
                    isSearching = true
                    lecturerManipulationAdapter.submitData(pagingData)
                }

            }
            false
        }
    }

    private fun ActivityLecturerBinding.setSort(token: String) {
        chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        listOf(chipSortBy, chipLecturerIdNumber, chipName).forEach { chipId ->
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

                        chipLecturerIdNumber -> {
                            showMenu(
                                chip,
                                R.menu.top_appbar_sort_smallest_largest_menu,
                                token,
                                SortBy.LECTURE_ID_NUMBER.value
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


    private fun setCollectData(token: String, lecturerData: LecturerData? = null) {
        defaultStateLoadAndSearch()
        clearSearch()


        lecturerData?.let {
            if (it.isDeleted) {
                val message = getString(R.string.text_string_pipe_string_format, it.lecturerIdNumber, it.fullName)
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
            lecturerManipulationAdapter.retry()
        }

        binding.rvLecturer.adapter = lecturerManipulationAdapter.withLoadStateFooter(
            footer = footerAdapter
        )


        collectPaging()


        lifecycleScope.launch {
            launch {

                val studentFlow = if (isAdded) {
                    viewmodel.getLecturers(token, sortDir = "desc")
                } else {
                    viewmodel.getLecturers(token)
                }

                studentFlow.collectLatest { pagingData ->
                    lecturerManipulationAdapter.submitData(pagingData)
                }

//                viewmodel.getLecturers(token).collectLatest { pagingData ->
//                    lecturerManipulationAdapter.submitData(pagingData)
//                }
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
                binding.apply {
                    if (searchView.isShowing) {
                        searchView.hide()
                    } else {
                        finish()
                    }
                }

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
                        viewmodel.getLecturers(
                            token,
                            keyword,
                            sortBy,
                            SortDir.ASC.value
                        ).collectLatest { pagingData ->
                            isSearching = true
                            lecturerManipulationAdapter.submitData(pagingData)
                        }
                    }

                    true
                }

                R.id.descMenu -> {
                    lifecycleScope.launch {
                        viewmodel.getLecturers(
                            token,
                            keyword,
                            sortBy,
                            SortDir.DESC.value
                        ).collectLatest { pagingData ->
                            isSearching = true
                            lecturerManipulationAdapter.submitData(pagingData)
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
                chipLecturerIdNumber.isChecked = false
                chipName.isChecked = false
            }
        }
        popup.show()
    }


    private fun collectPaging() {
        loadStateListener?.let { lecturerManipulationAdapter.removeLoadStateListener(it) }
        loadStateListener = { loadState ->
            val isDataLoaded =
                loadState.source.refresh is LoadState.NotLoading && lecturerManipulationAdapter.itemCount > 0
            val isLoading = loadState.source.refresh is LoadState.Loading
            val isError = listOf(
                loadState.source.append,
                loadState.source.prepend,
                loadState.source.refresh
            ).any { it is LoadState.Error }
            val isEmptyData =
                loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && lecturerManipulationAdapter.itemCount == 0

            when {
                isLoading -> {
                    showLoadingView(true)
                }

                isDataLoaded -> {
                    showDefaultView(true)
                    showLoadingView(false)
                    isSearching = false
                }

                isError -> {
                    showLoadingView(false)
                    val errorMessage =
                        (loadState.source.append as? LoadState.Error)?.error?.localizedMessage
                            ?: (loadState.source.prepend as? LoadState.Error)?.error?.localizedMessage
                            ?: (loadState.source.refresh as? LoadState.Error)?.error?.localizedMessage

                    errorMessage?.let { message ->
                        if (message.contains(ErrorCode.UNAUTHORIZED.value)) {
                            showDefaultView(true)
                            showFailedConnectView(false)
                            alertError(getString(R.string.text_const_unauthorized))
                        } else {
                            val isEmpty = lecturerManipulationAdapter.itemCount == 0
                            val isNotEmpty = lecturerManipulationAdapter.itemCount > 0
                            showDefaultView(isNotEmpty)
                            showFailedConnectView(isEmpty)
                            defaultStateLoadAndSearch()
                            showSnackBarError(message)
                        }
                    }
                }

                isEmptyData -> {
                    showLoadingView(false)
                    val isInitialLoadAndIsNotSearching = (isInitialLoad && !isSearching)
                    val isSearchingAndIsEmptyKeywordSearch = (isSearching && isEmptyKeywordSearch)
                    val noDataText: String =
                        if (isInitialLoadAndIsNotSearching || isSearchingAndIsEmptyKeywordSearch) {
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
            }
        }
        lecturerManipulationAdapter.addLoadStateListener(loadStateListener!!)
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
                                viewmodel.deleteLecturer(getToken(), userId)
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
                toolbar2.visibility = View.VISIBLE
                fabAdd.visibility = View.VISIBLE
                rvLecturer.animate().alpha(1.0f).setDuration(600)
            } else {
                toolbar.visibility = View.INVISIBLE
                toolbar2.visibility = View.GONE
                fabAdd.visibility = View.GONE
                rvLecturer.alpha = 0f
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

    private fun setToolbarVisible() {
        binding.toolbar.isVisible = true
    }

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
        private const val STATUS_CONFIRM_DELETE = "status_deleted"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
    }
}