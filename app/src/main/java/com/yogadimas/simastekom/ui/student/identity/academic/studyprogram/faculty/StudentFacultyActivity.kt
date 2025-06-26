package com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.faculty

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.adapter.student.identityacademic.studiprogram.facultylevelmajordegree.CodeNameAdapter
import com.yogadimas.simastekom.adapter.student.identityacademic.studiprogram.facultylevelmajordegree.CodeNameManipulationAdapter
import com.yogadimas.simastekom.databinding.ActivityStudentFacultyBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnItemClickManipulationCallback
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentFacultyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentFacultyBinding

    private val adminViewModel: AdminViewModel by viewModel()

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
        if (result.resultCode == StudentFacultyManipulationActivity.KEY_RESULT_CODE && result.data != null) {
            val successText =
                result.data?.getStringExtra(StudentFacultyManipulationActivity.KEY_EXTRA_SUCCESS)
                    .orEmpty()
            showAlertDialog(successText, STATUS_SUCCESS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentFacultyBinding.inflate(layoutInflater)
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
            appBarLayout.isVisible = false

            toolbar.setNavigationOnClickListener { finish() }

            toolbar.menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener {
                getAdminAndFaculties()
                true
            }

            fabAdd.setOnClickListener {
                resultLauncher.launch(
                    Intent(
                        this@StudentFacultyActivity,
                        StudentFacultyManipulationActivity::class.java
                    )
                )
            }

            val layoutManager = LinearLayoutManager(this@StudentFacultyActivity)
            rvFaculty.layoutManager = layoutManager

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdminAndFaculties() }

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onStart() {
        super.onStart()
        getAdminAndFaculties()
    }

    private fun getAdminAndFaculties() {
        authViewModel.getUser().observe(this) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                adminViewModel.getAllLFaculties()
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

                setFacultyData(it)


            }
        }


        adminViewModel.identityAcademic.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {


                if (it.isDeleted) {
                    val success = getString(R.string.text_success)
                    val faculty = getString(R.string.text_faculty)
                    showAlertDialog(
                        getString(R.string.text_alert_delete_format, success, faculty),
                        STATUS_SUCCESS
                    )
                    lifecycleScope.launch {
                        delay(300)
                        adminViewModel.getAllLFaculties()
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

    private fun setFacultyData(it: List<IdentityAcademicData>) {
        val adapter = if (isFromStudyProgram) {
            CodeNameAdapter(object : OnItemClickManipulationCallback<IdentityAcademicData> {
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
            CodeNameManipulationAdapter(object : OnItemClickManipulationCallback<IdentityAcademicData> {
                override fun onItemClicked(data: IdentityAcademicData) {
                    val intent = Intent(
                        this@StudentFacultyActivity,
                        StudentFacultyManipulationActivity::class.java
                    ).apply {
                        putExtra(StudentFacultyManipulationActivity.KEY_EXTRA_ID, data.id)
                    }
                    resultLauncher.launch(intent)
                }

                override fun onDeleteClicked(data: IdentityAcademicData) {
                    showAlertDialog(
                        getString(R.string.text_string_strip_string_format, data.code, data.name),
                        STATUS_DELETED,
                        data.id ?: 0
                    )
                }

            })
        }

        adapter.submitList(it)
        binding.rvFaculty.adapter = adapter
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
                message = getString(R.string.text_question_do_you_want_to_delete_format, msg)
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
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error_format, "")
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
                            STATUS_DELETED -> adminViewModel.deleteFaculty(id)
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
            appBarLayout.isVisible = boolean
            if (boolean) {
                toolbar.visibility = View.VISIBLE
                if (isFromStudyProgram) fabAdd.visibility = View.GONE else fabAdd.visibility = View.VISIBLE
                rvFaculty.visibility = View.VISIBLE
            } else {
                toolbar.visibility = View.INVISIBLE
                fabAdd.visibility = View.GONE
                rvFaculty.visibility = View.GONE
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
        const val KEY_STUDY_PROGRAM_RESULT_CODE = 10_100
        const val KEY_STUDY_PROGRAM_RESULT_EXTRA = "key_study_program_result_extra"
    }
}