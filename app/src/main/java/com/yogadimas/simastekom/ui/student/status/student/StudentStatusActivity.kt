package com.yogadimas.simastekom.ui.student.status.student

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.adapter.student.name.NameAdapter
import com.yogadimas.simastekom.adapter.student.name.NameManipulationAdapter
import com.yogadimas.simastekom.databinding.ActivityStudentStatusBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.SortDir
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.model.responses.NameData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentStatusBinding

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var isLoading = false
    private var isAlertDialogShow = false

    private var dialog: AlertDialog? = null

    private var isSuccessDialogShowingOrientation = false


    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == StudentStatusManipulationActivity.KEY_RESULT_CODE && result.data != null) {
            val successText =
                result.data?.getStringExtra(StudentStatusManipulationActivity.KEY_EXTRA_SUCCESS)
                    .orEmpty()
            showAlertDialog(successText, STATUS_SUCCESS)
        }
    }

    private var isFromStudent = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFromStudent = intent.getBooleanExtra(KEY_STUDENT, false)
        if (savedInstanceState != null) {
            isSuccessDialogShowingOrientation =
                savedInstanceState.getBoolean(KEY_SUCCESS_DIALOG_SHOWING)
            if (isSuccessDialogShowingOrientation) {
                showAlertDialog(status = STATUS_SUCCESS)
            }
        }

        mainCall()
    }


    private fun mainCall() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            toolbar.menu.findItem(R.id.refreshMenu).setOnMenuItemClickListener {
                getAdminAndData()
                true
            }

            searchView.setupWithSearchBar(searchBar)

            searchView
                .editText
                .setOnEditorActionListener { _, _, _ ->

                    searchBar.setText(searchView.text)
                    adminViewModel.searchSortStudentStatus(
                        searchView.text.toString().trim(),
                        null,
                        null
                    )

                    searchView.hide()

                    false
                }


            chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE

            listOf(chipSortBy, chipName).forEach { chipId ->
                chipId.setOnCheckedChangeListener { chip, isChecked ->
                    if (isChecked) {
                        if (chipId == chipSortBy) {
                            showMenu(
                                chip, R.menu.top_appbar_sort_by_menu, "created_at"
                            )
                        } else {
                            showMenu(
                                chip, R.menu.top_appbar_sort_asc_desc_menu, when (chipId) {
                                    chipName -> "nama"
                                    else -> ""
                                }
                            )
                        }

                    }
                }
            }



            fabAdd.setOnClickListener {
                resultLauncher.launch(
                    Intent(
                        this@StudentStatusActivity,
                        StudentStatusManipulationActivity::class.java
                    )
                )
            }

            val layoutManager = LinearLayoutManager(this@StudentStatusActivity)
            rvStudentStatus.layoutManager = layoutManager

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdminAndData() }

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
    }


    override fun onStart() {
        super.onStart()
        getAdminAndData()
    }

    private fun getAdminAndData() {
        binding.searchBar.setText(null)
        binding.searchView.setText(null)
        authViewModel.getUser().observe(this) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                adminViewModel.getAllStudentStatuses()
            }
        }

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.nameList.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {


                if (isLoading) {
                    isVisibleAllView(false)
                } else {
                    isVisibleAllView(true)
                }
                failedToConnect(false)

                setData(it)


            }
        }


        adminViewModel.name.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {


                if (it.isDeleted) {
                    val success = getString(R.string.text_success)
                    val label = getString(R.string.text_label_student_status)
                    showAlertDialog(
                        getString(R.string.text_alert_delete, success, label),
                       STATUS_SUCCESS
                    )
                    lifecycleScope.launch {
                        delay(300)
                        adminViewModel.getAllStudentStatuses()
                    }

                }


            }
        }

        adminViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val listMessage = data.errors.message.orEmpty()
                    isVisibleAllView(true)
                    failedToConnect(false)
                    showAlertDialog(listMessage[0], STATUS_ERROR)
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                isVisibleAllView(false)
                failedToConnect(true)
                Snackbar.make(
                    binding.root as ViewGroup,
                    snackBarText,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setData(it: List<NameData>) {
        val adapter =  if (isFromStudent) {
            NameAdapter(object : OnItemClickManipulationCallback<NameData> {
                override fun onItemClicked(data: NameData) {

                    val resultIntent = Intent()

                    val studentData = StudentData(
                        studentStatusId = data.id,
                        studentStatusName = data.name,
                    )

                    resultIntent.putExtra(
                        KEY_STUDENT_RESULT_EXTRA,
                        studentData
                    )

                    setResult(KEY_STUDENT_RESULT_CODE, resultIntent)
                    finish()
                }
                override fun onDeleteClicked(data: NameData) {}
            })
        } else { NameManipulationAdapter(object : OnItemClickManipulationCallback<NameData> {
            override fun onItemClicked(data: NameData) {
                val intent = Intent(
                    this@StudentStatusActivity,
                    StudentStatusManipulationActivity::class.java
                ).apply {
                    putExtra(StudentStatusManipulationActivity.KEY_EXTRA_ID, data.id)
                }
                resultLauncher.launch(intent)
            }

            override fun onDeleteClicked(data: NameData) {
                showAlertDialog(
                    data.name ?: "",
                    STATUS_DELETED,
                    data.id ?: 0
                )
            }

        })}
        adapter.submitList(it)
        binding.rvStudentStatus.adapter = adapter
    }



    private fun showAlertDialog(msg: String = "", status: String, id: Int = 0) {

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
                    isAlertDialogShow = false
                    isSuccessDialogShowingOrientation = false
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
                            STATUS_DELETED -> adminViewModel.deleteStudentStatus(
                                id
                            )

                            STATUS_ERROR -> {
                                if (unauthorized) authViewModel.saveUser(
                                    null,
                                    null,
                                    null
                                ) else return@setPositiveButton
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

    private fun showLoadingMain(boolean: Boolean) {
        showLoading(binding.mainProgressBar, boolean)
        if (boolean) {
            isVisibleAllView(false)
            failedToConnect(false)
        }
    }

    private fun isVisibleAllView(boolean: Boolean) {
        binding.apply {
            if (boolean) {
                toolbar.visibility = View.VISIBLE
                toolbar2.visibility = View.VISIBLE
                if (isFromStudent) fabAdd.visibility = View.GONE else fabAdd.visibility = View.VISIBLE
                rvStudentStatus.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                toolbar2.visibility = View.GONE
                fabAdd.visibility = View.GONE
                rvStudentStatus.visibility = View.GONE
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

    private fun showMenu(v: View, menuRes: Int, sortBy: String) {
        val popup = PopupMenu(this@StudentStatusActivity, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.ascMenu -> {
                    adminViewModel.searchSortStudentStatus(
                        binding.searchView.text.toString().trim(),
                        sortBy,
                        SortDir.ASC.value
                    )
                    true
                }

                R.id.descMenu -> {
                    adminViewModel.searchSortStudentStatus(
                        binding.searchView.text.toString().trim(),
                        sortBy,
                        SortDir.DESC.value
                    )
                    true
                }

                else -> false
            }

        }
        popup.setOnDismissListener {
            binding.apply {
                chipSortBy.isChecked = false
                chipName.isChecked = false
            }
        }
        // Show the popup menu.
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
        outState.putBoolean(KEY_SUCCESS_DIALOG_SHOWING, isSuccessDialogShowingOrientation)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATUS_DELETED = "status_deleted"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"


        const val KEY_STUDENT = "key_student"
        const val KEY_STUDENT_RESULT_CODE = 11_600
        const val KEY_STUDENT_RESULT_EXTRA = "key_student_result_extra"
    }
}