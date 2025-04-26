package com.yogadimas.simastekom.ui.student

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
import com.yogadimas.simastekom.adapter.student.StudentManipulationAdapter
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.ToastHelper
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.common.paging.LoadingStateAdapter
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityStudentBinding
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.StudentData
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

    private val context = this@StudentActivity

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var loadStateListener: ((CombinedLoadStates) -> Unit)? = null

    private lateinit var studentManipulationAdapter: StudentManipulationAdapter

    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false

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
            alertSuccess(successText)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)



        lifecycleScope.launch {
            getToken()?.let { token ->
                withContext(Dispatchers.Main) {
                    adminViewModel.studentState.collectLatest { state ->
                        if (isDeleted) {
                            when (state) {
                                is State.Loading -> showLoadingIndicator()
                                is State.Success -> setObserveData(token, state.data)
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
                    binding.appBarLayout.isVisible = false
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
        binding.apply {
            setToolbar(token)
            setSearch(token)
            setSort(token)

            fabAdd.setOnClickListener {
                resultLauncher.launch(
                    Intent(
                        context,
                        StudentManipulationActivity::class.java
                    )
                )
            }
            rvStudent.layoutManager = LinearLayoutManager(context)
            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { setObserveData(token) }
        }
        studentManipulationAdapter =
            StudentManipulationAdapter(object : OnItemClickManipulationCallback<StudentData> {
                override fun onItemClicked(data: StudentData) {
                    val intent = Intent(
                        context,
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
                    alertDelete(label, data.id ?: "0")
                }
            })


        onBackPressedDispatcher()

        setObserveData(token)
    }


    private fun ActivityStudentBinding.setToolbar(token: String) = toolbar.apply {
        setNavigationOnClickListener { finish() }
        menu.findItem(R.id.refreshMenu).setOnMenuItemClickListener {
            setObserveData(token)
            true
        }
    }

    private fun ActivityStudentBinding.setSearch(token: String) = searchView.apply {
        setupWithSearchBar(searchBar)
        editText.setOnEditorActionListener { _, _, _ ->
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
    }

    private fun ActivityStudentBinding.setSort(token: String) {
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
                                chip,
                                R.menu.top_appbar_sort_asc_desc_menu,
                                token,
                                when (chipId) {
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


    private fun setObserveData(token: String, studentData: StudentData? = null) {

        clearSearch()

        studentData?.let {
            if (it.isDeleted) {
                val message = "${it.studentIdNumber} | ${it.fullName}"
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
            studentManipulationAdapter.retry()
        }

        binding.rvStudent.adapter = studentManipulationAdapter.withLoadStateFooter(
            footer = footerAdapter
        )

        observePaging()

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

    private fun observePaging() {
        loadStateListener?.let { studentManipulationAdapter.removeLoadStateListener(it) }
        loadStateListener = { loadState ->
            val isDataLoaded = loadState.source.refresh is LoadState.NotLoading && studentManipulationAdapter.itemCount > 0
            val isLoading = loadState.source.refresh is LoadState.Loading
            val isError = listOf(loadState.source.append, loadState.source.prepend, loadState.source.refresh).any { it is LoadState.Error }
            val isEmptyData = loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && studentManipulationAdapter.itemCount == 0

            when {
                isLoading -> showLoadingView(true)

                isDataLoaded -> {
                    showDefaultView(true)
                    showLoadingView(false)
                }

                isError -> {
                    showLoadingView(false)
                    val errorState = (loadState.source.append as? LoadState.Error)
                        ?: (loadState.source.prepend as? LoadState.Error)
                        ?: (loadState.source.refresh as? LoadState.Error)

                    errorState?.error?.localizedMessage?.let { message ->
                        if (message.contains(ErrorCode.UNAUTHORIZED.value)) {
                            showDefaultView(true)
                            showFailedConnectView(false)
                            alertError(getString(R.string.text_const_unauthorized))
                        } else {
                            val isEmpty = studentManipulationAdapter.itemCount == 0
                            val isNotEmpty = studentManipulationAdapter.itemCount > 0
                            showFailedConnectView(isEmpty)
                            showDefaultView(isNotEmpty)
                            showSnackBarError(message)
                        }
                    }
                }

                isEmptyData -> {
                    if (!isCallback) {
                        showDefaultView(true)
                        showLoadingView(false)
                        ToastHelper.showCustomToastActivity(context, getString(R.string.text_not_found))
                    }
                }
            }
        }
        studentManipulationAdapter.addLoadStateListener(loadStateListener!!)
    }


    /*
    private fun observePaging() {
        loadStateListener?.let { studentManipulationAdapter.removeLoadStateListener(it) }
        loadStateListener = { loadState ->
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
                    showLoadingView(true)
                }

                isDataLoaded -> {
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            showDefaultView(true)
                            showLoadingView(false)
                        }
                    }


                }

                isError -> {
                    showLoadingView(false)
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
                                showDefaultView(true)
                                showFailedConnectView(false)
                                alertError(getString(R.string.text_const_unauthorized))
                            } else {
                                if (studentManipulationAdapter.itemCount == 0) {
                                    // Error terjadi sejak awal, tampilkan error dan sembunyikan view lainnya
                                    showFailedConnectView(true)
                                    showDefaultView(false)
                                } else {
                                    // Error terjadi di tengah pagination, tampilkan pesan error
                                    showFailedConnectView(false)
                                }
                                showSnackBarError(message)
                            }
                        }

                    }
                }

                isEmptyData -> {
                    lifecycleScope.launch {
                        if (!isCallback) {
                            withContext(Dispatchers.Main) {
                                showDefaultView(true)
                                showLoadingView(false)
                                Toast.makeText(
                                    context,
                                    getString(R.string.text_not_found), Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                    }
                }

            }
        }
        studentManipulationAdapter.addLoadStateListener(loadStateListener!!)
    }
*/

    private fun showLoadingIndicator() {
        showLoadingView(true)
        showFailedConnectView(false)
    }

    private fun showErrorClient(error: Errors) {
        showLoadingView(false)
        showFailedConnectView(false)

        var message = error.errors?.message?.first() ?: ""

        if (message.contains(getString(R.string.text_const_unauthorized))) {
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

    private fun showAlertDialog(msg: String = "", status: String, userId: String = "0") {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
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
                                getToken()?.let {
                                    isDeleted = true
                                    adminViewModel.deleteStudent(it, userId)
                                }
                            }

                            STATUS_ERROR -> {
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


    private fun showLoadingView(isLoading: Boolean) {
        val shouldShowLoading = isLoading && !isAlertDialogShow
        showLoading(binding.mainProgressBar, shouldShowLoading)

        if (isLoading) {
            showDefaultView(false)
            showFailedConnectView(false)
        }
    }

    private fun showDefaultView(isVisible: Boolean) {
        binding.apply {
            appBarLayout.isVisible = isVisible
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

    private fun showFailedConnectView(boolean: Boolean) {
        binding.viewHandle.viewFailedConnect.root.isVisible = boolean
    }

    private fun showSnackBarError(message: String) {
        val fabAdd = binding.fabAdd
        val anchorView = if (fabAdd.isVisible) fabAdd else null
        SnackBarHelper.display(
            viewGroup = binding.root as ViewGroup,
            message = message,
            lifecycleOwner = context,
            anchorView = anchorView
        )
    }

    private fun showMenu(v: View, menuRes: Int, token: String, sortBy: String) {
        val popup = PopupMenu(context, v)
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

    companion object {
        private const val STATUS_CONFIRM_DELETE = "status_deleted"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
    }


}