package com.yogadimas.simastekom.ui.student.studyprogram

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.databinding.ActivityStudentStudyProgramMenuBinding
import com.yogadimas.simastekom.ui.student.studyprogram.level.StudentLevelActivity

class StudentStudyProgramMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentStudyProgramMenuBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentStudyProgramMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }

            btnLevel.setOnClickListener {
                startActivity(
                    Intent(this@StudentStudyProgramMenuActivity, StudentLevelActivity::class.java)
                )
            }
        }
    }

}