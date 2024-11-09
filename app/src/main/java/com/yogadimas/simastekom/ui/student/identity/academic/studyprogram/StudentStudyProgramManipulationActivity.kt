package com.yogadimas.simastekom.ui.student.identity.academic.studyprogram

import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityStudentStudyProgramManipulationBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.degree.StudentDegreeActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.faculty.StudentFacultyActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.level.StudentLevelActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.major.StudentMajorActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentStudyProgramManipulationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentStudyProgramManipulationBinding

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var isLoading = false
    private var isAlertDialogShow = false

    private var dialog: AlertDialog? = null

    private var isEditDeleteView = false
    private var id: Int = 0

    private var identityAcademicData: IdentityAcademicData? = IdentityAcademicData()

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        binding.apply {
            if (result.resultCode == StudentFacultyActivity.KEY_STUDY_PROGRAM_RESULT_CODE && result.data != null) {
                val faculty =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(
                            StudentFacultyActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA,
                            IdentityAcademicData::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        result.data?.getParcelableExtra(StudentFacultyActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA)
                    }
                identityAcademicData?.apply {
                    facultyId = faculty?.id
                    facultyName = faculty?.name
                }
                edtFaculty.setText(identityAcademicData?.facultyName)
            } else if (result.resultCode == StudentLevelActivity.KEY_STUDY_PROGRAM_RESULT_CODE && result.data != null) {
                val level =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(
                            StudentLevelActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA,
                            IdentityAcademicData::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        result.data?.getParcelableExtra(StudentLevelActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA)
                    }
                identityAcademicData?.apply {
                    levelId = level?.id
                    levelName = level?.name
                }
                edtLevel.setText(identityAcademicData?.levelName)
            } else if (result.resultCode == StudentMajorActivity.KEY_STUDY_PROGRAM_RESULT_CODE && result.data != null) {
                val major =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(
                            StudentMajorActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA,
                            IdentityAcademicData::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        result.data?.getParcelableExtra(StudentMajorActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA)
                    }
                identityAcademicData?.apply {
                    majorId = major?.id
                    majorName = major?.name
                }
                edtMajor.setText(identityAcademicData?.majorName)
            } else if (result.resultCode == StudentDegreeActivity.KEY_STUDY_PROGRAM_RESULT_CODE && result.data != null) {
                val degree =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra(
                            StudentDegreeActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA,
                            IdentityAcademicData::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        result.data?.getParcelableExtra(StudentDegreeActivity.KEY_STUDY_PROGRAM_RESULT_EXTRA)
                    }
                identityAcademicData?.apply {
                    degreeId = degree?.id
                    degreeName = degree?.name
                }
                edtDegree.setText(identityAcademicData?.degreeName)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentStudyProgramManipulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getIntExtra(KEY_EXTRA_ID, 0)

        if (savedInstanceState != null) {
            identityAcademicData = if (Build.VERSION.SDK_INT >= 33) {
                savedInstanceState.getParcelable(
                    KEY_BUNDLE_STUDY_PROGRAM,
                    IdentityAcademicData::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                savedInstanceState.getParcelable(KEY_BUNDLE_STUDY_PROGRAM)
            }
        }

        isEditDeleteView = id != 0

        if (isEditDeleteView) {
            editDeleteMode()
        } else {
            addMode()
        }

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            edtCode.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtFaculty.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtLevel.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtMajor.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtDegree.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            edtFaculty.setOnClickListener {
                hideKeyboard()
                val intent = Intent(
                    this@StudentStudyProgramManipulationActivity,
                    StudentFacultyActivity::class.java
                ).apply {
                    putExtra(StudentFacultyActivity.KEY_STUDY_PROGRAM, true)
                }
                resultLauncher.launch(intent)
            }
            edtLevel.setOnClickListener {
                hideKeyboard()
                val intent = Intent(
                    this@StudentStudyProgramManipulationActivity,
                    StudentLevelActivity::class.java
                ).apply {
                    putExtra(StudentLevelActivity.KEY_STUDY_PROGRAM, true)
                }
                resultLauncher.launch(intent)
            }
            edtMajor.setOnClickListener {
                hideKeyboard()
                val intent = Intent(
                    this@StudentStudyProgramManipulationActivity,
                    StudentMajorActivity::class.java
                ).apply {
                    putExtra(StudentMajorActivity.KEY_STUDY_PROGRAM, true)
                }
                resultLauncher.launch(intent)
            }
            edtDegree.setOnClickListener {
                hideKeyboard()
                val intent = Intent(
                    this@StudentStudyProgramManipulationActivity,
                    StudentDegreeActivity::class.java
                ).apply {
                    putExtra(StudentDegreeActivity.KEY_STUDY_PROGRAM, true)
                }
                resultLauncher.launch(intent)
            }

            btnSave.setOnClickListener { save() }

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdminAndStudyProgram() }
        }

        getAdminAndStudyProgram()

    }

    private fun addMode() {
        binding.apply {
            toolbar.apply {
                title = getString(R.string.text_add)
                menu.clear()
            }
        }
    }

    private fun editDeleteMode() {
        binding.apply {
            toolbar.apply {
                title = getString(R.string.text_change_or_delete)
                menu.clear()
                menuInflater.inflate(R.menu.top_appbar_delete_menu, menu)
                val icon = menu.findItem(R.id.deleteMenu)?.icon?.mutate()
                val color = ContextCompat.getColor(context, R.color.md_theme_error)
                if (icon != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        icon.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
                    } else {
                        @Suppress("DEPRECATION")
                        icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                    }
                }


            }
        }
    }

    private fun getAdminAndStudyProgram() {
        buttonIsEnabled()

        authViewModel.getUser().observe(this) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                if (isEditDeleteView) {
                    adminViewModel.getStudyProgramById(id)
                }
            }
        }

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.identityAcademic.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    isVisibleAllView(false)
                } else {
                    isVisibleAllView(true)
                }
                failedToConnect(false)

                binding.apply {
                    edtCode.setText(it.code)
                    edtFaculty.setText(it.facultyName)
                    edtLevel.setText(it.levelName)
                    edtMajor.setText(it.majorName)
                    edtDegree.setText(it.degreeName)
                    identityAcademicData?.apply {
                        code = it.code
                        facultyId = it.facultyId
                        facultyName = it.facultyName
                        levelId = it.levelId
                        levelName = it.levelName
                        majorId = it.majorId
                        majorName = it.majorName
                        degreeId = it.degreeId
                        degreeName = it.degreeName
                    }
                    val studyProgram =
                        "${edtCode.text} | ${it.facultyName} - ${it.levelName} ${it.majorName} (${it.degreeName})"
                    toolbar.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.deleteMenu -> {
                                delete(it.id, studyProgram)
                                true
                            }

                            else -> false
                        }
                    }
                }

                if (it.isAdded || it.isUpdated || it.isDeleted) {
                    val success = getString(R.string.text_success)
                    val studyProgram = getString(R.string.title_study_program)
                    val resultIntent = Intent()

                    val msg = when {
                        it.isAdded -> R.string.text_alert_add_format
                        it.isUpdated -> R.string.text_alert_update_format
                        else -> R.string.text_alert_delete_format
                    }

                    resultIntent.putExtra(
                        KEY_EXTRA_SUCCESS,
                        getString(msg, success, studyProgram)
                    )

                    setResult(KEY_RESULT_CODE, resultIntent)
                    finish()
                }


            }

        }

        adminViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val errors = data.errors
                    var listMessage: List<String> = listOf()
                    when {
                        errors.message != null -> {
                            listMessage = errors.message
                        }
                    }
                    isVisibleAllView(true)
                    failedToConnect(false)
                    showAlertDialog(listMessage[0], STATUS_ERROR)
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                hideKeyboard()
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

    private fun save() {
        binding.apply {
            hideKeyboard()
            if (isEditDeleteView) {
                adminViewModel.updateStudyProgram(
                    id,
                    IdentityAcademicData(
                        code = edtCode.text.toString().trim(),
                        facultyId = identityAcademicData?.facultyId,
                        levelId = identityAcademicData?.levelId,
                        majorId = identityAcademicData?.majorId,
                        degreeId = identityAcademicData?.degreeId
                    )
                )
            } else {
                adminViewModel.addStudyProgram(
                    IdentityAcademicData(
                        id,
                        code = edtCode.text.toString().trim(),
                        facultyId = identityAcademicData?.facultyId,
                        levelId = identityAcademicData?.levelId,
                        majorId = identityAcademicData?.majorId,
                        degreeId = identityAcademicData?.degreeId
                    )
                )
            }
        }
    }

    private fun delete(id: Int?, msg: String) {
        binding.apply {
            hideKeyboard()
            showAlertDialog(
                msg,
                STATUS_DELETED,
                id ?: 0
            )
        }
    }

    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            binding.apply {
                inputLayoutCode.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
    }

    private fun buttonIsEnabled() {
        binding.apply {
            btnSave.isEnabled = edtCode.text.toString().isNotEmpty() &&
                    edtFaculty.text.toString().isNotEmpty() &&
                    edtLevel.text.toString().isNotEmpty() &&
                    edtMajor.text.toString().isNotEmpty() &&
                    edtDegree.text.toString().isNotEmpty()
        }
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

            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error, "")
                    message = msg
                }

            }
        }

        if (dialog == null) {

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
                            STATUS_DELETED -> adminViewModel.deleteStudyProgram(id)
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
                inputLayoutCode.visibility = View.VISIBLE
                edtCode.visibility = View.VISIBLE
                inputLayoutFaculty.visibility = View.VISIBLE
                edtFaculty.visibility = View.VISIBLE
                inputLayoutLevel.visibility = View.VISIBLE
                edtLevel.visibility = View.VISIBLE
                inputLayoutMajor.visibility = View.VISIBLE
                edtMajor.visibility = View.VISIBLE
                inputLayoutDegree.visibility = View.VISIBLE
                edtDegree.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                inputLayoutCode.visibility = View.GONE
                edtCode.visibility = View.GONE
                inputLayoutFaculty.visibility = View.GONE
                edtFaculty.visibility = View.GONE
                inputLayoutLevel.visibility = View.GONE
                edtLevel.visibility = View.GONE
                inputLayoutMajor.visibility = View.GONE
                edtMajor.visibility = View.GONE
                inputLayoutDegree.visibility = View.GONE
                edtDegree.visibility = View.GONE
                btnSave.visibility = View.GONE
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
        outState.putParcelable(KEY_BUNDLE_STUDY_PROGRAM, identityAcademicData)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATUS_DELETED = "status_deleted"
        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_ID = "key_extra_id"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 200

        const val KEY_BUNDLE_STUDY_PROGRAM = "key_bundle_study_program"
    }
}