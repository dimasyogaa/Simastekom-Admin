package com.yogadimas.simastekom.ui.student.identity.academic

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.adapter.student.identityacademic.IdentityAcademicAdapter
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.paging.LoadingStateAdapter
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityAcademicBinding
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentIdentityAcademicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentIdentityAcademicBinding

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private lateinit var identityAcademicAdapter: IdentityAcademicAdapter
    private var snackbar: Snackbar? = null
    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentIdentityAcademicBinding.inflate(layoutInflater)
        setContentView(binding.root)



        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    val token = getToken()
                    token?.let { mainCall(it) }
                }
            }


        }

    }


    private suspend fun getToken(): String? {
        val user = authViewModel.getUser().asFlow().first()
        val token = user.first
        return if (token == AuthPreferences.DEFAULT_VALUE) {
            goToLogin(this@StudentIdentityAcademicActivity)
            null
        } else {
            token
        }
    }

    private fun mainCall(token: String) {
        binding.apply {
            toolbar.setNavigationOnClickListener {
                finish()
            }

            toolbar.menu.findItem(R.id.refreshMenu).setOnMenuItemClickListener {
                getData(token)
                true
            }

            searchView.setupWithSearchBar(searchBar)
            searchView
                .editText
                .setOnEditorActionListener { _, _, _ ->
                    searchBar.setText(searchView.text)
                    lifecycleScope.launch {
                        searchView.hide()
                        adminViewModel.getIdentitiesAcademic(
                            token,
                            binding.searchView.text.toString().trim(),
                            null,
                            null
                        ).collectLatest { pagingData ->
                            identityAcademicAdapter.submitData(pagingData)
                        }

                    }

                    false
                }

            chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            listOf(chipSortBy, chipStudentIdNumber).forEach { chipId ->
                chipId.setOnCheckedChangeListener { chip, isChecked ->
                    if (isChecked) {
                        when (chipId) {
                            chipSortBy -> {
                                showMenu(
                                    chip, R.menu.top_appbar_sort_by_menu, token, "created_at"
                                )
                            }

                            chipStudentIdNumber -> {
                                showMenu(
                                    chip, R.menu.top_appbar_sort_smallest_largest_menu, token, "nim"
                                )
                            }
                        }

                    }
                }
            }


            rvIdentityAcademic.layoutManager =
                LinearLayoutManager(this@StudentIdentityAcademicActivity)

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getData(token) }
        }
        identityAcademicAdapter = IdentityAcademicAdapter()


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
        onBackPressedDispatcher.addCallback(this, callback)

        getData(token)
    }

    private fun getData(token: String) {

        setSearchBarViewNull()


        val footerAdapter = LoadingStateAdapter {
            identityAcademicAdapter.retry()
        }

        binding.rvIdentityAcademic.adapter = identityAcademicAdapter.withLoadStateFooter(
            footer = footerAdapter
        )





        identityAcademicAdapter.addLoadStateListener { loadState ->
            // Cek jika data berhasil dimuat
            val isDataLoaded = loadState.source.refresh is LoadState.NotLoading &&
                    identityAcademicAdapter.itemCount > 0

            // Cek jika sedang dalam proses loading data pertama kali
            val isLoading = loadState.source.refresh is LoadState.Loading

            // Cek jika terjadi error
            val isError = loadState.source.append is LoadState.Error ||
                    loadState.source.prepend is LoadState.Error ||
                    loadState.source.refresh is LoadState.Error


            val isEmptyData =
                loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && identityAcademicAdapter.itemCount == 0



            when {
                isLoading -> {
                    showLoadingMain(true)
                }

                isDataLoaded -> {
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            isVisibleAllView(true)
                            showLoadingMain(false)
                        }
                    }


                }

                isError -> {
                    showLoadingMain(false)
                    // Terjadi error di tengah pagination
                    val errorState = when {
                        loadState.source.append is LoadState.Error -> loadState.source.append as LoadState.Error
                        loadState.source.prepend is LoadState.Error -> loadState.source.prepend as LoadState.Error
                        loadState.source.refresh is LoadState.Error -> loadState.source.refresh as LoadState.Error
                        else -> null
                    }
                    errorState?.let {
                        errorState.error.localizedMessage?.let { message ->
                            if (message.contains("401")) {
                                isVisibleAllView(true)
                                failedToConnect(false)
                                showAlertDialog(
                                    getString(R.string.text_const_unauthorized)
                                )
                            } else {
                                if (identityAcademicAdapter.itemCount == 0) {
                                    // Error terjadi sejak awal, tampilkan error dan sembunyikan view lainnya
                                    failedToConnect(true)
                                    isVisibleAllView(false)
                                } else {
                                    // Error terjadi di tengah pagination, tampilkan pesan error
                                    failedToConnect(false)
                                }
                                showSnackBarError(
                                    message = message
                                )
                            }
                        }

                    }
                }

                isEmptyData -> {
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            isVisibleAllView(true)
                            showLoadingMain(false)
                            Toast.makeText(
                                this@StudentIdentityAcademicActivity,
                                getString(R.string.text_not_found), Toast.LENGTH_SHORT
                            ).show()

                        }


                    }
                }

            }
        }


        lifecycleScope.launch {
            launch {

                adminViewModel.getIdentitiesAcademic(token).collectLatest { pagingData ->
                    identityAcademicAdapter.submitData(pagingData)
                }

            }


            // Mengelola error dari errorStateFlow
            launch {
                adminViewModel.errorStateFlow.collect { errorMessage ->
                    errorMessage?.let {
                        if (errorMessage.contains(ErrorCode.UNAUTHORIZED.value)) {
                            isVisibleAllView(true)
                            failedToConnect(false)
                            showAlertDialog(
                                getString(R.string.text_const_unauthorized)
                            )
                        }
                    }
                }
            }
        }
    }


    private fun setSearchBarViewNull() {
        binding.searchBar.setText(null)
        binding.searchView.setText(null)
    }

    private fun showAlertDialog(msg: String = "") {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        val title: String
        val message: String
        val icon: Drawable?



        if (unauthorized) {
            icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
            title = getString(R.string.title_dialog_login_again)
            message = getString(R.string.text_please_login_again)
        } else {
            icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
            title = getString(R.string.text_error, "")
            message = msg
        }


        if (dialog == null) {
            dialog = MaterialAlertDialogBuilder(this).apply {
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
                        goToLogin(this@StudentIdentityAcademicActivity)
                    } else {
                        lifecycleScope.launch {
                            getToken()?.let { getData(it) }
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

    private fun showLoadingMain(isLoading: Boolean) {
        val shouldShowLoading = isLoading && !isAlertDialogShow
        showLoading(binding.mainProgressBar, shouldShowLoading)

        if (isLoading) {
            isVisibleAllView(false)
            failedToConnect(false)
        }
    }

    private fun isVisibleAllView(isVisible: Boolean) {
        binding.apply {
            if (isVisible) {
                toolbar.visibility = View.VISIBLE
                toolbar2.visibility = View.VISIBLE
                rvIdentityAcademic.apply {
                    animate().alpha(1.0f).setDuration(600)
                }
            } else {
                toolbar.visibility = View.INVISIBLE
                toolbar2.visibility = View.GONE
                rvIdentityAcademic.alpha = 0f
            }
        }
    }

    private fun failedToConnect(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    private fun showSnackBarError(message: String) {
        initSnackBar(message)
        snackbar?.show()
    }

    private fun initSnackBar(message: String) {
        try {
            snackbar = Snackbar.make(
                this@StudentIdentityAcademicActivity,
                binding.root as ViewGroup,
                message,
                Snackbar.LENGTH_LONG
            )
        } catch (_: Exception) {
        }
    }

    private fun showMenu(v: View, menuRes: Int, token: String, sortBy: String) {
        val popup = PopupMenu(this@StudentIdentityAcademicActivity, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.ascMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.getIdentitiesAcademic(
                            token,
                            binding.searchView.text.toString().trim(),
                            sortBy,
                            SortDir.ASC.value
                        ).collectLatest { pagingData ->
                            identityAcademicAdapter.submitData(pagingData)
                        }
                    }

                    true
                }

                R.id.descMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.getIdentitiesAcademic(
                            token,
                            binding.searchView.text.toString().trim(),
                            sortBy,
                            SortDir.DESC.value
                        ).collectLatest { pagingData ->
                            identityAcademicAdapter.submitData(pagingData)
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

    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
    }


}
