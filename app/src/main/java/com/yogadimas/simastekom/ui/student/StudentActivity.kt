package com.yogadimas.simastekom.ui.student

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.adapter.student.StudentManipulationAdapter
import com.yogadimas.simastekom.common.custom.CustomItemDecoration
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.common.paging.LoadingStateAdapter
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityStudentBinding
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private lateinit var studentManipulationAdapter: StudentManipulationAdapter
    private var snackbar: Snackbar? = null
    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false
    private var isDialogShowingOrientationSuccess = false
    private var isCallback = false
    private var isAdded = false
    private var isDeleted = false

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == StudentManipulationActivity.KEY_RESULT_CODE && result.data != null) {
            isCallback = true
            val successText =
                result.data?.getStringExtra(StudentManipulationActivity.KEY_EXTRA_SUCCESS)
                    .orEmpty()
            if (successText.contains(getString(R.string.text_to_add).lowercase())) {
                isAdded = true
            } else if (successText.contains(getString(R.string.text_to_delete).lowercase())) {
                isDeleted = true
            }
            showAlertDialog(successText, STATUS_SUCCESS)
        }
    }

    private lateinit var customItemDecoration: CustomItemDecoration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        customItemDecoration = CustomItemDecoration()

        lifecycleScope.launch {
            getToken()?.let { token ->
                withContext(Dispatchers.Main) {
                    adminViewModel.studentState.collectLatest { state ->
                        if (isDeleted) {
                            when (state) {
                                is State.Loading -> showLoadingIndicator()
                                is State.Success -> getData(token)
                                is State.ErrorClient -> showErrorClient(state.error)
                                is State.ErrorServer -> showErrorServer(state.error)
                            }
                        }
                    }
                }
            }

        }

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    if (savedInstanceState != null) {
                        isDialogShowingOrientationSuccess =
                            savedInstanceState.getBoolean(KEY_DIALOG_SHOWING_SUCCESS)
                        if (isDialogShowingOrientationSuccess) {
                            showAlertDialog(status = STATUS_SUCCESS)
                        }
                    }

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
            goToLogin(this@StudentActivity)
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
                        adminViewModel.searchSortStudents(
                            token,
                            binding.searchView.text.toString().trim(),
                            null,
                            null
                        ).collectLatest { pagingData ->
                            studentManipulationAdapter.submitData(pagingData)
                        }

                    }

                    false
                }

            chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE
            listOf(chipSortBy, chipStudentIdNumber, chipName, chipMajor).forEach { chipId ->
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
                            else -> {
                                showMenu(
                                    chip, R.menu.top_appbar_sort_asc_desc_menu, token, when (chipId) {
                                        chipName -> "nama-lengkap"
                                        chipMajor -> "jurusan-nama"
                                        else -> ""
                                    }
                                )
                            }
                        }

                    }
                }
            }

            fabAdd.setOnClickListener {
                resultLauncher.launch(
                    Intent(
                        this@StudentActivity,
                        StudentManipulationActivity::class.java
                    )
                )
            }
            rvStudent.layoutManager = LinearLayoutManager(this@StudentActivity)

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getData(token) }
        }
        studentManipulationAdapter =
            StudentManipulationAdapter(object : OnItemClickManipulationCallback<StudentData> {
                override fun onItemClicked(data: StudentData) {
                    val intent = Intent(
                        this@StudentActivity,
                        StudentManipulationActivity::class.java
                    ).apply {
                        putExtra(StudentManipulationActivity.KEY_EXTRA_ID, data.id)
                        putExtra(StudentManipulationActivity.KEY_EXTRA_USER_TYPE, data.userType)
                    }
                    resultLauncher.launch(intent)
                }

                override fun onDeleteClicked(data: StudentData) {
                    val label =
                        "${data.studentIdNumber} | ${data.fullName}"
                    showAlertDialog(
                        label,
                        STATUS_DELETED,
                        data.id ?: "0"
                    )
                }
            })


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
            studentManipulationAdapter.retry()
        }

        binding.rvStudent.adapter = studentManipulationAdapter.withLoadStateFooter(
            footer = footerAdapter
        )





        studentManipulationAdapter.addLoadStateListener { loadState ->
            // Cek jika data berhasil dimuat
            val isDataLoaded = loadState.source.refresh is LoadState.NotLoading &&
                    studentManipulationAdapter.itemCount > 0

            // Cek jika sedang dalam proses loading data pertama kali
            val isLoading = loadState.source.refresh is LoadState.Loading

            // Cek jika terjadi error
            val isError = loadState.source.append is LoadState.Error ||
                    loadState.source.prepend is LoadState.Error ||
                    loadState.source.refresh is LoadState.Error


            val isEmptyData =
                loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && studentManipulationAdapter.itemCount == 0



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
                                    getString(R.string.text_const_unauthorized),
                                    STATUS_ERROR
                                )
                            } else {
                                if (studentManipulationAdapter.itemCount == 0) {
                                    // Error terjadi sejak awal, tampilkan error dan sembunyikan view lainnya
                                    failedToConnect(true)
                                    isVisibleAllView(false)
                                } else {
                                    // Error terjadi di tengah pagination, tampilkan pesan error
                                    failedToConnect(false)
                                }
                                showSnackBarError(
                                    true,
                                    message = message
                                )
                            }
                        }

                    }
                }

                isEmptyData -> {
                    lifecycleScope.launch {
                        if (!isCallback) {
                            withContext(Dispatchers.Main) {
                                isVisibleAllView(true)
                                showLoadingMain(false)
                                Toast.makeText(
                                    this@StudentActivity,
                                    getString(R.string.text_not_found), Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                    }
                }

            }
        }


        lifecycleScope.launch {
            // Mengambil data dan mengirimkan ke adapter
            launch {
                val studentFlow = if (isAdded) {
                    adminViewModel.getAllStudents(token, "desc")
                } else {
                    adminViewModel.getAllStudents(token)
                }

                studentFlow.collectLatest { pagingData ->
                    studentManipulationAdapter.submitData(pagingData)
                }
            }


            // Mengelola error dari errorStateFlow
            launch {
                adminViewModel.errorStateFlow.collect { errorMessage ->
                    errorMessage?.let {
                        if (errorMessage.contains("401")) {
                            isVisibleAllView(true)
                            failedToConnect(false)
                            showAlertDialog(
                                getString(R.string.text_const_unauthorized),
                                STATUS_ERROR
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showLoadingIndicator() {
        showLoadingMain(true)
        failedToConnect(false)
    }

    private fun showErrorClient(error: Errors) {
        showLoadingMain(false)
        failedToConnect(false)

        var message = error.errors?.message?.first() ?: ""

        if (message.contains(getString(R.string.text_const_unauthorized))) {
            isVisibleAllView(true)
            message = getString(R.string.text_const_unauthorized)
        }

        showAlertDialog(msg = message, status = STATUS_ERROR)
    }

    private fun showErrorServer(errorMessage: String) {
        showLoadingMain(false)
        failedToConnect(true)
        isVisibleAllView(false)
        showSnackBarError(
            message = errorMessage
        )
    }

    private fun setSearchBarViewNull() {
        binding.searchBar.setText(null)
        binding.searchView.setText(null)
    }

    private fun showAlertDialog(msg: String = "", status: String, id: String = "0") {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null
        when (status) {
            STATUS_DELETED -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_delete)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(this, R.color.md_theme_error)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_delete)
                message = getString(R.string.text_question_do_you_want_to_delete, msg)
            }

            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(this, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = msg
            }

            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.title_dialog_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error, "")
                    message = msg
                }

            }
        }

        if (dialog == null) {
            if (status == STATUS_SUCCESS) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500)
                    isAdded = false
                    isDeleted = false
                    isAlertDialogShow = false
                    isDialogShowingOrientationSuccess = false
                    dialog?.dismiss()
                    dialog = null
                }
            }

            dialog = MaterialAlertDialogBuilder(this).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (status == STATUS_DELETED || status == STATUS_ERROR) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        isAlertDialogShow = false
                        dialog = null
                        when (status) {
                            STATUS_DELETED -> lifecycleScope.launch {
                                getToken()?.let {
                                    isDeleted = true
                                    adminViewModel.deleteStudent(it, id)
                                }
                            }

                            STATUS_ERROR -> {
                                if (unauthorized) {
                                    authViewModel.saveUser(
                                        null,
                                        null,
                                        null
                                    )
                                    goToLogin(this@StudentActivity)
                                } else {
                                    lifecycleScope.launch {
                                        getToken()?.let { getData(it) }
                                    }
                                    return@setPositiveButton
                                }

                            }

                        }
                    }

                    if (status == STATUS_DELETED) {
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
                fabAdd.visibility = View.VISIBLE
                rvStudent.apply {
                    animate().alpha(1.0f).setDuration(600)
                }
            } else {
                toolbar.visibility = View.INVISIBLE
                toolbar2.visibility = View.GONE
                fabAdd.visibility = View.GONE
                rvStudent.alpha = 0f
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

    private fun showSnackBarError(boolean: Boolean = true, message: String) {
        initSnackBar(message)
        if (boolean) {
            if (binding.fabAdd.isVisible) {
                snackbar?.anchorView = binding.fabAdd
            }
            snackbar?.show()
        } else snackbar?.dismiss()
    }

    private fun initSnackBar(message: String) {
        try {
            snackbar = Snackbar.make(
                this@StudentActivity,
                binding.root as ViewGroup,
                message,
                Snackbar.LENGTH_LONG
            )
        } catch (_: Exception) {
        }
    }

    private fun showMenu(v: View, menuRes: Int, token: String, sortBy: String) {
        val popup = PopupMenu(this@StudentActivity, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.ascMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.searchSortStudents(
                            token,
                            binding.searchView.text.toString().trim(),
                            sortBy,
                            SortDir.ASC.value
                        ).collectLatest { pagingData ->
                            studentManipulationAdapter.submitData(pagingData)
                        }
                    }

                    true
                }

                R.id.descMenu -> {
                    lifecycleScope.launch {
                        adminViewModel.searchSortStudents(
                            token,
                            binding.searchView.text.toString().trim(),
                            sortBy,
                            SortDir.DESC.value
                        ).collectLatest { pagingData ->
                            studentManipulationAdapter.submitData(pagingData)
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
                chipName.isChecked = false
                chipMajor.isChecked = false
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_DIALOG_SHOWING_SUCCESS, isDialogShowingOrientationSuccess)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATUS_DELETED = "status_deleted"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val KEY_DIALOG_SHOWING_SUCCESS = "key_dialog_showing_success"
    }


}