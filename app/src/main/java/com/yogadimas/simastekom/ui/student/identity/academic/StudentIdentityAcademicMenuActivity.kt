package com.yogadimas.simastekom.ui.student.identity.academic

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityAcademicMenuBinding
import com.yogadimas.simastekom.ui.student.identity.academic.campus.CampusActivity
import com.yogadimas.simastekom.ui.student.identity.academic.classsession.ClassSessionActivity
import com.yogadimas.simastekom.ui.student.identity.academic.lecturemethod.LectureMethodActivity
import com.yogadimas.simastekom.ui.student.identity.academic.semester.SemesterActivity
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


            btnClassSession.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentIdentityAcademicMenuActivity,
                        ClassSessionActivity::class.java
                    )
                )
            }

            btnSemester.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentIdentityAcademicMenuActivity,
                        SemesterActivity::class.java
                    )
                )
            }

            btnLectureMethod.setOnClickListener {
                startActivity(
                    Intent(
                        this@StudentIdentityAcademicMenuActivity,
                        LectureMethodActivity::class.java
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
                        StudentIdentityAcademicActivity::class.java
                    )
                )
            }

            btnClassGroup.setOnClickListener {

            }

        }

    }

}