package com.yogadimas.simastekom.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.core.ui.model.auth.AuthUserUiModel
import com.yogadimas.simastekom.databinding.ActivityStudentMenuBinding
import com.yogadimas.simastekom.ui.identity.address.AddressHomeActivity
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalActivity
import com.yogadimas.simastekom.ui.student.identity.academic.StudentIdentityAcademicMenuActivity
import com.yogadimas.simastekom.ui.student.identity.parent.StudentIdentityParentActivity
import com.yogadimas.simastekom.ui.student.status.employment.EmploymentStatusActivity
import com.yogadimas.simastekom.ui.student.status.student.StudentStatusActivity
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StudentMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentMenuBinding
    private val activityContext = this@StudentMenuActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListener()


    }

    private suspend fun getToken(): String = authViewModel.getUser().asFlow().first().first
    private suspend fun getUserId(): String = authViewModel.getUser().asFlow().first().second
    private suspend fun getUserType(): String = authViewModel.getUser().asFlow().first().third


    private fun setupListener() = binding.apply {
        toolbar.setNavigationOnClickListener { finish() }
        fun navigateTo(activityClass: Class<*>, role: String? = null) {
            val intent = Intent(activityContext, activityClass)
            role?.let { roleValue ->
                intent.putExtra(AddressHomeActivity.KEY_EXTRA_ROLE, roleValue)
            }
            startActivity(intent)
        }

        btnSimastekomMahasiswa.setOnClickListener {
            lifecycleScope.launch {
                val user = AuthUserUiModel(getToken(), getUserId(), getUserType())
                val intent = Intent().setClassName(
                    activityContext,
                    "com.yogadimas.simastekom.simastekom_mahasiswa.ui.main.SMMainActivity"
                )
                intent.putExtra("auth_user", user)
                startActivity(intent)

            }
        }

        btnStudent.setOnClickListener {
            navigateTo(StudentActivity::class.java)
        }

        btnIdentityPersonal.setOnClickListener {
            navigateTo(IdentityPersonalActivity::class.java, Role.STUDENT.value)
        }

        btnIdentityAcademic.setOnClickListener {
            navigateTo(StudentIdentityAcademicMenuActivity::class.java)
        }

        btnIdentityParent.setOnClickListener {
            navigateTo(StudentIdentityParentActivity::class.java)
        }

        btnStudentStatus.setOnClickListener {
            navigateTo(StudentStatusActivity::class.java)
        }

        btnEmploymentStatus.setOnClickListener {
            navigateTo(EmploymentStatusActivity::class.java)
        }

        btnAddressHomePersonal.setOnClickListener {
            navigateTo(AddressHomeActivity::class.java, Role.STUDENT.value)
        }

        btnAddressHomeParent.setOnClickListener {
            navigateTo(AddressHomeActivity::class.java, Role.PARENT.value)
        }
    }

}