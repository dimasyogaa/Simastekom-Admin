package com.yogadimas.simastekom.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.databinding.ActivityStudentMenuBinding
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalActivity
import com.yogadimas.simastekom.ui.student.identity.academic.StudentIdentityAcademicMenuActivity
import com.yogadimas.simastekom.ui.student.identity.academic.campus.CampusActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.StudentStudyProgramMenuActivity
import com.yogadimas.simastekom.ui.student.status.employment.EmploymentStatusActivity
import com.yogadimas.simastekom.ui.student.status.student.StudentStatusActivity

class StudentActivityMenu : AppCompatActivity() {

    private lateinit var binding: ActivityStudentMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            btnIdentityPersonal.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        IdentityPersonalActivity::class.java
                    ).putExtra(IdentityPersonalActivity.KEY_EXTRA_ROLE, Role.STUDENT.value)
                )
            }

            btnIdentityAcademic.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        StudentIdentityAcademicMenuActivity::class.java
                    )
                )
            }
            btnStudentStatus.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        StudentStatusActivity::class.java
                    )
                )
            }
            btnEmploymentStatus.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        EmploymentStatusActivity::class.java
                    )
                )
            }
            btnStudent.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        StudentActivity::class.java
                    )
                )
            }
        }

    }
}