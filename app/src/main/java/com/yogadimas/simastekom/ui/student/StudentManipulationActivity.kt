package com.yogadimas.simastekom.ui.student

import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.getParcelableCompat
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnOptionDialogListenerInterface
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityStudentManipulationBinding
import com.yogadimas.simastekom.databinding.LayoutHandleDataConnectionBinding
import com.yogadimas.simastekom.databinding.LayoutStudentManipulationTextInputs1Binding
import com.yogadimas.simastekom.databinding.LayoutStudentManipulationTextInputs2Binding
import com.yogadimas.simastekom.model.ErrorData
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.ui.dialog.GenderDialogFragment
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity
import com.yogadimas.simastekom.ui.student.identity.academic.campus.CampusActivity
import com.yogadimas.simastekom.ui.student.identity.academic.classsession.ClassSessionActivity
import com.yogadimas.simastekom.ui.student.identity.academic.lecturemethod.LectureMethodActivity
import com.yogadimas.simastekom.ui.student.identity.academic.semester.SemesterActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.StudentStudyProgramActivity
import com.yogadimas.simastekom.ui.student.status.employment.EmploymentStatusActivity
import com.yogadimas.simastekom.ui.student.status.student.StudentStatusActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class StudentManipulationActivity : AppCompatActivity(), OnOptionDialogListenerInterface {

    private lateinit var binding: ActivityStudentManipulationBinding

    private lateinit var includeViewRefreshBinding: LayoutHandleDataConnectionBinding

    private lateinit var vStub1: ViewStub
    private lateinit var vStub2: ViewStub

    private var vStub1Binding: LayoutStudentManipulationTextInputs1Binding? = null
    private var vStub2Binding: LayoutStudentManipulationTextInputs2Binding? = null

    private val adminViewModel: AdminViewModel by viewModel()
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false
    private var isDialogShowingOrientationErrorClient401 = false
    private var isDialogShowingOrientationErrorClient400 = false

    private var isEditDeleteMode = false
    private var id: String = ""
    private var userType: String = ""
    private var minCharacterPassword: Int = 6

    private var studentData: StudentData? = StudentData()
    private var errorData: ErrorData? = ErrorData()


    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { data ->
            when (result.resultCode) {
                StudentStudyProgramActivity.KEY_STUDENT_RESULT_CODE -> handleStudentResult(
                    data,
                    StudentStudyProgramActivity.KEY_STUDENT_RESULT_EXTRA,
                    {
                        studentData?.apply {
                            studyProgramId = it.studyProgramId
                            studyProgramCode = it.studyProgramCode
                            studyProgramName = it.studyProgramName
                        }
                    },
                    vStub1Binding?.edtStudyProgram
                )

                ClassSessionActivity.KEY_STUDENT_RESULT_CODE -> handleStudentResult(
                    data,
                    ClassSessionActivity.KEY_STUDENT_RESULT_EXTRA,
                    {
                        studentData?.apply {
                            classSessionId = it.classSessionId
                            classSessionName = it.classSessionName
                        }
                    },
                    vStub2Binding?.edtClassSession
                )

                SemesterActivity.KEY_STUDENT_RESULT_CODE -> handleStudentResult(
                    data,
                    SemesterActivity.KEY_STUDENT_RESULT_EXTRA,
                    {
                        studentData?.apply {
                            semesterId = it.semesterId
                            numberSemester = it.numberSemester
                        }
                    },
                    vStub2Binding?.edtSemester
                )

                LectureMethodActivity.KEY_STUDENT_RESULT_CODE -> handleStudentResult(
                    data,
                    LectureMethodActivity.KEY_STUDENT_RESULT_EXTRA,
                    {
                        studentData?.apply {
                            lectureMethodId = it.lectureMethodId
                            lectureMethodName = it.lectureMethodName
                        }
                    },
                    vStub2Binding?.edtLectureMethod
                )

                StudentStatusActivity.KEY_STUDENT_RESULT_CODE -> handleStudentResult(
                    data,
                    StudentStatusActivity.KEY_STUDENT_RESULT_EXTRA,
                    {
                        studentData?.apply {
                            studentStatusId = it.studentStatusId
                            studentStatusName = it.studentStatusName
                        }
                    },
                    vStub2Binding?.edtStudentStatus
                )

                EmploymentStatusActivity.KEY_STUDENT_RESULT_CODE -> handleStudentResult(
                    data,
                    EmploymentStatusActivity.KEY_STUDENT_RESULT_EXTRA,
                    {
                        studentData?.apply {
                            employmentStatusId = it.employmentStatusId
                            employmentStatusName = it.employmentStatusName
                        }
                    },
                    vStub2Binding?.edtEmploymentStatus
                )

                CampusActivity.KEY_STUDENT_RESULT_CODE -> handleStudentResult(
                    data,
                    CampusActivity.KEY_STUDENT_RESULT_EXTRA,
                    {
                        studentData?.apply {
                            campusId = it.campusId
                            campusCode = it.campusCode
                            campusName = it.campusName
                        }
                    },
                    vStub2Binding?.edtCampus
                )
            }
        }
    }

    private fun handleStudentResult(
        data: Intent,
        key: String,
        updateStudentData: (StudentData) -> Unit,
        editText: TextInputEditText?
    ) {
        val studentData = getParcelableExtra<StudentData>(data, key)
        studentData?.let {
            updateStudentData(it)
            editText?.setText(
                it.studyProgramName ?: it.classSessionName ?: it.numberSemester
                ?: it.lectureMethodName ?: it.studentStatusName ?: it.employmentStatusName
                ?: it.campusName
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentManipulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vStub1 = binding.vs1
        vStub2 = binding.vs2
        includeViewRefreshBinding = binding.includeViewRefresh
        binding.toolbar.isInvisible = true

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }

        id = intent.getStringExtra(KEY_EXTRA_ID).orEmpty()
        userType = intent.getStringExtra(KEY_EXTRA_USER_TYPE).orEmpty()
        isEditDeleteMode = id.isNotEmpty() && userType.isNotEmpty()

        lifecycleScope.launch {
            if (isEditDeleteMode) editDeleteMode() else addMode()
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        studentData = savedInstanceState.getParcelableCompat(KEY_BUNDLE_STUDENT)
        isDialogShowingOrientationErrorClient401 =
            savedInstanceState.getBoolean(KEY_DIALOG_SHOWING_ERROR_401)
        isDialogShowingOrientationErrorClient400 =
            savedInstanceState.getBoolean(KEY_DIALOG_SHOWING_ERROR_400)

        if (isDialogShowingOrientationErrorClient401) {
            errorData = savedInstanceState.getParcelableCompat(KEY_BUNDLE_ERROR)
            errorData?.message?.firstOrNull()?.let {
                showAlertDialog(msg = it, status = STATUS_ERROR)
            }
        }
    }


    private suspend fun getToken(): String? {
        val user = authViewModel.getUser().asFlow().first()
        val token = user.first
        return if (token == AuthPreferences.DEFAULT_VALUE) {
            goToLogin(this@StudentManipulationActivity)
            null
        } else token
    }




    private fun addMode() = executeMode { validToken ->
        withContext(Dispatchers.Main) {
            showLoadingIndicator()
            delay(600)
            displayData(validToken, studentData ?: StudentData())
            collectStudentState(validToken)
        }

    }

    private fun editDeleteMode() = executeMode { validToken ->
        studentData?.id = id
        studentData?.userType = userType
        withContext(Dispatchers.Main) {
            adminViewModel.getStudentById(validToken, studentData?.id ?: "0")
            collectStudentState(validToken)
        }

    }


    private fun executeMode(action: suspend (String) -> Unit) = lifecycleScope.launch {
        getToken()?.let { validToken ->
            action(validToken)
        }
    }


    private suspend fun collectStudentState(token: String) {
        adminViewModel.studentState.collect { state ->
            when (state) {
                is State.Loading -> showLoadingIndicator()
                is State.Success -> displayData(token, state.data)
                is State.ErrorClient -> showErrorClient(state.error)
                is State.ErrorServer -> showErrorServer(state.error)
            }
        }
    }

    private fun showLoadingIndicator() {
        showLoadingMain(true)
        failedToConnect(display = false)
    }

    private fun displayData(token: String, data: StudentData) = lifecycleScope.launch {

        showLoadingMain(false)


        if (data.isAdded || data.isUpdated || data.isDeleted) {
            val success = getString(R.string.text_success)
            val label = getString(R.string.title_student)
            val resultIntent = Intent()

            val msg = when {
                data.isAdded -> R.string.text_alert_add
                data.isUpdated -> R.string.text_alert_change
                else -> R.string.text_alert_delete
            }

            resultIntent.putExtra(
                KEY_EXTRA_SUCCESS,
                getString(msg, success, label)
            )

            setResult(KEY_RESULT_CODE, resultIntent)
            finish()
        } else {

            if (isEditDeleteMode) {
                showToolbarEditDeleteMode()
            } else {
                showToolbarAddMode()
            }


            val hasErrorClient =
                isDialogShowingOrientationErrorClient401 || isDialogShowingOrientationErrorClient400

            if (!hasErrorClient) {
                studentData?.let {
                    it.apply {
                        fullName = data.fullName
                        gender = data.gender
                        studentIdNumber = data.studentIdNumber
                        studyProgramId = data.studyProgramId
                        studyProgramName = data.studyProgramName
                        batch = data.batch
                        classSessionId = data.classSessionId
                        classSessionName = data.classSessionName
                        semesterId = data.semesterId
                        numberSemester = data.numberSemester
                        lectureMethodId = data.lectureMethodId
                        lectureMethodName = data.lectureMethodName
                        studentStatusId = data.studentStatusId
                        studentStatusName = data.studentStatusName
                        employmentStatusId = data.employmentStatusId
                        employmentStatusName = data.employmentStatusName
                        campusId = data.campusId
                        campusName = data.campusName

                    }
                }
            }

            if (vStub1.parent != null) {
                vStub1Binding = LayoutStudentManipulationTextInputs1Binding.bind(vStub1.inflate())
                studentData?.let { setupFirstInputLayout(vStub1Binding!!, it) }
            } else {
                studentData?.let { vStub1Binding?.let { vs -> setupFirstInputLayout(vs, it) } }
            }

            delay(200)

            if (vStub2.parent != null) {
                vStub2Binding = LayoutStudentManipulationTextInputs2Binding.bind(vStub2.inflate())
                studentData?.let { setupSecondInputLayout(vStub2Binding!!, it, token) }
            } else {
                studentData?.let {
                    vStub2Binding?.let { vs ->
                        setupSecondInputLayout(
                            vs,
                            it,
                            token
                        )
                    }
                }
            }

            if (vStub1Binding?.root?.isVisible != true && vStub2Binding?.root?.isVisible != true) {
                setVisibilityViewStub(true)
            }

            checkButtonIsEnabled()

        }


    }

    private suspend fun showErrorClient(error: Errors) {

        var message = error.errors?.message?.first() ?: error.errors?.password?.first() ?: ""


        showLoadingMain(false)
        errorData?.message = listOf(message)


        if (message.contains(getString(R.string.text_const_unauthorized))) {
            isDialogShowingOrientationErrorClient401 = true
            if (isEditDeleteMode) {
                showToolbarEditDeleteMode()
            } else {
                showToolbarAddMode()
            }
            studentData?.let { data ->
                getToken()?.let { token -> displayData(token, data) }
            }
            setVisibilityViewStub(true)
        } else if (message.contains(getString(R.string.cannot_be_less_than))) {
            isDialogShowingOrientationErrorClient400 = true
            message = getString(
                R.string.min_character_field,
                getString(R.string.text_label_password),
                minCharacterPassword
            )
            setVisibilityAllView(true)
        } else {
            isDialogShowingOrientationErrorClient400 = true
            setVisibilityAllView(true)
        }


        showAlertDialog(msg = message, status = STATUS_ERROR)

        checkButtonIsEnabled()
    }

    private fun showErrorServer(errorMessage: String) {
        setVisibilityAllView(false)
        showLoadingMain(false)
        hideKeyboard()
        failedToConnect(display = true)
        Snackbar.make(
            binding.root as ViewGroup,
            errorMessage,
            Snackbar.LENGTH_SHORT
        ).show()
    }


    private fun showToolbarEditDeleteMode() {
        binding.toolbar.apply {
            isInvisible = false
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
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.deleteMenu -> {
                        studentData?.let {
                            delete(it.id, it.studentIdNumber ?: "", it.fullName ?: "")
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun showToolbarAddMode() {
        binding.toolbar.apply {
            isInvisible = false
            title = getString(R.string.text_add)
        }
    }

    private fun setupFirstInputLayout(
        vsb1: LayoutStudentManipulationTextInputs1Binding,
        data: StudentData
    ) {
        vsb1.apply {
            if (isEditDeleteMode) {

                inputLayoutPassword.isVisible = false
                edtPassword.isVisible = false
                inputLayoutConfirmPassword.isVisible = false
                edtConfirmPassword.isVisible = false
                animateViewStub(this)

                edtFullname.setText(data.fullName)
                setGender(this, data.gender)
                edtStudentIdNumber.setText(data.studentIdNumber)
                edtStudyProgram.setText(data.studyProgramName)


            } else {
                animateViewStub(this)

                val studentDataValue = studentData ?: StudentData()

                studentDataValue.let {
                    edtFullname.setText(it.fullName)
                    edtPassword.setText(it.password)
                    edtConfirmPassword.setText(it.confirmPassword)
                    setGender(this, it.gender)
                    edtStudentIdNumber.setText(it.studentIdNumber)
                    edtStudyProgram.setText(it.studyProgramName)
                }

            }

            setupTextWatchers(edtFullname) { studentData?.fullName = it }
            setupTextWatchers(edtPassword) { studentData?.password = it }
            setupTextWatchers(edtConfirmPassword) { studentData?.confirmPassword = it }
            setupTextWatchers(edtGender) { studentData?.gender = it }
            setupTextWatchers(edtStudentIdNumber) { studentData?.studentIdNumber = it }
            setupTextWatchers(edtStudyProgram) { studentData?.studyProgramName = it }


            setupGenderDialog()
            setInputLauncher(
                edtStudyProgram,
                StudentStudyProgramActivity.KEY_STUDENT,
                StudentStudyProgramActivity::class.java
            )

        }


    }

    private fun setupSecondInputLayout(
        vsb2: LayoutStudentManipulationTextInputs2Binding,
        data: StudentData,
        token: String
    ) {
        animateViewStub(vStub2Binding)
        vsb2.apply {
            if (isEditDeleteMode) {
                edtBatch.setText(data.batch)
                edtClassSession.setText(data.classSessionName)
                edtSemester.setText(data.numberSemester)
                edtLectureMethod.setText(data.lectureMethodName)
                edtStudentStatus.setText(data.studentStatusName)
                edtEmploymentStatus.setText(data.employmentStatusName)
                edtCampus.setText(data.campusName)


                div1.isVisible = true
                layoutNavigation.isVisible = true
                fun sendDataParcelable() {
                    val identityPersonalDataParcelable = IdentityPersonalData(
                        userId = data.id,
                        userType = data.userType,
                        isFromAdminStudent = true
                    )
                    startActivity(
                        Intent(
                            this@StudentManipulationActivity,
                            IdentityPersonalEditActivity::class.java
                        ).apply {
                            putExtra(
                                IdentityPersonalEditActivity.KEY_ADMIN_STUDENT,
                                identityPersonalDataParcelable
                            )
                        })
                }
                btnProfilePicture.setOnClickListener {sendDataParcelable()}
                btnIdentityPersonal.setOnClickListener {sendDataParcelable()}
                btnIdentityParent.setOnClickListener {sendDataParcelable()}

            } else {
                val studentDataValue = studentData ?: StudentData()

                studentDataValue.let {
                    edtBatch.setText(it.batch)
                    edtClassSession.setText(it.classSessionName)
                    edtSemester.setText(it.numberSemester)
                    edtLectureMethod.setText(it.lectureMethodName)
                    edtStudentStatus.setText(data.studentStatusName)
                    edtEmploymentStatus.setText(it.employmentStatusName)
                    edtCampus.setText(it.campusName)
                }

                div1.isVisible = false
                layoutNavigation.isVisible = false


            }

            setupTextWatchers(edtBatch) { studentData?.batch = it }
            setupTextWatchers(edtClassSession) { studentData?.classSessionName = it }
            setupTextWatchers(edtSemester) { studentData?.numberSemester = it }
            setupTextWatchers(edtLectureMethod) { studentData?.lectureMethodName = it }
            setupTextWatchers(edtStudentStatus) { studentData?.studentStatusName = it }
            setupTextWatchers(edtEmploymentStatus) { studentData?.employmentStatusName = it }
            setupTextWatchers(edtCampus) { studentData?.campusName = it }

            setInputLauncher(
                edtClassSession,
                ClassSessionActivity.KEY_STUDENT,
                ClassSessionActivity::class.java
            )
            setInputLauncher(
                edtSemester,
                SemesterActivity.KEY_STUDENT,
                SemesterActivity::class.java
            )
            setInputLauncher(
                edtLectureMethod,
                LectureMethodActivity.KEY_STUDENT,
                LectureMethodActivity::class.java
            )
            setInputLauncher(
                edtStudentStatus,
                StudentStatusActivity.KEY_STUDENT,
                StudentStatusActivity::class.java
            )
            setInputLauncher(
                edtEmploymentStatus,
                EmploymentStatusActivity.KEY_STUDENT,
                EmploymentStatusActivity::class.java
            )
            setInputLauncher(edtCampus, CampusActivity.KEY_STUDENT, CampusActivity::class.java)

            btnSave.setOnClickListener { save(token) }


        }
    }


    private fun setupTextWatchers(editText: TextInputEditText, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                onTextChanged(p0.toString())
                checkButtonIsEnabled()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }


    private fun LayoutStudentManipulationTextInputs1Binding.setupGenderDialog() {
        edtGender.setOnClickListener {
            hideKeyboard()
            GenderDialogFragment().show(
                supportFragmentManager,
                GenderDialogFragment::class.java.simpleName
            )
        }
    }

    private fun <T> setInputLauncher(
        textInputEditText: TextInputEditText,
        key: String,
        activityClass: Class<T>
    ) {
        textInputEditText.setOnClickListener {
            hideKeyboard()
            val intent = Intent(this@StudentManipulationActivity, activityClass).apply {
                putExtra(key, true)
            }
            resultLauncher.launch(intent)
        }
    }

    private fun save(
        token: String
    ) {
        hideKeyboard()

        vStub1Binding?.apply {
            studentData?.fullName = edtFullname.text.toString().trim()
            studentData?.gender = edtGender.text.toString().lowercase()
            studentData?.studentIdNumber = edtStudentIdNumber.text.toString().trim()
        }
        vStub2Binding?.apply {
            studentData?.batch = edtBatch.text.toString().trim()
        }
        if (isEditDeleteMode) {
            setVisibilityAllView(false)
            studentData?.let { data ->
                adminViewModel.updateStudent(token, id, data)
            }
        } else {
            studentData?.let { data ->
                if (!data.password.equals(data.confirmPassword)) {
                    binding.root.scrollY = 0
                    isDialogShowingOrientationErrorClient400 = true
                    showAlertDialog(
                        getString(
                            R.string.text_make_sure_confirm_matches_entered,
                            getString(R.string.text_label_password)
                        ),
                        STATUS_ERROR
                    )
                    vStub1Binding?.apply {
                        edtPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        edtConfirmPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    }
                } else {
                    setVisibilityAllView(false)
                    adminViewModel.addStudent(token, data)
                }
            }

        }

    }

    private fun delete(id: String?, studentIdNumber: String, name: String) {
        binding.apply {
            hideKeyboard()
            showAlertDialog(
                getString(R.string.format_string_strip_string, studentIdNumber, name),
                STATUS_DELETED,
                id ?: "0"
            )
        }
    }

    private fun Activity.hideKeyboard() {
        clearFocusFromAllInputs()
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun clearFocusFromAllInputs() {
        initializeBindings()
        vStub1Binding?.apply {
            clearFocusOnInputs(
                inputLayoutFullname.editText as TextInputEditText?,
                inputLayoutPassword.editText as TextInputEditText?,
                inputLayoutConfirmPassword.editText as TextInputEditText?,
                inputLayoutGender.editText as TextInputEditText?,
                inputLayoutStudentIdNumber.editText as TextInputEditText?,
                inputLayoutStudyProgram.editText as TextInputEditText?
            )
        }
        vStub2Binding?.apply {
            clearFocusOnInputs(
                inputLayoutBatch.editText as TextInputEditText?,
                inputLayoutClassSession.editText as TextInputEditText?,
                inputLayoutSemester.editText as TextInputEditText?,
                inputLayoutLectureMethod.editText as TextInputEditText?,
                inputLayoutStudentStatus.editText as TextInputEditText?,
                inputLayoutEmploymentStatus.editText as TextInputEditText?,
                inputLayoutCampus.editText as TextInputEditText?
            )
        }
    }

    private fun initializeBindings() {
        if (vStub1Binding == null) {
            vStub1Binding = LayoutStudentManipulationTextInputs1Binding.bind(vStub1.inflate())
        }
        if (vStub2Binding == null) {
            vStub2Binding = LayoutStudentManipulationTextInputs2Binding.bind(vStub2.inflate())
        }
    }

    private fun clearFocusOnInputs(vararg editTexts: TextInputEditText?) {
        editTexts.forEach { it?.clearFocus() }
    }

    private fun setVisibilityAllView(isVisible: Boolean) {
        initializeBindings()
        setVisibilityToolbar(!isVisible)
        setVisibilityViewStub(isVisible)

    }

    private fun setVisibilityViewStub(isVisible: Boolean) {
        vStub1Binding?.root?.isVisible = isVisible
        vStub2Binding?.root?.isVisible = isVisible
    }

    private fun setVisibilityToolbar(isInvisible: Boolean) {
        binding.toolbar.isInvisible = isInvisible
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
                                    setVisibilityAllView(false)
                                    adminViewModel.deleteStudent(it, id)
                                }
                            }

                            STATUS_ERROR -> {
                                if (unauthorized) {
                                    isDialogShowingOrientationErrorClient401 = false
                                    authViewModel.saveUser(
                                        null,
                                        null,
                                        null
                                    )
                                    goToLogin(this@StudentManipulationActivity)
                                } else {
                                    isDialogShowingOrientationErrorClient400 = false
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

    private fun showLoadingMain(boolean: Boolean) {
        showLoading(binding.mainProgressBar, boolean)
    }

    private fun failedToConnect(display: Boolean) {
        includeViewRefreshBinding.viewFailedConnect.apply {
            btnRefresh.setOnClickListener {
                if (isEditDeleteMode) {
                    editDeleteMode()
                } else {
                    addMode()
                }
            }
            root.isVisible = display
        }
    }

    private fun <T : ViewBinding?> animateViewStub(binding: T) {
        // Mengatur alpha dan translationY
        binding?.root?.alpha = 0f
        binding?.root?.translationY = -100f

        // Memulai animasi
        binding?.root?.animate()
            ?.alpha(1f)
            ?.translationY(0f)
            ?.setDuration(210)
            ?.start()
    }

    private fun setGender(
        viewBinding1: LayoutStudentManipulationTextInputs1Binding,
        gender: String?
    ) {
        viewBinding1.apply {
            if (gender != null) {
                when (gender.lowercase()) {
                    getString(R.string.text_man).lowercase() -> {
                        edtGender.setText(getString(R.string.text_man))
                    }

                    getString(R.string.text_woman).lowercase() -> {
                        edtGender.setText(getString(R.string.text_woman))
                    }
                }
            }
        }
    }

    private fun checkButtonIsEnabled() {
        val viewBinding1isNotEmpty = vStub1Binding?.run {
            listOf(
                edtFullname,
                edtGender,
                edtStudentIdNumber,
                edtStudyProgram
            ).all { it.text.toString().isNotEmpty() }
        } ?: false

        val viewBinding2isNotEmpty = vStub2Binding?.run {
            listOf(
                edtBatch, edtClassSession, edtSemester, edtLectureMethod,
                edtStudentStatus, edtEmploymentStatus, edtCampus
            ).all { it.text.toString().isNotEmpty() }
        } ?: false

        val isPasswordNotEmpty = vStub1Binding?.run {
            edtPassword.text.toString().isNotEmpty() && edtConfirmPassword.text.toString()
                .isNotEmpty()
        } == true

        vStub2Binding?.btnSave?.isEnabled = if (isEditDeleteMode) {
            viewBinding1isNotEmpty && viewBinding2isNotEmpty
        } else {
            viewBinding1isNotEmpty && viewBinding2isNotEmpty && isPasswordNotEmpty
        }

    }


    override fun onOptionChosen(text: String, category: String) {
        when (category) {
            GenderDialogFragment.KEY_OPTION_GENDER -> {
                vStub1Binding?.apply {
                    setGender(this, text)
                    edtGender.setText(text)
                }
            }
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
        outState.putParcelable(KEY_BUNDLE_STUDENT, studentData)
        outState.putBoolean(KEY_DIALOG_SHOWING_ERROR_401, isDialogShowingOrientationErrorClient401)
        outState.putBoolean(KEY_DIALOG_SHOWING_ERROR_400, isDialogShowingOrientationErrorClient400)
        outState.putParcelable(KEY_BUNDLE_ERROR, errorData)
        super.onSaveInstanceState(outState)
    }


    companion object {
        private const val STATUS_DELETED = "status_deleted"
        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_ID = "key_extra_id"
        const val KEY_EXTRA_USER_TYPE = "key_extra_user_type"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 200

        const val KEY_BUNDLE_STUDENT = "key_bundle_student"

        private const val KEY_DIALOG_SHOWING_ERROR_401 = "key_dialog_showing_error_401"
        private const val KEY_DIALOG_SHOWING_ERROR_400 = "key_dialog_showing_error_400"
        private const val KEY_BUNDLE_ERROR = "key_bundle_error"
    }
}