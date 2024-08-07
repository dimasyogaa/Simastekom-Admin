package com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.major

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
import com.yogadimas.simastekom.adapter.student.identitasacademic.studiprogram.facultylevelmajordegree.CodeNameAdapter
import com.yogadimas.simastekom.adapter.student.identitasacademic.studiprogram.facultylevelmajordegree.CodeNameManipulationAdapter
import com.yogadimas.simastekom.databinding.ActivityStudentMajorBinding
import com.yogadimas.simastekom.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.enums.Sort
import com.yogadimas.simastekom.helper.showLoading
import com.yogadimas.simastekom.interfaces.OnItemClickCallback
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.faculty.StudentFacultyManipulationActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.level.StudentLevelActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.level.StudentLevelManipulationActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudentMajorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentMajorBinding

    private val adminViewModel: AdminViewModel by viewModels()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var isLoading = false
    private var isAlertDialogShow = false
    private var isFromStudyProgram = false

    private var dialog: AlertDialog? = null

    private var isSuccessDialogShowingOrientation = false

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == StudentMajorManipulationActivity.KEY_RESULT_CODE && result.data != null) {
            val successText =
                result.data?.getStringExtra(StudentMajorManipulationActivity.KEY_EXTRA_SUCCESS)
                    .orEmpty()
            showAlertDialog(successText, STATUS_SUCCESS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentMajorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFromStudyProgram = intent.getBooleanExtra(KEY_STUDY_PROGRAM, false)

        if (savedInstanceState != null) {
            isSuccessDialogShowingOrientation =
                savedInstanceState.getBoolean(KEY_SUCCESS_DIALOG_SHOWING)
            if (isSuccessDialogShowingOrientation) {
                showAlertDialog(status = STATUS_SUCCESS)
            }
        }

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            toolbar.menu.findItem(R.id.refreshMenu).setOnMenuItemClickListener {
                getAdminAndMajors()
                true
            }

            searchView.setupWithSearchBar(searchBar)

            searchView
                .editText
                .setOnEditorActionListener { _, _, _ ->

                    searchBar.setText(searchView.text)
                    adminViewModel.searchSortMajor(searchView.text.toString().trim(), null, null)

                    searchView.hide()

                    false
                }


            chipGroup.layoutDirection = View.LAYOUT_DIRECTION_LOCALE

            listOf(chipSortBy, chipCode, chipName).forEach { chipId ->
                chipId.setOnCheckedChangeListener { chip, isChecked ->
                    if (isChecked) {
                        if (chipId == chipSortBy) {
                            showMenu(
                                chip, R.menu.top_appbar_sort_by_menu, "created_at"
                            )
                        } else {
                            showMenu(
                                chip, R.menu.top_appbar_sort_asc_desc_menu, when (chipId) {
                                    chipCode -> "kode"
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
                        this@StudentMajorActivity,
                        StudentMajorManipulationActivity::class.java
                    )
                )
            }

            val layoutManager = LinearLayoutManager(this@StudentMajorActivity)
            rvMajor.layoutManager = layoutManager

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdminAndMajors() }

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
        getAdminAndMajors()
    }

    private fun getAdminAndMajors() {
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
                adminViewModel.getAllMajors()
            }
        }

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.identityAcademicList.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {


                if (isLoading) {
                    isVisibleAllView(false)
                } else {
                    isVisibleAllView(true)
                }
                failedToConnect(false)

                setMajorData(it)


            }
        }


        adminViewModel.identityAcademic.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {



                if (it.isDeleted) {
                    val success = getString(R.string.text_success)
                    val major = getString(R.string.title_major)
                    showAlertDialog(
                        getString(R.string.text_alert_delete, success, major),
                        STATUS_SUCCESS
                    )
                    lifecycleScope.launch {
                        delay(300)
                        adminViewModel.getAllMajors()
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

    private fun setMajorData(it: List<IdentityAcademicData>) {
        val adapter = if (isFromStudyProgram) {
            CodeNameAdapter(object : OnItemClickCallback<IdentityAcademicData> {
                override fun onItemClicked(data: IdentityAcademicData) {
                    val resultIntent = Intent()

                    resultIntent.putExtra(
                        KEY_STUDY_PROGRAM_RESULT_EXTRA,
                        data
                    )

                    setResult(KEY_STUDY_PROGRAM_RESULT_CODE, resultIntent)
                    finish()
                }

                override fun onDeleteClicked(data: IdentityAcademicData) {}

            })
        } else {
            CodeNameManipulationAdapter(object : OnItemClickCallback<IdentityAcademicData> {
                override fun onItemClicked(data: IdentityAcademicData) {
                    val intent = Intent(
                        this@StudentMajorActivity,
                        StudentMajorManipulationActivity::class.java
                    ).apply {
                        putExtra(StudentMajorManipulationActivity.KEY_EXTRA_ID, data.id)
                    }
                    resultLauncher.launch(intent)
                }

                override fun onDeleteClicked(data: IdentityAcademicData) {
                    showAlertDialog(
                        getString(R.string.format_code_name, data.code, data.name),
                        STATUS_DELETED,
                        data.id ?: 0
                    )
                }

            })
        }
        adapter.submitList(it)
        binding.rvMajor.adapter = adapter
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
                            STATUS_DELETED -> adminViewModel.deleteMajor(id)
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
                if (isFromStudyProgram) fabAdd.visibility = View.GONE else fabAdd.visibility = View.VISIBLE
                rvMajor.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                toolbar2.visibility = View.GONE
                fabAdd.visibility = View.GONE
                rvMajor.visibility = View.GONE
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
        val popup = PopupMenu(this@StudentMajorActivity, v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.ascMenu -> {
                    adminViewModel.searchSortMajor(
                        binding.searchView.text.toString().trim(),
                        sortBy,
                        Sort.ASC.value
                    )
                    true
                }

                R.id.descMenu -> {
                    adminViewModel.searchSortMajor(
                        binding.searchView.text.toString().trim(),
                        sortBy,
                        Sort.DESC.value
                    )
                    true
                }

                else -> false
            }

        }
        popup.setOnDismissListener {
            binding.apply {
                chipSortBy.isChecked = false
                chipCode.isChecked = false
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

        const val KEY_STUDY_PROGRAM = "key_study_program"
        const val KEY_STUDY_PROGRAM_RESULT_CODE = 10_300
        const val KEY_STUDY_PROGRAM_RESULT_EXTRA = "key_study_program_result_extra"
    }
}