package com.yogadimas.simastekom.ui.identity.personal

import android.app.Activity
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import com.yogadimas.simastekom.adapter.student.identitypersonal.IdentityPersonalAdapter
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.enums.FieldType
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.enums.SortBy
import com.yogadimas.simastekom.common.helper.ToastHelper
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.sendMessage
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.paging.LoadingStateAdapter
import com.yogadimas.simastekom.databinding.ActivityIdentityPersonalBinding
import com.yogadimas.simastekom.model.responses.UserCurrent
import com.yogadimas.simastekom.ui.student.StudentManipulationActivity.Companion.KEY_EXTRA_ID
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class IdentityPersonalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentityPersonalBinding


    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private lateinit var identityPersonalAdapter: IdentityPersonalAdapter
    private var snackbar: Snackbar? = null
    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false
    private lateinit var role: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentityPersonalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        role = intent.getStringExtra(KEY_EXTRA_ROLE) ?: Role.STUDENT.value


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
            goToLogin(this@IdentityPersonalActivity)
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
                        adminViewModel.getIdentitiesPersonal(
                            token,
                            binding.searchView.text.toString().trim(),
                            null,
                            null
                        ).collectLatest { pagingData ->
                            identityPersonalAdapter.submitData(pagingData)
                        }

                    }

                    false
                }

            chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            val roleAdmin = Role.ADMIN.value
            val roleLecture = Role.LECTURE.value
            chipUser.text = when(role) {
                roleAdmin ->  getString(R.string.text_label_id_username)
                roleLecture ->  getString(R.string.text_label_lecture_id_number)
                else ->  getString(R.string.text_label_student_id_number)
            }
            listOf(chipSortBy, chipUser).forEach { chipId ->
                chipId.setOnCheckedChangeListener { chip, isChecked ->


                    val (userSortBy, userSortDirTextMenu) = when (role) {
                        roleAdmin -> SortBy.USERNAME.value to R.menu.top_appbar_sort_asc_desc_menu
                        roleLecture -> SortBy.LECTURE_ID_NUMBER.value to R.menu.top_appbar_sort_smallest_largest_menu
                        else -> SortBy.STUDENT_ID_NUMBER.value to R.menu.top_appbar_sort_smallest_largest_menu
                    }
                    if (isChecked) {
                        val menuRes = when (chipId) {
                            chipSortBy -> R.menu.top_appbar_sort_by_menu
                            chipUser -> userSortDirTextMenu
                            else -> null
                        }
                        menuRes?.let {
                            showMenu(chip, it, token, if (chipId == chipSortBy) SortBy.CREATED_AT.value else userSortBy)
                        }
                    }
                }
            }




            rvIdentityPersonal.layoutManager =
                LinearLayoutManager(this@IdentityPersonalActivity)

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getData(token) }
        }
        identityPersonalAdapter = IdentityPersonalAdapter { data, fieldType ->

            val userTypeReceiver: Role? = when(data.userType) {
                Role.ADMIN.value -> Role.ADMIN
                Role.LECTURE.value -> Role.LECTURE
                Role.STUDENT.value -> Role.STUDENT
                else -> null
            }

            val userCurrent = data.userCurrent ?: UserCurrent()
            when(fieldType) {
                FieldType.EMAIL -> {
                    sendMessage(
                        userCurrent = UserCurrent(userCurrent.userType, userCurrent.name, userCurrent.identity),
                        emailAddress = data.email,
                        receiverRole = userTypeReceiver,
                        receiverName = data.name,
                        receiverLectureGender = data.gender,
                        isWhatsApp = false,
                        context = this@IdentityPersonalActivity
                    )
                }
                FieldType.PHONE -> {
                    sendMessage(
                        userCurrent = UserCurrent(userCurrent.userType, userCurrent.name, userCurrent.identity),
                        receiverPhoneNumber = data.phone,
                        receiverRole = userTypeReceiver,
                        receiverName = data.name,
                        receiverLectureGender = data.gender,
                        context = this@IdentityPersonalActivity
                    )
                }
            }

        }


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
            identityPersonalAdapter.retry()
        }

        binding.rvIdentityPersonal.adapter = identityPersonalAdapter.withLoadStateFooter(
            footer = footerAdapter
        )


        identityPersonalAdapter.addLoadStateListener { loadState ->
            // Cek jika data berhasil dimuat
            val isDataLoaded = loadState.source.refresh is LoadState.NotLoading &&
                    identityPersonalAdapter.itemCount > 0

            // Cek jika sedang dalam proses loading data pertama kali
            val isLoading = loadState.source.refresh is LoadState.Loading

            // Cek jika terjadi error
            val isError = loadState.source.append is LoadState.Error ||
                    loadState.source.prepend is LoadState.Error ||
                    loadState.source.refresh is LoadState.Error


            val isEmptyData =
                loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && identityPersonalAdapter.itemCount == 0



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
                            if (message.contains(ErrorCode.UNAUTHORIZED.value)) {
                                isVisibleAllView(true)
                                failedToConnect(false)
                                showAlertDialog(
                                    getString(R.string.text_const_unauthorized)
                                )
                            } else {
                                if (identityPersonalAdapter.itemCount == 0) {
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
                            ToastHelper.showCustomToast(this@IdentityPersonalActivity,  getString(R.string.text_not_found))




                        }


                    }
                }

            }
        }


        lifecycleScope.launch {
            launch {

                adminViewModel.getIdentitiesPersonal(token).collectLatest { pagingData ->
                    identityPersonalAdapter.submitData(pagingData)
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
                        goToLogin(this@IdentityPersonalActivity)
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
                this@IdentityPersonalActivity,
                binding.root as ViewGroup,
                message,
                Snackbar.LENGTH_LONG
            )
        } catch (_: Exception) {
        }
    }

    private fun showMenu(v: View, menuRes: Int, token: String, sortBy: String) {
        val popup = PopupMenu(this@IdentityPersonalActivity, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.ascMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.getIdentitiesPersonal(
                            token,
                            binding.searchView.text.toString().trim(),
                            sortBy,
                            SortDir.ASC.value
                        ).collectLatest { pagingData ->
                            identityPersonalAdapter.submitData(pagingData)
                        }
                    }

                    true
                }

                R.id.descMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.getIdentitiesPersonal(
                            token,
                            binding.searchView.text.toString().trim(),
                            sortBy,
                            SortDir.DESC.value
                        ).collectLatest { pagingData ->
                            identityPersonalAdapter.submitData(pagingData)
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