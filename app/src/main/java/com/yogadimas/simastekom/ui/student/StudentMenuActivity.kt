package com.yogadimas.simastekom.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.databinding.ActivityStudentMenuBinding
import com.yogadimas.simastekom.ui.identity.address.AddressHomeActivity
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalActivity
import com.yogadimas.simastekom.ui.student.identity.academic.StudentIdentityAcademicMenuActivity
import com.yogadimas.simastekom.ui.student.identity.parent.StudentIdentityParentActivity
import com.yogadimas.simastekom.ui.student.status.employment.EmploymentStatusActivity
import com.yogadimas.simastekom.ui.student.status.student.StudentStatusActivity

class StudentMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            fun navigateTo(activityClass: Class<*>, role: String? = null) {
                val intent = Intent(this@StudentMenuActivity, activityClass)
                role?.let { roleValue ->
                    intent.putExtra(AddressHomeActivity.KEY_EXTRA_ROLE, roleValue)
                }
                startActivity(intent)
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
}