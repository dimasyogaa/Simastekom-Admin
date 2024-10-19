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
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.common.custom.CustomViewStub
import com.yogadimas.simastekom.databinding.ActivityStudentManipulationViewstubBinding
import com.yogadimas.simastekom.databinding.LayoutHandleDataConnectionBinding
import com.yogadimas.simastekom.databinding.LayoutStudentManipulationTextInputs1Binding
import com.yogadimas.simastekom.databinding.LayoutStudentManipulationTextInputs2Binding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.interfaces.OnOptionDialogListenerInterface
import com.yogadimas.simastekom.model.ErrorData
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.ui.dialog.GenderDialogFragment
import com.yogadimas.simastekom.ui.login.LoginActivity
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.coroutines.resume


class StudentManipulationViewStubActivity : AppCompatActivity(), OnOptionDialogListenerInterface {
    private lateinit var binding: ActivityStudentManipulationViewstubBinding

    private lateinit var viewBinding1: LayoutStudentManipulationTextInputs1Binding
    private lateinit var viewBinding2: LayoutStudentManipulationTextInputs2Binding

    private var viewParent1: ViewParent? = null
    private var viewParent2: ViewParent? = null

    private var inflatedViewBinding1: View? = null
    private var inflatedViewBinding2: View? = null

    private lateinit var viewBindingHandleDataConnection: LayoutHandleDataConnectionBinding






    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var isLoading = false
    private var dialog: AlertDialog? = null
    private var isAlertDialogShow = false
    private var isDialogShowingOrientationError = false

    private var isEditDeleteMode = false
    private var id: String = "0"

    private var studentData: StudentData? = StudentData()
    private var errorData: ErrorData? = ErrorData()

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == StudentStudyProgramActivity.KEY_STUDENT_RESULT_CODE && result.data != null) {
            val studyProgram =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        StudentStudyProgramActivity.KEY_STUDENT_RESULT_EXTRA,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(StudentStudyProgramActivity.KEY_STUDENT_RESULT_EXTRA)
                }

            studentData?.apply {
                studyProgramId = studyProgram?.studyProgramId
                studyProgramCode = studyProgram?.studyProgramCode
                studyProgramName = studyProgram?.studyProgramName
            }
            viewBinding1.edtStudyProgram.setText(studentData?.studyProgramName)
        } else if (result.resultCode == ClassSessionActivity.KEY_STUDENT_RESULT_CODE && result.data != null) {
            val classSession =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        ClassSessionActivity.KEY_STUDENT_RESULT_EXTRA,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(ClassSessionActivity.KEY_STUDENT_RESULT_EXTRA)
                }
            studentData?.apply {
                classSessionId = classSession?.classSessionId
                classSessionName = classSession?.classSessionName
            }
            viewBinding2.edtClassSession.setText(studentData?.classSessionName)
        } else if (result.resultCode == SemesterActivity.KEY_STUDENT_RESULT_CODE && result.data != null) {
            val semester =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        SemesterActivity.KEY_STUDENT_RESULT_EXTRA,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(SemesterActivity.KEY_STUDENT_RESULT_EXTRA)
                }
            studentData?.apply {
                semesterId = semester?.semesterId
                numberSemester = semester?.numberSemester
            }
            viewBinding2.edtSemester.setText(studentData?.numberSemester)
        } else if (result.resultCode == LectureMethodActivity.KEY_STUDENT_RESULT_CODE && result.data != null) {
            val lectureMethod =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        LectureMethodActivity.KEY_STUDENT_RESULT_EXTRA,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(LectureMethodActivity.KEY_STUDENT_RESULT_EXTRA)
                }
            studentData?.apply {
                lectureMethodId = lectureMethod?.lectureMethodId
                lectureMethodName = lectureMethod?.lectureMethodName
            }
            viewBinding2.edtLectureMethod.setText(studentData?.lectureMethodName)
        } else if (result.resultCode == StudentStatusActivity.KEY_STUDENT_RESULT_CODE && result.data != null) {
            val studentStatus =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        StudentStatusActivity.KEY_STUDENT_RESULT_EXTRA,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(StudentStatusActivity.KEY_STUDENT_RESULT_EXTRA)
                }
            studentData?.apply {
                studentStatusId = studentStatus?.studentStatusId
                studentStatusName = studentStatus?.studentStatusName
            }
            viewBinding2.edtStudentStatus.setText(studentData?.studentStatusName)
        } else if (result.resultCode == EmploymentStatusActivity.KEY_STUDENT_RESULT_CODE && result.data != null) {
            val employmentStatus =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        EmploymentStatusActivity.KEY_STUDENT_RESULT_EXTRA,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(EmploymentStatusActivity.KEY_STUDENT_RESULT_EXTRA)
                }
            studentData?.apply {
                employmentStatusId = employmentStatus?.employmentStatusId
                employmentStatusName = employmentStatus?.employmentStatusName
            }
            viewBinding2.edtEmploymentStatus.setText(studentData?.employmentStatusName)
        } else if (result.resultCode == CampusActivity.KEY_STUDENT_RESULT_CODE && result.data != null) {
            val campus =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        CampusActivity.KEY_STUDENT_RESULT_EXTRA,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(CampusActivity.KEY_STUDENT_RESULT_EXTRA)
                }
            studentData?.apply {
                campusId = campus?.campusId
                campusCode = campus?.campusCode
                campusName = campus?.campusName
            }
            viewBinding2.edtCampus.setText(studentData?.campusName)
        }


    }
    /*
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityStudentManipulationBinding.inflate(layoutInflater)
            setContentView(binding.root)


            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        if (savedInstanceState != null) {
                            studentData = if (Build.VERSION.SDK_INT >= 33) {
                                savedInstanceState.getParcelable(
                                    KEY_BUNDLE_STUDENT,
                                    StudentData::class.java
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                savedInstanceState.getParcelable(KEY_BUNDLE_STUDENT)
                            }
                            isDialogShowingOrientationError =
                                savedInstanceState.getBoolean(KEY_DIALOG_SHOWING_ERROR)
                            if (isDialogShowingOrientationError) {

                                errorData = if (Build.VERSION.SDK_INT >= 33) {
                                    savedInstanceState.getParcelable(
                                        KEY_BUNDLE_ERROR,
                                        ErrorData::class.java
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    savedInstanceState.getParcelable(KEY_BUNDLE_ERROR)
                                }
                                errorData?.message?.let {
                                    showAlertDialog(
                                        msg = it.first(),
                                        status = STATUS_ERROR
                                    )
                                }
                            }

                        }
                    }

                }

                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    launch {
                        id = intent.getStringExtra(KEY_EXTRA_ID) ?: "0"
                        isEditDeleteMode = id != "0"
                        if (isEditDeleteMode) {
                            editDeleteMode()
                        } else {
                            appBarAddMode()
                            mainContentAddMode()
                        }

                        binding.toolbar.setNavigationOnClickListener { finish() }
                    }
                }


    //            if (savedInstanceState != null) {
    //                studentData = if (Build.VERSION.SDK_INT >= 33) {
    //                    savedInstanceState.getParcelable(
    //                        KEY_BUNDLE_STUDENT,
    //                        StudentData::class.java
    //                    )
    //                } else {
    //                    @Suppress("DEPRECATION")
    //                    savedInstanceState.getParcelable(KEY_BUNDLE_STUDENT)
    //                }
    //                isDialogShowingOrientationError =
    //                    savedInstanceState.getBoolean(KEY_DIALOG_SHOWING_ERROR)
    //                if (isDialogShowingOrientationError) {
    //
    //                    errorData = if (Build.VERSION.SDK_INT >= 33) {
    //                        savedInstanceState.getParcelable(
    //                            KEY_BUNDLE_ERROR,
    //                            ErrorData::class.java
    //                        )
    //                    } else {
    //                        @Suppress("DEPRECATION")
    //                        savedInstanceState.getParcelable(KEY_BUNDLE_ERROR)
    //                    }
    //                    errorData?.message?.let {
    //                        showAlertDialog(
    //                            msg = it.first(),
    //                            status = STATUS_ERROR
    //                        )
    //                    }
    //                }
    //
    //            }
    //
    //            id = intent.getStringExtra(KEY_EXTRA_ID) ?: "0"
    //            isEditDeleteMode = id != "0"
    //            if (isEditDeleteMode) {
    //                editDeleteMode()
    //            } else {
    //                appBarAddMode()
    //                mainContentAddMode()
    //            }
    //
    //            binding.toolbar.setNavigationOnClickListener { finish() }




            }

        }*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentManipulationViewstubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            if (savedInstanceState != null) {
                studentData = if (Build.VERSION.SDK_INT >= 33) {
                    savedInstanceState.getParcelable(
                        KEY_BUNDLE_STUDENT,
                        StudentData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    savedInstanceState.getParcelable(KEY_BUNDLE_STUDENT)
                }

                isDialogShowingOrientationError =
                    savedInstanceState.getBoolean(KEY_DIALOG_SHOWING_ERROR)
                if (isDialogShowingOrientationError) {

                    errorData = if (Build.VERSION.SDK_INT >= 33) {
                        savedInstanceState.getParcelable(
                            KEY_BUNDLE_ERROR,
                            ErrorData::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        savedInstanceState.getParcelable(KEY_BUNDLE_ERROR)
                    }

                    errorData?.message?.let {
                        showAlertDialog(
                            msg = it.first(),
                            status = STATUS_ERROR
                        )
                    }
                }
            }
            id = intent.getStringExtra(KEY_EXTRA_ID) ?: "0"
            isEditDeleteMode = id != "0"
            if (isEditDeleteMode) {
                editDeleteMode()
            } else {
                appBarAddMode()
                mainContentAddMode()
            }

            binding.toolbar.setNavigationOnClickListener { finish() }


        }


    }


    private fun appBarAddMode() {
        binding.apply {
            toolbar.apply {
                title = getString(R.string.text_add)
                menu.clear()
            }
        }
    }

    private fun mainContentAddMode() = lifecycleScope.launch {
        withContext(Dispatchers.Main) {

            binding.mainProgressBar.visibility = View.VISIBLE

            delay(500)

            binding.mainProgressBar.visibility = View.INVISIBLE

            delay(10)

            // Inflate the first ViewStub immediately
            val view1 = binding.viewStubStudentManipulationTextInputs1.inflate()

            viewBinding1 = LayoutStudentManipulationTextInputs1Binding.bind(view1)

            // Set initial visibility and animation properties
            view1.alpha = 0f
            view1.translationY = -100f

            // Start animation to make the view visible
            view1.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(210)
                .start()

            delay(220)

            // Inflate the second ViewStub
            val view2 = binding.viewStubStudentManipulationTextInputs2.inflate()

            viewBinding2 = LayoutStudentManipulationTextInputs2Binding.bind(view2)

            // Set initial visibility and animation properties
            view2.alpha = 0f
            view2.translationY = -100f

            // Start animation to make the view visible
            view2.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(210)
                .start()


            viewBinding1.apply {
                edtFullname.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonIsEnabledAddMode(viewBinding1, viewBinding2)
                    }

                    override fun afterTextChanged(p0: Editable?) {}

                })
                edtPassword.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonIsEnabledAddMode(viewBinding1, viewBinding2)
                    }

                    override fun afterTextChanged(p0: Editable?) {}

                })
                edtConfirmPassword.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonIsEnabledAddMode(viewBinding1, viewBinding2)
                    }

                    override fun afterTextChanged(p0: Editable?) {}

                })
                edtGender.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonIsEnabledAddMode(viewBinding1, viewBinding2)
                    }

                    override fun afterTextChanged(p0: Editable?) {}

                })
                edtStudentIdNumber.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonIsEnabledAddMode(viewBinding1, viewBinding2)
                    }

                    override fun afterTextChanged(p0: Editable?) {}

                })
                edtStudyProgram.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonIsEnabledAddMode(viewBinding1, viewBinding2)
                    }

                    override fun afterTextChanged(p0: Editable?) {}

                })

                edtGender.setOnClickListener { v ->
                    hideKeyboard(v)
                    GenderDialogFragment().show(
                        supportFragmentManager,
                        GenderDialogFragment::class.java.simpleName
                    )
                }
                edtStudyProgram.setOnClickListener { v ->
                    hideKeyboard(v)
                    val intent = Intent(
                        this@StudentManipulationViewStubActivity,
                        StudentStudyProgramActivity::class.java
                    ).apply {
                        putExtra(StudentStudyProgramActivity.KEY_STUDENT, true)
                    }
                    resultLauncher.launch(intent)
                }
            }
            viewBinding2.apply {
                edtClassSession.setOnClickListener { v ->
                    hideKeyboard(v)
                    val intent = Intent(
                        this@StudentManipulationViewStubActivity,
                        ClassSessionActivity::class.java
                    ).apply {
                        putExtra(ClassSessionActivity.KEY_STUDENT, true)
                    }
                    resultLauncher.launch(intent)
                }
                edtSemester.setOnClickListener { v ->
                    hideKeyboard(v)
                    val intent = Intent(
                        this@StudentManipulationViewStubActivity,
                        SemesterActivity::class.java
                    ).apply {
                        putExtra(SemesterActivity.KEY_STUDENT, true)
                    }
                    resultLauncher.launch(intent)
                }
                edtLectureMethod.setOnClickListener { v ->
                    hideKeyboard(v)
                    val intent = Intent(
                        this@StudentManipulationViewStubActivity,
                        LectureMethodActivity::class.java
                    ).apply {
                        putExtra(LectureMethodActivity.KEY_STUDENT, true)
                    }
                    resultLauncher.launch(intent)
                }
                edtStudentStatus.setOnClickListener { v ->
                    hideKeyboard(v)
                    val intent = Intent(
                        this@StudentManipulationViewStubActivity,
                        StudentStatusActivity::class.java
                    ).apply {
                        putExtra(StudentStatusActivity.KEY_STUDENT, true)
                    }
                    resultLauncher.launch(intent)
                }
                edtEmploymentStatus.setOnClickListener { v ->
                    hideKeyboard(v)
                    val intent = Intent(
                        this@StudentManipulationViewStubActivity,
                        EmploymentStatusActivity::class.java
                    ).apply {
                        putExtra(EmploymentStatusActivity.KEY_STUDENT, true)
                    }
                    resultLauncher.launch(intent)
                }
                edtCampus.setOnClickListener { v ->
                    hideKeyboard(v)
                    val intent = Intent(
                        this@StudentManipulationViewStubActivity,
                        CampusActivity::class.java
                    ).apply {
                        putExtra(CampusActivity.KEY_STUDENT, true)
                    }
                    resultLauncher.launch(intent)
                }
//            btnSave.setOnClickListener { save() }

            }

            buttonIsEnabledAddMode(viewBinding1, viewBinding2)


        }
    }

    private fun editDeleteMode() = lifecycleScope.launch {
        val token = getToken()
        token?.let { validToken ->
            withContext(Dispatchers.Main) {
                adminViewModel.getStudentById(validToken, id)
                adminViewModel.studentState.collect { state ->
                    when (state) {
                        is State.Loading -> showLoadingIndicator()
                        is State.Success -> displayData(validToken, state.data)
                        is State.ErrorClient -> showErrorClient(
                            state.error,
                            validToken,
                            studentData ?: StudentData()
                        )
                        is State.ErrorServer -> showErrorServer(validToken, id, state.error)
                    }
                }
            }
        }
    }



    private suspend fun getToken(): String? = suspendCancellableCoroutine { continuation ->
        authViewModel.getUser().observe(this@StudentManipulationViewStubActivity) { user ->
            val token = user.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                continuation.cancel()
                val intent = Intent(this@StudentManipulationViewStubActivity, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                continuation.resume(token)
            }
        }
    }


    private fun showLoadingIndicator() {
        failedToConnect(display = false)
        setVisibilityToolbar(isInvisible = true)
        showLoadingMain(true)
    }

    private fun displayData(
        token: String = "",
        data: StudentData = StudentData(),
    ) = lifecycleScope.launch {

        setVisibilityToolbar(isInvisible = false)
        showLoadingMain(false)

        if (data.isUpdated || data.isDeleted) {
            val success = getString(R.string.text_success)
            val label = getString(R.string.title_student)
            val resultIntent = Intent()

            val msg = when {
                data.isUpdated -> R.string.text_alert_change
                else -> R.string.text_alert_delete
            }

            resultIntent.putExtra(
                KEY_EXTRA_SUCCESS,
                getString(msg, success, label)
            )

            setResult(KEY_RESULT_CODE, resultIntent)
            finish()
        }

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

        if (viewParent1 == null) {
            viewParent1 = binding.viewStubStudentManipulationTextInputs1.parent
        }

//        val viewStub1 = CustomViewStub(binding.viewStubStudentManipulationTextInputs1)

        val viewStub1 = CustomViewStub(binding.viewStubStudentManipulationTextInputs1, binding.linearLayout)

        if (viewParent1 != null) {
            if (!viewStub1.isInflated()) {
                inflatedViewBinding1 = viewStub1.inflate()
                viewParent1 = inflatedViewBinding1?.parent as? ViewGroup
            }
        } else {
            inflatedViewBinding1 = viewStub1.inflate()
            viewParent1 = inflatedViewBinding1?.parent as? ViewGroup
        }

        if (viewParent1 != null) {
            viewBinding1 = LayoutStudentManipulationTextInputs1Binding.bind(inflatedViewBinding1!!)

            viewBinding1.inputLayoutPassword.isVisible = false
            viewBinding1.edtPassword.isVisible = false
            viewBinding1.inputLayoutConfirmPassword.isVisible = false
            viewBinding1.edtConfirmPassword.isVisible = false

            // Set initial visibility and animation properties
            inflatedViewBinding1?.alpha = 0f
            inflatedViewBinding1?.translationY = -100f

            // Start animation to make the view visible
            inflatedViewBinding1?.animate()
                ?.alpha(1f)
                ?.translationY(0f)
                ?.setDuration(210)
                ?.start()

        }

/** */
        viewBinding1.apply {
            edtFullname.setText(data.fullName)
            setGender(this, data.gender)
            edtStudentIdNumber.setText(data.studentIdNumber)
            edtStudyProgram.setText(data.studyProgramName)

            studentData?.studyProgramId = data.studyProgramId
        }

        delay(220)

        if (viewParent2 == null) {
            viewParent2 = binding.viewStubStudentManipulationTextInputs2.parent
        }

        val viewStub2 = CustomViewStub(binding.viewStubStudentManipulationTextInputs2, binding.linearLayout)

        if (viewParent2 != null) {
            if (!viewStub2.isInflated()) {
                inflatedViewBinding2 = viewStub2.inflate()
                viewParent2 = inflatedViewBinding2?.parent as? ViewGroup
            }
        } else {
            inflatedViewBinding2 = viewStub2.inflate()
            viewParent2 = inflatedViewBinding2?.parent as? ViewGroup
        }
        if (viewParent2 != null) {
            viewBinding2 = LayoutStudentManipulationTextInputs2Binding.bind(inflatedViewBinding2!!)

            inflatedViewBinding2?.alpha = 0f
            inflatedViewBinding2?.translationY = -100f

            inflatedViewBinding2?.animate()
                ?.alpha(1f)
                ?.translationY(0f)
                ?.setDuration(210)
                ?.start()
        }

/** */
        viewBinding2.apply {
            edtBatch.setText(data.batch)

            edtClassSession.setText(data.classSessionName)
            studentData?.classSessionId = data.classSessionId

            edtSemester.setText(data.numberSemester)
            studentData?.semesterId = data.semesterId

            edtLectureMethod.setText(data.lectureMethodName)
            studentData?.lectureMethodId = data.lectureMethodId

            edtStudentStatus.setText(data.studentStatusName)
            studentData?.studentStatusId = data.studentStatusId

            edtEmploymentStatus.setText(data.employmentStatusName)
            studentData?.employmentStatusId = data.employmentStatusId

            edtCampus.setText(data.campusName)
            studentData?.campusId = data.campusId
        }

        viewBinding1.apply {
            edtFullname.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtConfirmPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtGender.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtStudentIdNumber.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtStudyProgram.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            edtGender.setOnClickListener { v ->
                hideKeyboard(v)
                GenderDialogFragment().show(
                    supportFragmentManager,
                    GenderDialogFragment::class.java.simpleName
                )
            }
            edtStudyProgram.setOnClickListener { v ->
                hideKeyboard(v)
                val intent = Intent(
                    this@StudentManipulationViewStubActivity,
                    StudentStudyProgramActivity::class.java
                ).apply {
                    putExtra(StudentStudyProgramActivity.KEY_STUDENT, true)
                }
                resultLauncher.launch(intent)
            }
        }
        viewBinding2.apply {
            edtBatch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtClassSession.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtSemester.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtLectureMethod.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtStudentStatus.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtEmploymentStatus.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })
            edtCampus.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            edtClassSession.setOnClickListener { v ->
                hideKeyboard(v)
                val intent = Intent(
                    this@StudentManipulationViewStubActivity,
                    ClassSessionActivity::class.java
                ).apply {
                    putExtra(ClassSessionActivity.KEY_STUDENT, true)
                }
                resultLauncher.launch(intent)
            }
            edtSemester.setOnClickListener { v ->
                hideKeyboard(v)
                val intent = Intent(
                    this@StudentManipulationViewStubActivity,
                    SemesterActivity::class.java
                ).apply {
                    putExtra(SemesterActivity.KEY_STUDENT, true)
                }
                resultLauncher.launch(intent)
            }
            edtLectureMethod.setOnClickListener { v ->
                hideKeyboard(v)
                val intent = Intent(
                    this@StudentManipulationViewStubActivity,
                    LectureMethodActivity::class.java
                ).apply {
                    putExtra(LectureMethodActivity.KEY_STUDENT, true)
                }
                resultLauncher.launch(intent)
            }
            edtStudentStatus.setOnClickListener { v ->
                hideKeyboard(v)
                val intent = Intent(
                    this@StudentManipulationViewStubActivity,
                    StudentStatusActivity::class.java
                ).apply {
                    putExtra(StudentStatusActivity.KEY_STUDENT, true)
                }
                resultLauncher.launch(intent)
            }
            edtEmploymentStatus.setOnClickListener { v ->
                hideKeyboard(v)
                val intent = Intent(
                    this@StudentManipulationViewStubActivity,
                    EmploymentStatusActivity::class.java
                ).apply {
                    putExtra(EmploymentStatusActivity.KEY_STUDENT, true)
                }
                resultLauncher.launch(intent)
            }
            edtCampus.setOnClickListener { v ->
                hideKeyboard(v)
                val intent = Intent(
                    this@StudentManipulationViewStubActivity,
                    CampusActivity::class.java
                ).apply {
                    putExtra(CampusActivity.KEY_STUDENT, true)
                }
                resultLauncher.launch(intent)
            }
            btnSave.setOnClickListener { save(token, viewBinding1, viewBinding2) }

        }

        buttonIsEnabledEditDeleteMode(viewBinding1, viewBinding2)


    }

    private fun showErrorClient(
        error: Errors,
        token: String = "",
        data: StudentData = StudentData()
    ) {
        val messageList = error.errors?.message
        val message = error.errors?.message?.first() ?: ""
        displayData(token)
        isDialogShowingOrientationError = true
        errorData?.message = messageList
        showAlertDialog(msg = message, status = STATUS_ERROR)
    }

    private fun showErrorServer(token: String, id: String, errorMessage: String) {
        setVisibilityToolbar(isInvisible = true)
        showLoadingMain(false)
        hideKeyboard()
        failedToConnect(token = token, id = id, display = true)
        Snackbar.make(
            binding.root as ViewGroup,
            errorMessage,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun setVisibilityToolbar(isInvisible: Boolean) {
        binding.toolbar.isInvisible = isInvisible
    }

    private fun buttonIsEnabledAddMode(
        viewBinding1: LayoutStudentManipulationTextInputs1Binding,
        viewBinding2: LayoutStudentManipulationTextInputs2Binding
    ) {
        val viewBinding1isNotEmpty = viewBinding1.let {
            it.edtFullname.text.toString().isNotEmpty() &&
                    it.edtPassword.text.toString().isNotEmpty() &&
                    it.edtConfirmPassword.text.toString().isNotEmpty() &&
                    it.edtGender.text.toString().isNotEmpty() &&
                    it.edtStudentIdNumber.text.toString().isNotEmpty() &&
                    it.edtStudyProgram.text.toString().isNotEmpty()
        }

        val viewBinding2isNotEmpty = viewBinding2.let {
            it.edtBatch.text.toString().isNotEmpty() &&
                    it.edtClassSession.text.toString().isNotEmpty() &&
                    it.edtSemester.text.toString().isNotEmpty() &&
                    it.edtLectureMethod.text.toString().isNotEmpty() &&
                    it.edtStudentStatus.text.toString().isNotEmpty() &&
                    it.edtEmploymentStatus.text.toString().isNotEmpty() &&
                    it.edtCampus.text.toString().isNotEmpty()
        }
        viewBinding2.btnSave.isEnabled = viewBinding1isNotEmpty && viewBinding2isNotEmpty

    }

    private fun buttonIsEnabledEditDeleteMode(
        viewBinding1: LayoutStudentManipulationTextInputs1Binding,
        viewBinding2: LayoutStudentManipulationTextInputs2Binding
    ) {
        val viewBinding1isNotEmpty = viewBinding1.let {
            it.edtFullname.text.toString().isNotEmpty() &&
                    it.edtGender.text.toString().isNotEmpty() &&
                    it.edtStudentIdNumber.text.toString().isNotEmpty() &&
                    it.edtStudyProgram.text.toString().isNotEmpty()
        }

        val viewBinding2isNotEmpty = viewBinding2.let {
            it.edtBatch.text.toString().isNotEmpty() &&
                    it.edtClassSession.text.toString().isNotEmpty() &&
                    it.edtSemester.text.toString().isNotEmpty() &&
                    it.edtLectureMethod.text.toString().isNotEmpty() &&
                    it.edtStudentStatus.text.toString().isNotEmpty() &&
                    it.edtEmploymentStatus.text.toString().isNotEmpty() &&
                    it.edtCampus.text.toString().isNotEmpty()
        }
        viewBinding2.btnSave.isEnabled = viewBinding1isNotEmpty && viewBinding2isNotEmpty

    }


    private fun save(
        token: String, viewBinding1: LayoutStudentManipulationTextInputs1Binding,
        viewBinding2: LayoutStudentManipulationTextInputs2Binding
    ) {
        hideKeyboard()
        viewBinding1.root.isVisible = false
        viewBinding2.root.isVisible = false
        if (isEditDeleteMode) {
            viewBinding1.apply {
                studentData?.fullName = edtFullname.text.toString().trim()
                studentData?.gender = edtGender.text.toString().lowercase()
                studentData?.studentIdNumber = edtStudentIdNumber.text.toString().trim()

            }
            viewBinding2.apply {
                studentData?.batch = edtBatch.text.toString().trim()
            }

            studentData?.let { data -> adminViewModel.updateStudent(token, id, data) }

        } else {
//                adminViewModel.addStudyProgram(
//                    IdentityAcademicData(
//                        id,
//                        code = edtCode.text.toString().trim(),
//                        facultyId = identityAcademicData?.facultyId,
//                        levelId = identityAcademicData?.levelId,
//                        majorId = identityAcademicData?.majorId,
//                        degreeId = identityAcademicData?.degreeId
//                    )
//                )
        }

    }


    private fun delete(
        id: String?,
        studentIdName: String,
        fullname: String
    ) {
        binding.apply {
            hideKeyboard()
            showAlertDialog(
                getString(R.string.format_string_strip_string, studentIdName, fullname),
                STATUS_DELETED,
                id ?: "0"
            )
        }
    }


    override fun onOptionChosen(text: String, category: String) {
        when (category) {
            GenderDialogFragment.KEY_OPTION_GENDER -> {
                setGender(viewBinding1, text)
                viewBinding1.edtGender.setText(text)
            }
        }
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


    private fun Activity.hideKeyboard(
        viewBinding1: LayoutStudentManipulationTextInputs1Binding? = null,
        viewBinding2: LayoutStudentManipulationTextInputs2Binding? = null
    ) {
        fun clearFocus() {
            viewBinding1?.let {
                it.inputLayoutFullname.editText?.clearFocus()
                it.inputLayoutPassword.editText?.clearFocus()
                it.inputLayoutConfirmPassword.editText?.clearFocus()
                it.inputLayoutGender.editText?.clearFocus()
                it.inputLayoutStudentIdNumber.editText?.clearFocus()
                it.inputLayoutStudyProgram.editText?.clearFocus()
            }
            viewBinding2?.let {
                it.inputLayoutBatch.editText?.clearFocus()
                it.inputLayoutClassSession.editText?.clearFocus()
                it.inputLayoutSemester.editText?.clearFocus()
                it.inputLayoutLectureMethod.editText?.clearFocus()
                it.inputLayoutStudentStatus.editText?.clearFocus()
                it.inputLayoutEmploymentStatus.editText?.clearFocus()
                it.inputLayoutCampus.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
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
//                            STATUS_DELETED -> adminViewModel.deleteStudyProgram(id)
                            STATUS_ERROR -> {
                                isDialogShowingOrientationError = false
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
    }

    private fun failedToConnect(token: String = "", id: String = "", display: Boolean) {
        if (binding.viewStubHandleDataConnection.parent != null) {
            val viewStubHandleDataConnection = CustomViewStub(binding.viewStubHandleDataConnection, binding.root)
            val viewHandleDataConnection = viewStubHandleDataConnection.inflate()
            viewBindingHandleDataConnection =
                LayoutHandleDataConnectionBinding.bind(viewHandleDataConnection)
        }
        viewBindingHandleDataConnection.viewFailedConnect.apply {
            btnRefresh.setOnClickListener {
                adminViewModel.getStudentById(token, id)
            }
            root.isVisible = display

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
        outState.putBoolean(KEY_DIALOG_SHOWING_ERROR, isDialogShowingOrientationError)
        outState.putParcelable(KEY_BUNDLE_ERROR, errorData)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATUS_DELETED = "status_deleted"
        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_ID = "key_extra_id"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 200

        const val KEY_BUNDLE_STUDENT = "key_bundle_student"

        private const val KEY_DIALOG_SHOWING_ERROR = "key_dialog_showing_error"
        private const val KEY_BUNDLE_ERROR = "key_bundle_error"
    }
}
