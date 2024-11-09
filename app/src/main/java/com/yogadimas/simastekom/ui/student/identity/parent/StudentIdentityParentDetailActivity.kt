package com.yogadimas.simastekom.ui.student.identity.parent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.databinding.ActivityStudentIdentityParentDetailBinding

class StudentIdentityParentDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentIdentityParentDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentIdentityParentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}