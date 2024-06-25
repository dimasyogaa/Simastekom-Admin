package com.yogadimas.simastekom.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.databinding.ActivityStudentMenuBinding
import com.yogadimas.simastekom.ui.student.identity.academic.StudentIdentityAcademicMenuActivity
import com.yogadimas.simastekom.ui.student.identity.academic.campus.CampusActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.StudentStudyProgramMenuActivity

class StudentActivityMenu : AppCompatActivity() {

    private lateinit var binding: ActivityStudentMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            btnIdentityAcademic.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        StudentIdentityAcademicMenuActivity::class.java
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