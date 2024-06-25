package com.yogadimas.simastekom.ui.student.identity.academic

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityAcademicMenuBinding
import com.yogadimas.simastekom.databinding.ActivityStudentMenuBinding
import com.yogadimas.simastekom.ui.student.StudentActivity
import com.yogadimas.simastekom.ui.student.identity.academic.campus.CampusActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.StudentStudyProgramMenuActivity

class StudentIdentityAcademicMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentIdentityAcademicMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentIdentityAcademicMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            btnStudyProgram.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentIdentityAcademicMenuActivity,
                        StudentStudyProgramMenuActivity::class.java
                    )
                )
            }

            btnCampus.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentIdentityAcademicMenuActivity,
                        CampusActivity::class.java
                    )
                )
            }

            btnIdentityAcademic.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentIdentityAcademicMenuActivity,
                        StudentIdentityAcademicMenuActivity::class.java
                    )
                )
            }

        }

    }

}