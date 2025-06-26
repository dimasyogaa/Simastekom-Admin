package com.yogadimas.simastekom.ui.student.identity.parent

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.yogadimas.simastekom.adapter.student.identityparent.StudentIdentityParentAdapter
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.common.enums.SortBy
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.ToastHelper
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.sendMessage
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.paging.LoadingStateAdapter
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityParentBinding
import com.yogadimas.simastekom.model.responses.UserCurrent
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentIdentityParentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentIdentityParentBinding

    private val context = this@StudentIdentityParentActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var loadStateListener: ((CombinedLoadStates) -> Unit)? = null

    private lateinit var studentIdentityParentAdapter: StudentIdentityParentAdapter

    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false

    private var isInitialLoad = true
    private var isSearching = false
    private var isEmptyKeywordSearch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentIdentityParentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBarLayout.isVisible = false

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    val token = getToken()
                    token?.let { setMainContent(it) }
                }
            }
        }

    }

    private suspend fun getToken(): String? {
        val user = authViewModel.getUser().asFlow().first()
        val token = user.first
        return if (token == AuthPreferences.DEFAULT_VALUE) {
            goToLogin(context)
            null
        } else {
            token
        }
    }

    private fun setMainContent(token: String) {
        setToolbar(token)
        setSearch(token)
        setSort(token)
        binding.apply {
            rvStudentIdentityParent.layoutManager =
                LinearLayoutManager(context)

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener {
                setObserveData(token)
            }
        }
        studentIdentityParentAdapter = StudentIdentityParentAdapter { data ->
            val userCurrent = data.userCurrent ?: UserCurrent()
            sendMessage(
                userCurrent = UserCurrent(
                    userCurrent.userType,
                    userCurrent.name,
                    userCurrent.identity
                ),
                receiverPhoneNumber = data.phone,
                receiverRole = Role.PARENT,
                receiverName = data.studentName,
                context = context
            )

        }
        onBackPressedDispatcher()
        setObserveData(token)
    }

    private fun setToolbar(token: String) = binding.toolbar.apply {
        setNavigationOnClickListener { finish() }
        menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener {
            setObserveData(token)
            true
        }
    }

    private fun setSearch(token: String) = binding.searchView.apply {
        val searchBar = binding.searchBar
        setupWithSearchBar(searchBar)
        editText.setOnEditorActionListener { _, _, _ ->
            searchBar.setText(text)
            lifecycleScope.launch {
                hide()
                val keyword = text.toString().trim()
                isEmptyKeywordSearch = keyword.isEmpty()

                adminViewModel.getStudentIdentitiesParent(
                    token,
                    keyword,
                    null,
                    null
                ).collectLatest { pagingData ->
                    isSearching = true
                    studentIdentityParentAdapter.submitData(pagingData)
                }
            }
            false
        }
    }

    private fun setSort(token: String) = binding.apply {
        chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        listOf(chipSortBy, chipStudentIdNumber).forEach { chipId ->
            chipId.setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    val menuRes = when (chipId) {
                        chipSortBy -> R.menu.top_appbar_sort_by_menu
                        chipStudentIdNumber -> R.menu.top_appbar_sort_smallest_largest_menu
                        else -> null
                    }
                    menuRes?.let {
                        showMenu(
                            chip,
                            it,
                            token,
                            if (chipId == chipSortBy) SortBy.CREATEDAT.value else SortBy.STUDENT_ID_NUMBER.value
                        )
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

    private fun setObserveData(token: String) {
        defaultStateLoadAndSearch()
        clearSearch()


        val footerAdapter = LoadingStateAdapter {
            studentIdentityParentAdapter.retry()
        }

        binding.rvStudentIdentityParent.adapter = studentIdentityParentAdapter.withLoadStateFooter(
            footer = footerAdapter
        )


        observePaging()


        lifecycleScope.launch {
            launch {
                adminViewModel.getStudentIdentitiesParent(token).collectLatest { pagingData ->
                    studentIdentityParentAdapter.submitData(pagingData)
                }
            }

            // Mengelola error dari errorStateFlow
            launch {
                adminViewModel.errorStateFlow.collect { errorMessage ->
                    errorMessage?.let {
                        if (errorMessage.contains(ErrorCode.UNAUTHORIZED.value)) {
                            showDefaultView(true)
                            showFailedConnectView(false)
                            showAlertDialog(
                                getString(R.string.text_const_unauthorized)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observePaging() {
        loadStateListener?.let { studentIdentityParentAdapter.removeLoadStateListener(it) }
        loadStateListener = { loadState ->
            val isDataLoaded =
                loadState.source.refresh is LoadState.NotLoading && studentIdentityParentAdapter.itemCount > 0
            val isLoading = loadState.source.refresh is LoadState.Loading
            val isError = listOf(
                loadState.source.append,
                loadState.source.prepend,
                loadState.source.refresh
            ).any { it is LoadState.Error }
            val isEmptyData =
                loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && studentIdentityParentAdapter.itemCount == 0

            when {
                isLoading -> showLoadingView(true)

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
                            showAlertDialog(getString(R.string.text_const_unauthorized))
                        } else {
                            val isEmpty = studentIdentityParentAdapter.itemCount == 0
                            val isNotEmpty = studentIdentityParentAdapter.itemCount > 0
                            showFailedConnectView(isEmpty)
                            showDefaultView(isNotEmpty)
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
        studentIdentityParentAdapter.addLoadStateListener(loadStateListener!!)
    }

    private fun defaultStateLoadAndSearch() {
        isInitialLoad = true
        isSearching = false
        isEmptyKeywordSearch = true
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

                        adminViewModel.getStudentIdentitiesParent(
                            token,
                            keyword,
                            sortBy,
                            SortDir.ASC.value
                        ).collectLatest { pagingData ->
                            isSearching = true
                            studentIdentityParentAdapter.submitData(pagingData)
                        }
                    }

                    true
                }

                R.id.descMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.getStudentIdentitiesParent(
                            token,
                            keyword,
                            sortBy,
                            SortDir.DESC.value
                        ).collectLatest { pagingData ->
                            isSearching = true
                            studentIdentityParentAdapter.submitData(pagingData)
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
                chipStudentIdNumber.isChecked = false
            }
        }
        popup.show()
    }

    private fun clearSearch() {
        binding.searchBar.setText(null)
        binding.searchView.setText(null)
    }

    private fun showAlertDialog(msg: String = "") {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        val title: String
        val message: String
        val icon: Drawable?



        if (unauthorized) {
            icon = ContextCompat.getDrawable(context, R.drawable.z_ic_warning)
            title = getString(R.string.text_login_again)
            message = getString(R.string.text_please_login_again)
        } else {
            icon = ContextCompat.getDrawable(context, R.drawable.z_ic_warning)
            title = getString(R.string.text_error_format, "")
            message = msg
        }


        if (dialog == null) {
            dialog = MaterialAlertDialogBuilder(context).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    isAlertDialogShow = false
                    dialog = null
                    if (unauthorized) {
                        authViewModel.saveUser(
                            null,
                            null,
                            null
                        )
                        goToLogin(context)
                    } else {
                        lifecycleScope.launch {
                            getToken()?.let { setObserveData(it) }
                        }
                        return@setPositiveButton
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
            binding.appBarLayout.isVisible = isVisible
            if (isVisible) {
                toolbar.visibility = View.VISIBLE
                toolbar2.visibility = View.VISIBLE
                rvStudentIdentityParent.apply {
                    animate().alpha(1.0f).setDuration(600)
                }
            } else {
                toolbar.visibility = View.INVISIBLE
                toolbar2.visibility = View.GONE
                rvStudentIdentityParent.alpha = 0f
            }
        }
    }

    private fun showFailedConnectView(isVisible: Boolean) {
        binding.viewHandle.viewFailedConnect.root.isVisible = isVisible
    }

    private fun showEmptyDataView(isVisible: Boolean) {
        binding.viewHandle.viewEmptyData.root.isVisible = isVisible
    }

    private fun showSnackBarError(message: String) {
        SnackBarHelper.display(
            viewGroup = binding.root as ViewGroup,
            message = message,
            lifecycleOwner = context,
        )
    }


    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
    }

}