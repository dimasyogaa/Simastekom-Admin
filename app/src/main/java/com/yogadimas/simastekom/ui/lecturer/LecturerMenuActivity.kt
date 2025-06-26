package com.yogadimas.simastekom.ui.lecturer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.databinding.ActivityLecturerMenuBinding
import com.yogadimas.simastekom.ui.identity.address.AddressHomeActivity
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalActivity

class LecturerMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLecturerMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fun navigateTo(activityClass: Class<*>, role: String? = null) {
            val intent = Intent(this@LecturerMenuActivity, activityClass)
            role?.let { roleValue ->
                intent.putExtra(AddressHomeActivity.KEY_EXTRA_ROLE, roleValue)
            }
            startActivity(intent)
        }

        binding.apply {

            toolbar.setNavigationOnClickListener { finish() }

            btnLecturer.setOnClickListener{navigateTo(LecturerActivity::class.java)}

            btnIdentityPersonal.setOnClickListener {
                navigateTo(IdentityPersonalActivity::class.java, Role.LECTURE.value)
            }

            btnAddressHomePersonal.setOnClickListener {
                navigateTo(AddressHomeActivity::class.java, Role.LECTURE.value)
            }

            btnHeadOfStudyProgram.setOnClickListener {

            }

        }


    }
}