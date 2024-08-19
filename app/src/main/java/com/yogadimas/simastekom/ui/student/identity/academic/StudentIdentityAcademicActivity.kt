package com.yogadimas.simastekom.ui.student.identity.academic

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityAcademicBinding
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityAcademicMenuBinding
import com.yogadimas.simastekom.ui.student.identity.academic.campus.CampusActivity
import com.yogadimas.simastekom.ui.student.identity.academic.campus.CampusManipulationActivity
import com.yogadimas.simastekom.ui.student.identity.academic.studyprogram.StudentStudyProgramMenuActivity

class StudentIdentityAcademicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentIdentityAcademicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentIdentityAcademicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            fabAdd.setOnClickListener {
//                resultLauncher.launch(
//                    Intent(
//                        this@StudentIdentityAcademicActivity,
//                        StudentIdentityAcademicManipulationActivity::class.java
//                    )
//                )
                startActivity(Intent(
                    this@StudentIdentityAcademicActivity,
                    StudentIdentityAcademicManipulationActivity::class.java
                ))
            }

        }

    }
}