package com.yogadimas.simastekom.ui.admin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.databinding.ActivityAdminMenuBinding
import com.yogadimas.simastekom.ui.identity.address.AddressHomeActivity
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalActivity
import com.yogadimas.simastekom.ui.lecturer.LecturerActivity

class AdminMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMenuBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupToolbar()
        setupButtonMenu()
    }

    private fun setupButtonMenu() {
        fun navigateTo(activityClass: Class<*>, role: String? = null) {
            val intent = Intent(this@AdminMenuActivity, activityClass)
            role?.let { roleValue ->
                intent.putExtra(AddressHomeActivity.KEY_EXTRA_ROLE, roleValue)
            }
            startActivity(intent)
        }

        binding.apply {

            btnAdmin.setOnClickListener { navigateTo(AdminActivity::class.java) }

            btnIdentityPersonal.setOnClickListener {
                navigateTo(IdentityPersonalActivity::class.java, Role.ADMIN.value)
            }

            btnAddressHomePersonal.setOnClickListener {
                navigateTo(AddressHomeActivity::class.java, Role.ADMIN.value)
            }
        }
    }

    private fun setupView() {
        binding = ActivityAdminMenuBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setStatusBarColor(window)
    }

    private fun setStatusBarColor(window: Window) {
        val typedValue = TypedValue()
        val theme = window.context.theme
        theme.resolveAttribute(R.color.md_theme_surface, typedValue, true)
        val color = typedValue.data
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = color
            val isLight = resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_NO

            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = isLight
                isAppearanceLightNavigationBars = isLight
            }
        }
    }


    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener { finish() }
        }
    }
}