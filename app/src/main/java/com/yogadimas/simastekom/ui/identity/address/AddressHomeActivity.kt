package com.yogadimas.simastekom.ui.identity.address

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
import com.yogadimas.simastekom.adapter.address.AddressAdapter
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.common.enums.SortBy
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.ToastHelper
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.paging.LoadingStateAdapter
import com.yogadimas.simastekom.databinding.ActivityAddressHomeBinding
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddressHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressHomeBinding

    private val context = this@AddressHomeActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var loadStateListener: ((CombinedLoadStates) -> Unit)? = null

    private lateinit var addressAdapter: AddressAdapter

    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false

    private var isInitialLoad = true
    private var isSearching = false
    private var isEmptyKeywordSearch = true

    private lateinit var userTypeRole: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleFromIntent = intent.getStringExtra(KEY_EXTRA_ROLE) ?: Role.STUDENT.value

        adminViewModel.intentData = roleFromIntent

        userTypeRole = adminViewModel.intentData

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    val token = getToken()
                    token?.let { setMainContent(it, Role.fromValue(userTypeRole) ?: Role.STUDENT) }
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

    private fun setMainContent(token: String, role: Role) {
        setToolbar(token, role)
        setSearch(token, role)
        setSort(token, role)

        binding.apply {
            rvIdentityPersonal.layoutManager =
                LinearLayoutManager(context)

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener {
                setObserveData(token, role)
            }
        }

        addressAdapter = AddressAdapter()

        onBackPressedDispatcher()

        setObserveData(token, role)
    }

    private fun setToolbar(token: String, role: Role) = binding.toolbar.apply {
        setNavigationOnClickListener { finish() }
        subtitle = if (role == Role.PARENT) {
            getString(R.string.text_parent)
        } else {
            getString(R.string.text_personal)
        }
        menu.findItem(R.id.refreshMenu).setOnMenuItemClickListener {
            setObserveData(token, role)
            true
        }
    }

    private fun setSearch(token: String, role: Role) = binding.searchView.apply {
        val searchBar = binding.searchBar
        setupWithSearchBar(searchBar)
        editText
            .setOnEditorActionListener { _, _, _ ->
                searchBar.setText(text)
                lifecycleScope.launch {
                    hide()
                    val keyword = text.toString().trim()
                    isEmptyKeywordSearch = keyword.isEmpty()

                    adminViewModel.getAddresses(
                        token,
                        keyword,
                        null,
                        null,
                        role
                    ).collectLatest { pagingData ->
                        isSearching = true
                        addressAdapter.submitData(pagingData)
                    }

                }

                false
            }
    }

    private fun setSort(token: String, role: Role) = binding.apply {
        chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
        chipUser.text = when (role) {
            Role.ADMIN -> getString(R.string.text_label_id_username)
            Role.LECTURE -> getString(R.string.text_label_lecture_id_number)
            else -> getString(R.string.text_label_student_id_number)
        }
        listOf(chipSortBy, chipUser).forEach { chipId ->
            chipId.setOnCheckedChangeListener { chip, isChecked ->
                val (userSortBy, userSortDirTextMenu) = when (role) {
                    Role.ADMIN -> SortBy.USERNAME.value to R.menu.top_appbar_sort_asc_desc_menu
                    Role.LECTURE -> SortBy.LECTURE_ID_NUMBER.value to R.menu.top_appbar_sort_smallest_largest_menu
                    else -> SortBy.STUDENT_ID_NUMBER.value to R.menu.top_appbar_sort_smallest_largest_menu
                }
                if (isChecked) {
                    val menuRes = when (chipId) {
                        chipSortBy -> R.menu.top_appbar_sort_by_menu
                        chipUser -> userSortDirTextMenu
                        else -> null
                    }
                    menuRes?.let {
                        showMenu(
                            chip,
                            it,
                            token,
                            if (chipId == chipSortBy) SortBy.CREATED_AT.value else userSortBy,
                            role
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

    private fun setObserveData(token: String, role: Role) {
        defaultStateLoadAndSearch()
        clearSearch()

        val footerAdapter = LoadingStateAdapter {
            addressAdapter.retry()
        }
        binding.rvIdentityPersonal.adapter = addressAdapter.withLoadStateFooter(
            footer = footerAdapter
        )

        observePaging()

        lifecycleScope.launch {
            launch {

                adminViewModel.getAddresses(token, role = role).collectLatest { pagingData ->
                    addressAdapter.submitData(pagingData)
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
        loadStateListener?.let { addressAdapter.removeLoadStateListener(it) }
        loadStateListener = { loadState ->
            val isDataLoaded =
                loadState.source.refresh is LoadState.NotLoading && addressAdapter.itemCount > 0
            val isLoading = loadState.source.refresh is LoadState.Loading
            val isError = listOf(
                loadState.source.append,
                loadState.source.prepend,
                loadState.source.refresh
            ).any { it is LoadState.Error }
            val isEmptyData =
                loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && addressAdapter.itemCount == 0

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
                            val isEmpty = addressAdapter.itemCount == 0
                            val isNotEmpty = addressAdapter.itemCount > 0
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
                    ToastHelper.showCustomToast(context, noDataText)
                }
            }
        }
        addressAdapter.addLoadStateListener(loadStateListener!!)
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
            title = getString(R.string.text_error, "")
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
                            getToken()?.let {
                                setObserveData(
                                    it,
                                    Role.fromValue(userTypeRole) ?: Role.STUDENT
                                )
                            }
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

    private fun showLoadingView(isLoading: Boolean) {
        val shouldShowLoading = isLoading && !isAlertDialogShow
        showLoading(binding.mainProgressBar, shouldShowLoading)

        if (isLoading) {
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
                rvIdentityPersonal.apply {
                    animate().alpha(1.0f).setDuration(600)
                }
            } else {
                toolbar.visibility = View.INVISIBLE
                toolbar2.visibility = View.GONE
                rvIdentityPersonal.alpha = 0f
            }
        }
    }

    private fun showFailedConnectView(isVisible: Boolean) {
        binding.viewHandle.viewFailedConnect.root.isVisible = isVisible
    }

    private fun showSnackBarError(message: String) {
        SnackBarHelper.display(
            viewGroup = binding.root as ViewGroup,
            message = message,
            lifecycleOwner = context,
        )
    }

    private fun showEmptyDataView(isVisible: Boolean) {
        binding.viewHandle.viewEmptyData.root.isVisible = isVisible
    }

    private fun showMenu(v: View, menuRes: Int, token: String, sortBy: String, role: Role) {
        val popup = PopupMenu(context, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        val keyword = binding.searchView.text.toString().trim()
        isEmptyKeywordSearch = keyword.isEmpty()

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.ascMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.getAddresses(
                            token,
                            keyword,
                            sortBy,
                            SortDir.ASC.value,
                            role
                        ).collectLatest { pagingData ->
                            isSearching = true
                            addressAdapter.submitData(pagingData)
                        }
                    }

                    true
                }

                R.id.descMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.getAddresses(
                            token,
                            keyword,
                            sortBy,
                            SortDir.DESC.value,
                            role
                        ).collectLatest { pagingData ->
                            isSearching = true
                            addressAdapter.submitData(pagingData)
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
                chipUser.isChecked = false
            }
        }
        popup.show()
    }


    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
    }


    companion object {
        const val KEY_EXTRA_ROLE = "key_extra_role"
    }
}