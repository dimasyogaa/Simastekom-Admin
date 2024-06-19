package com.yogadimas.simastekom.ui.student.studyprogram

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.databinding.ActivityStudentStudyProgramMenuBinding
import com.yogadimas.simastekom.ui.student.studyprogram.faculty.StudentFacultyActivity
import com.yogadimas.simastekom.ui.student.studyprogram.level.StudentLevelActivity
import com.yogadimas.simastekom.ui.student.studyprogram.major.StudentMajorActivity
import com.yogadimas.simastekom.ui.student.studyprogram.major.StudentMajorManipulationActivity

class StudentStudyProgramMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentStudyProgramMenuBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentStudyProgramMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            btnFaculty.setOnClickListener {
                startActivity(
                    Intent(this@StudentStudyProgramMenuActivity, StudentFacultyActivity::class.java)
                )
            }

            btnLevel.setOnClickListener {
                startActivity(
                    Intent(this@StudentStudyProgramMenuActivity, StudentLevelActivity::class.java)
                )
            }

            btnMajor.setOnClickListener {
                startActivity(
                    Intent(this@StudentStudyProgramMenuActivity, StudentMajorActivity::class.java)
                )
            }
        }
    }

}