package com.yogadimas.simastekom.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.databinding.ActivityStudentMenuBinding
import com.yogadimas.simastekom.ui.student.campus.CampusActivity
import com.yogadimas.simastekom.ui.student.studyprogram.StudentStudyProgramActivity
import com.yogadimas.simastekom.ui.student.studyprogram.StudentStudyProgramMenuActivity

class StudentActivityMenu : AppCompatActivity() {

    private lateinit var binding: ActivityStudentMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            btnStudyProgram.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        StudentStudyProgramMenuActivity::class.java
                    )
                )
            }
            btnCampus.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentActivityMenu,
                        CampusActivity::class.java
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